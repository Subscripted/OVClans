package dev.subscripted.eloriseClans.manager;

import dev.subscripted.eloriseClans.Main;
import dev.subscripted.eloriseClans.cache.RankCache;
import dev.subscripted.eloriseClans.database.MySQL;
import dev.subscripted.eloriseClans.cache.ChunkCache;
import dev.subscripted.eloriseClans.settings.ChunkVisualizer;
import dev.subscripted.eloriseClans.utils.ClanChunk;
import dev.subscripted.eloriseClans.cache.SkullTextureCache;
import dev.subscripted.eloriseClans.utils.UUIDFetcher;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@RequiredArgsConstructor()
public class ClanManager {

    final MySQL mySQL;
    final Set<UUID> addingMoney = new HashSet<>();
    final Set<UUID> removingMoney = new HashSet<>();
    private static final Map<Integer, Integer> levelChunkMap = Map.of(
            1, 4,
            2, 0,
            3, 1,
            4, 1,
            5, 3,
            6, 1,
            7, 0,
            8, 0,
            9, 2,
            10, 3
    );


    @SneakyThrows
    public void createClan(UUID owner, String clanPrefix, String clanName) throws SQLException, ExecutionException, InterruptedException {
        String createClanQuery = "INSERT INTO Clans (ClanPrefix, ClanName, ClanLevel, Chunks) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(createClanQuery)) {
            int additionalChunks = levelChunkMap.getOrDefault(1, 4);
            stmt.setString(1, clanPrefix);
            stmt.setString(2, clanName);
            stmt.setInt(3, 1);
            stmt.setInt(4, additionalChunks);
            stmt.executeUpdate();
        }
        addMemberToClan(clanPrefix, owner, "Owner");
        addClanSettings(clanPrefix);
    }

    public void deleteClan(String clanPrefix) throws SQLException {
        String deleteClanQuery = "DELETE FROM Clans WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(deleteClanQuery)) {
            stmt.setString(1, clanPrefix);
            stmt.executeUpdate();
        }
        removeAllChunksFromClan(clanPrefix);
        removeMembersFromClan(clanPrefix);
    }

    private void removeAllChunksFromClan(String clanPrefix) throws SQLException {
        String deleteChunksQuery = "DELETE FROM ClaimedChunks WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(deleteChunksQuery)) {
            stmt.setString(1, clanPrefix);
            stmt.executeUpdate();
        }

        ChunkCache.getAllChunks().stream()
                .filter(chunk -> chunk.getClanPrefix().equals(clanPrefix))
                .forEach(chunk -> ChunkCache.removeChunk(chunk.getWorld(), chunk.getChunkX(), chunk.getChunkZ()));
    }

    @SneakyThrows
    public void addMemberToClan(String clanPrefix, UUID memberUUID, String rank) {
        String addMemberQuery = "INSERT INTO ClanMembers (ClanPrefix, MemberUUID, Rank) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(addMemberQuery)) {
            stmt.setString(1, clanPrefix);
            stmt.setString(2, memberUUID.toString());
            stmt.setString(3, rank);
            stmt.executeUpdate();
        }
        SkullTextureCache.updateCache(memberUUID);
    }

    @SneakyThrows
    public void removeMemberFromClan(String clanPrefix, UUID memberUUID) throws SQLException, ExecutionException, InterruptedException {
        String removeMemberQuery = "DELETE FROM ClanMembers WHERE ClanPrefix = ? AND MemberUUID = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(removeMemberQuery)) {
            stmt.setString(1, clanPrefix);
            stmt.setString(2, memberUUID.toString());
            stmt.executeUpdate();
        }
        RankCache.removeFromCache(memberUUID);
    }

    @SneakyThrows
    public void addClanSettings(String clanPrefix) {
        String addSettingsQuery = "INSERT INTO ClanSettings (ClanPrefix, pvp) VALUES (?, ?)";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(addSettingsQuery)) {
            stmt.setString(1, clanPrefix);
            stmt.setBoolean(2, true);
            stmt.executeUpdate();
        }
    }


    @SneakyThrows
    public void leaveClan(UUID memberUUID) throws SQLException, ExecutionException, InterruptedException {
        String leaveClanQuery = "DELETE FROM ClanMembers WHERE MemberUUID = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(leaveClanQuery)) {
            stmt.setString(1, memberUUID.toString());
            stmt.executeUpdate();
        }
    }

    @SneakyThrows
    public void removeMembersFromClan(String clanPrefix) {
        String removeMembersQuery = "DELETE FROM ClanMembers WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(removeMembersQuery)) {
            stmt.setString(1, clanPrefix);
            stmt.executeUpdate();
        }
    }

    @SneakyThrows
    public String getMemberClan(UUID memberUUID) throws SQLException, ExecutionException, InterruptedException {
        String getMemberClanQuery = "SELECT ClanPrefix FROM ClanMembers WHERE MemberUUID = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(getMemberClanQuery)) {
            stmt.setString(1, memberUUID.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("ClanPrefix");
            }
        }
        return null;
    }

    @SneakyThrows
    public boolean isMemberOfClan(UUID memberUUID, String clanPrefix) throws SQLException {
        String isMemberQuery = "SELECT 1 FROM ClanMembers WHERE MemberUUID = ? AND ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(isMemberQuery)) {
            stmt.setString(1, memberUUID.toString());
            stmt.setString(2, clanPrefix);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    @SneakyThrows
    public boolean isOwnerOfClan(UUID ownerUUID, String clanPrefix) throws SQLException {
        String isOwnerQuery = "SELECT 1 FROM ClanMembers WHERE MemberUUID = ? AND ClanPrefix = ? AND Rank = 'Owner'";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(isOwnerQuery)) {
            stmt.setString(1, ownerUUID.toString());
            stmt.setString(2, clanPrefix);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }


    @SneakyThrows
    public List<UUID> getAllMembersAsList(String clanPrefix) {
        List<UUID> members = new ArrayList<>();
        String getMembersQuery = "SELECT MemberUUID FROM ClanMembers WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(getMembersQuery)) {
            stmt.setString(1, clanPrefix);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(UUID.fromString(rs.getString("MemberUUID")));
            }
        }
        return members;
    }

    @SneakyThrows
    public boolean clanExists(String clanPrefix) {
        String clanExistsQuery = "SELECT ClanName FROM Clans WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(clanExistsQuery)) {
            stmt.setString(1, clanPrefix);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    @SneakyThrows
    public boolean isClanNameTaken(String clanName) throws SQLException {
        String clanNameTakenQuery = "SELECT ClanName FROM Clans WHERE ClanName = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(clanNameTakenQuery)) {
            stmt.setString(1, clanName);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    @SneakyThrows
    public String getClanNameByPrefix(String clanPrefix) throws SQLException {
        String getClanNameQuery = "SELECT ClanName FROM Clans WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(getClanNameQuery)) {
            stmt.setString(1, clanPrefix);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("ClanName");
            }
        }
        return null;
    }

    @SneakyThrows
    public String getClanPrefix(UUID memberUUID) {
        String getClanPrefixQuery = "SELECT ClanPrefix FROM ClanMembers WHERE MemberUUID = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(getClanPrefixQuery)) {
            stmt.setString(1, memberUUID.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("ClanPrefix");
            }
        }
        return null;
    }

    @SneakyThrows
    public String getClanName(UUID memberUUID) {
        String clanPrefix = getClanPrefix(memberUUID);
        if (clanPrefix != null) {
            return getClanNameByPrefix(clanPrefix);
        }
        return null;
    }

    public List<UUID> getClanMembers(String clanPrefix) {
        return getAllMembersAsList(clanPrefix);
    }

    public UUID getClanOwner(String rank, String prefix) throws SQLException {
        String getClanOwnerQuery = "SELECT MemberUUID FROM ClanMembers WHERE Rank = ? AND ClanPrefix = ?";
        try (PreparedStatement stmt = MySQL.getConnection().prepareStatement(getClanOwnerQuery)) {
            stmt.setString(1, rank);
            stmt.setString(2, prefix);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString("MemberUUID"));
            }
        }
        return null;
    }

    @SneakyThrows
    public boolean isClanOwner(UUID ownerUUID) {
        String isClanOwnerQuery = "SELECT 1 FROM ClanMembers WHERE MemberUUID = ? AND Rank = 'Owner'";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(isClanOwnerQuery)) {
            stmt.setString(1, ownerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }


    public boolean isClanMember(UUID memberUUID) throws SQLException {
        String isClanMemberQuery = "SELECT ClanPrefix FROM ClanMembers WHERE MemberUUID = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(isClanMemberQuery)) {
            stmt.setString(1, memberUUID.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public String getClan(UUID memberUUID) throws SQLException, ExecutionException, InterruptedException {
        String getClanQuery = "SELECT Clans.ClanPrefix, Clans.ClanName " +
                "FROM Clans JOIN ClanMembers ON Clans.ClanPrefix = ClanMembers.ClanPrefix " +
                "WHERE ClanMembers.MemberUUID = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(getClanQuery)) {
            stmt.setString(1, memberUUID.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("ClanPrefix") + " - " + rs.getString("ClanName");
            }
        }
        return null;
    }

    @SneakyThrows
    public List<String> getClans() throws SQLException {
        List<String> clans = new ArrayList<>();
        String query = "SELECT ClanPrefix FROM Clans";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String prefix = rs.getString("ClanPrefix");
                clans.add(prefix);
            }
        }

        return clans;
    }

    public UUID getClanOwnerUUID(String clanPrefix) throws SQLException {
        String query = "SELECT MemberUUID FROM ClanMembers WHERE ClanPrefix = ? AND Rank = 'Owner' LIMIT 1";

        try (PreparedStatement stmt = MySQL.getConnection().prepareStatement(query)) {
            stmt.setString(1, clanPrefix);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(rs.getString("MemberUUID"));
                }
            }
        }

        return null; // Falls kein Owner gefunden wird
    }

    public int getClanLevel(String clanPrefix) throws SQLException, ExecutionException, InterruptedException {
        String getLevelQuery = "SELECT ClanLevel FROM Clans WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(getLevelQuery)) {
            stmt.setString(1, clanPrefix);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ClanLevel");
            }
        }

        return 0;
    }

    public int getClanEco(String clanPrefix) throws SQLException, ExecutionException, InterruptedException {
        String getEcoQuery = "SELECT ClanbankEco FROM Clans WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(getEcoQuery)) {
            stmt.setString(1, clanPrefix);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ClanbankEco");
            }
        }
        return 0;
    }

    public List<Map<String, Object>> getTop5Clans() throws SQLException {
        List<Map<String, Object>> clanDataList = new ArrayList<>();

        // SQL-Query, um Clans und Mitgliederanzahl zu berechnen
        String query = """
        SELECT c.ClanPrefix, c.ClanName, c.ClanLevel, 
               (SELECT COUNT(*) FROM clanmembers m WHERE m.ClanPrefix = c.ClanPrefix) AS Members
        FROM clans c
        ORDER BY c.ClanLevel DESC, Members DESC
        LIMIT 5
    """;

        try (PreparedStatement stmt = MySQL.getConnection().prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> clanData = new HashMap<>();
                clanData.put("ClanPrefix", rs.getString("ClanPrefix"));
                clanData.put("ClanName", rs.getString("ClanName"));
                clanData.put("ClanLevel", rs.getInt("ClanLevel"));
                clanData.put("Members", rs.getInt("Members"));
                clanDataList.add(clanData);
            }
        }

        return clanDataList;
    }


    public void setClanEco(String clanPrefix, int newEco) throws
            SQLException, ExecutionException, InterruptedException {
        String setEcoQuery = "UPDATE Clans SET ClanbankEco = ? WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(setEcoQuery)) {
            stmt.setInt(1, newEco);
            stmt.setString(2, clanPrefix);
            stmt.executeUpdate();
        }
    }

    public void setClanLevel(String clanPrefix, int newLevel) throws
            SQLException, ExecutionException, InterruptedException {
        String setEcoQuery = "UPDATE Clans SET ClanLevel = ? WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(setEcoQuery)) {
            stmt.setInt(1, newLevel);
            stmt.setString(2, clanPrefix);
            stmt.executeUpdate();
        }
    }

    public void setClanToNextLevel(String clanPrefix) throws SQLException, ExecutionException, InterruptedException {
        // Aktuelles Level des Clans abrufen
        int currentLevel = getClanLevel(clanPrefix);
        int nextLevel = currentLevel + 1;

        // Zusätzliche Chunks aus der Map holen
        int additionalChunks = levelChunkMap.getOrDefault(nextLevel, 0);

        // SQL-Query: Clan-Level erhöhen und zusätzliche Chunks hinzufügen
        String updateQuery = "UPDATE Clans SET ClanLevel = ?, Chunks = Chunks + ? WHERE ClanPrefix = ?";

        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(updateQuery)) {
            stmt.setInt(1, nextLevel);          // Neues Level setzen
            stmt.setInt(2, additionalChunks);  // Zusätzliche Chunks hinzufügen
            stmt.setString(3, clanPrefix);     // Clan-Präfix für die Abfrage
            stmt.executeUpdate();
        }
    }


    public void addMoneyToClanBank(String clanPrefix, int amount) throws
            SQLException, ExecutionException, InterruptedException {
        int currentEco = getClanEco(clanPrefix);
        int newEco = currentEco + amount;
        setClanEco(clanPrefix, newEco);
    }

    public void removeMoneyFromClanBank(String clanPrefix, int amount) throws
            SQLException, ExecutionException, InterruptedException {
        int currentEco = getClanEco(clanPrefix);
        int newEco = currentEco - amount;
        setClanEco(clanPrefix, newEco);
    }

    @SneakyThrows
    public void grantToClanOwner(UUID newOwnerUUID, String clanPrefix) {
        // Prüfen, ob der Clan existiert
        if (!clanExists(clanPrefix)) {
            System.out.println("Der Clan mit dem Prefix " + clanPrefix + " existiert nicht.");
            return;
        }

        // Prüfen, ob der neue Besitzer Mitglied des Clans ist
        if (!isMemberOfClan(newOwnerUUID, clanPrefix)) {
            System.out.println("Der Benutzer mit UUID " + newOwnerUUID + " ist kein Mitglied des Clans " + clanPrefix + ".");
            return;
        }

        // Aktuellen Besitzer ermitteln
        UUID currentOwnerUUID = getClanOwner("Owner", clanPrefix);
        if (currentOwnerUUID == null) {
            System.out.println("Der Clan " + clanPrefix + " hat keinen aktuellen Besitzer.");
            return;
        }

        // Rang des aktuellen Besitzers ändern
        setMemberRank(currentOwnerUUID, clanPrefix, "Vize");

        // Rang des neuen Besitzers setzen
        setMemberRank(newOwnerUUID, clanPrefix, "Owner");
    }


    public boolean isAddingMoney(Player player) {
        return addingMoney.contains(player.getUniqueId());
    }

    public boolean isRemovingMoney(Player player) {
        return removingMoney.contains(player.getUniqueId());
    }

    public void addToAddingMoney(Player player) {
        addingMoney.add(player.getUniqueId());
    }

    public void addToRemovingMoney(Player player) {
        removingMoney.add(player.getUniqueId());
    }

    public void removeFromAddingMoney(Player player) {
        addingMoney.remove(player.getUniqueId());
    }

    public void removeFromRemovingMoney(Player player) {
        removingMoney.remove(player.getUniqueId());
    }

    public String getUUID(String playername) {
        Player player = Main.getInstance().getServer().getPlayer(playername);
        if (player != null) {
            return player.getUniqueId().toString();
        } else {
            return UUIDFetcher.getUUID(playername).toString();
        }
    }

    @SneakyThrows
    public void activateClanPVP(String clanPrefix) throws SQLException {
        String activatePVPQuery = "UPDATE ClanSettings SET pvp = ? WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(activatePVPQuery)) {
            stmt.setBoolean(1, true);
            stmt.setString(2, clanPrefix);
            stmt.executeUpdate();
        }
    }

    @SneakyThrows
    public void deactivateClanPVP(String clanPrefix) throws SQLException {
        String deactivatePVPQuery = "UPDATE ClanSettings SET pvp = ? WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(deactivatePVPQuery)) {
            stmt.setBoolean(1, false);
            stmt.setString(2, clanPrefix);
            stmt.executeUpdate();
        }
    }

    @SneakyThrows
    public boolean isClanPVPEnabled(String clanPrefix) throws SQLException {
        String checkPVPQuery = "SELECT pvp FROM ClanSettings WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(checkPVPQuery)) {
            stmt.setString(1, clanPrefix);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("pvp");
            }
        }
        return false;
    }

    @SneakyThrows
    public boolean isClanBorderShown(String clanPrefix) throws SQLException {
        String checkPVPQuery = "SELECT clanborder FROM ClanSettings WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = MySQL.getConnection().prepareStatement(checkPVPQuery)) {
            stmt.setString(1, clanPrefix);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("clanborder");
            }
        }
        return false;
    }


    @SneakyThrows
    public void hideBorders(String clanPrefix) throws SQLException {
        String updateQuery = "UPDATE ClanSettings SET clanborder = 0 WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = MySQL.getConnection().prepareStatement(updateQuery)) {
            stmt.setString(1, clanPrefix);
            stmt.executeUpdate();
        }
    }

    @SneakyThrows
    public void showBorders(String clanPrefix) throws SQLException {
        String updateQuery = "UPDATE ClanSettings SET clanborder = 1 WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = MySQL.getConnection().prepareStatement(updateQuery)) {
            stmt.setString(1, clanPrefix);
            stmt.executeUpdate();
        }
    }


    @SneakyThrows
    public void setMemberRank(UUID memberUUID, String clanPrefix, String rank) throws SQLException {
        if (!Arrays.asList("Mitglied", "Ältester", "Vize", "Owner").contains(rank)) {
            throw new IllegalArgumentException("Ungültiger Rang: " + rank);
        }

        String updateRankQuery = "UPDATE ClanMembers SET Rank = ? WHERE MemberUUID = ? AND ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(updateRankQuery)) {
            stmt.setString(1, rank);
            stmt.setString(2, memberUUID.toString());
            stmt.setString(3, clanPrefix);
            stmt.executeUpdate();
        }
        RankCache.updateCache(memberUUID, rank);
    }

    @SneakyThrows
    public String getMemberRank(UUID memberUUID, String clanPrefix) throws SQLException {
        String getRankQuery = "SELECT Rank FROM ClanMembers WHERE MemberUUID = ? AND ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(getRankQuery)) {
            stmt.setString(1, memberUUID.toString());
            stmt.setString(2, clanPrefix);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Rank");
            }
        }
        return null;
    }

    @SneakyThrows
    public boolean hasMemberRank(UUID memberUUID, String clanPrefix, String rank) throws SQLException {
        String getRankQuery = "SELECT 1 FROM ClanMembers WHERE MemberUUID = ? AND ClanPrefix = ? AND Rank = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(getRankQuery)) {
            stmt.setString(1, memberUUID.toString());
            stmt.setString(2, clanPrefix);
            stmt.setString(3, rank);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    @SneakyThrows
    public List<UUID> getMembersByRank(String clanPrefix, String rank) throws SQLException {
        List<UUID> members = new ArrayList<>();
        String getMembersQuery = "SELECT MemberUUID FROM ClanMembers WHERE ClanPrefix = ? AND Rank = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(getMembersQuery)) {
            stmt.setString(1, clanPrefix);
            stmt.setString(2, rank);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(UUID.fromString(rs.getString("MemberUUID")));
            }
        }
        return members;
    }

    public boolean claimChunk(Player player, String clanPrefix) {
        Chunk chunk = player.getLocation().getChunk();
        String world = chunk.getWorld().getName();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        try {
            // Überprüfen, ob der Chunk bereits geclaimt wurde
            if (ChunkCache.isChunkClaimed(world, chunkX, chunkZ)) {
                ClanChunk existingClaim = ChunkCache.getClaimedChunk(world, chunkX, chunkZ);
                if (existingClaim != null) {
                    if (existingClaim.getClanPrefix().equals(clanPrefix)) {
                        player.sendMessage("Dieser Chunk gehört bereits deinem Clan!");
                    } else {
                        player.sendMessage("Dieser Chunk gehört bereits dem Clan: " + existingClaim.getClanPrefix());
                    }
                } else {
                    player.sendMessage("Dieser Chunk wurde bereits geclaimt!");
                }
                return false;
            }

            // Aktuelle Anzahl geclaimter Chunks des Clans abrufen
            int claimedChunks = getClaimedChunkCount(clanPrefix);

            // Maximale Anzahl an Chunks für den Clan abrufen
            int maxChunks = getAvailableChunks(clanPrefix);

            // Überprüfen, ob die maximale Anzahl bereits erreicht wurde
            if (claimedChunks >= maxChunks) {
                player.sendMessage("Dein Clan hat bereits die maximale Anzahl an geclaimten Chunks erreicht (" + maxChunks + ").");
                return false;
            }

            // Chunk claimen
            PreparedStatement claimStatement = MySQL.getConnection().prepareStatement(
                    "INSERT INTO ClaimedChunks (ClanPrefix, World, ChunkX, ChunkZ) VALUES (?, ?, ?, ?)"
            );
            claimStatement.setString(1, clanPrefix);
            claimStatement.setString(2, world);
            claimStatement.setInt(3, chunkX);
            claimStatement.setInt(4, chunkZ);

            claimStatement.executeUpdate();

            // Chunk in den Cache hinzufügen
            ClanChunk clanChunk = new ClanChunk(world, chunkX, chunkZ, clanPrefix);
            ChunkCache.addChunk(clanChunk);

            player.sendMessage("Der Chunk wurde erfolgreich geclaimt!");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("Ein Fehler ist beim Claimen des Chunks aufgetreten.");
            return false;
        }
    }


    @SneakyThrows
    public boolean delClaim(Player player, String clanPrefix) {
        Chunk chunk = player.getLocation().getChunk();
        String world = chunk.getWorld().getName();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        ClanChunk claimedChunk = ChunkCache.getClaimedChunk(world, chunkX, chunkZ);
        if (claimedChunk == null) {
            player.sendMessage("§cDieser Chunk ist nicht geclaimt!");
            return false;
        }

        if (!claimedChunk.getClanPrefix().equals(clanPrefix)) {
            player.sendMessage("§cDu kannst nur Chunks löschen, die deinem Clan gehören!");
            return false;
        }

        try (Connection connection = MySQL.getConnection()) {
            PreparedStatement deleteStatement = connection.prepareStatement(
                    "DELETE FROM ClaimedChunks WHERE ClanPrefix = ? AND World = ? AND ChunkX = ? AND ChunkZ = ?"
            );
            deleteStatement.setString(1, clanPrefix);
            deleteStatement.setString(2, world);
            deleteStatement.setInt(3, chunkX);
            deleteStatement.setInt(4, chunkZ);
            deleteStatement.executeUpdate();

            ChunkCache.removeChunk(world, chunkX, chunkZ);

            player.sendMessage("§aDer Claim wurde erfolgreich gelöscht!");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("§cEin Fehler ist beim Löschen des Claims aufgetreten.");
            return false;
        }
    }

    @SneakyThrows
    public String getLastSeen(UUID playerUUID) {
        String query = "SELECT lastSeen FROM ClanMembers WHERE MemberUUID = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Timestamp lastSeen = rs.getTimestamp("lastSeen");
                SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy HH.mm");
                return sdf.format(lastSeen) + " Uhr";
            }
        }
        return null;
    }

    @SneakyThrows
    public void saveLastSeen(UUID playerUUID, String prefix) {
        String query = "UPDATE ClanMembers SET lastSeen = CURRENT_TIMESTAMP WHERE MemberUUID = ? AND ClanPrefix = ?";
        try (PreparedStatement stmt = MySQL.getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, prefix);
            stmt.executeUpdate();
        }
    }

    public void startShowingClaims(Player player, String clanPrefix) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (!isClanBorderShown(clanPrefix)) {
                        cancel();
                        return;
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                try {
                    if (isMemberOfClan(player.getUniqueId(), clanPrefix)) {

                        ChunkCache.getAllChunks().stream()
                                .filter(chunk -> chunk.getClanPrefix().equals(clanPrefix))
                                .forEach(chunk -> {
                                    ChunkVisualizer.showChunkBorder(player, chunk);
                                });
                    } else {
                        cancel();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 5L);
    }

    public boolean doesClanExist(String clanPrefix) {
        String query = "SELECT COUNT(*) FROM Clans WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = MySQL.getConnection().prepareStatement(query)) {
            stmt.setString(1, clanPrefix);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public Map<UUID, String> fetchPlayersFromDatabase() {
        Map<UUID, String> players = new HashMap<>();
        String query = "SELECT MemberUUID, Rank FROM ClanMembers";

        try (Connection connection = MySQL.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("MemberUUID"));
                players.put(uuid, resultSet.getString("Rank"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return players;
    }


    public Map<UUID, String> fetchRanksFromDatabase() {
        Map<UUID, String> ranks = new HashMap<>();
        String query = "SELECT MemberUUID, Rank FROM ClanMembers";

        try (Connection connection = MySQL.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("MemberUUID"));
                ranks.put(uuid, resultSet.getString("Rank"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ranks;
    }

    public Map<UUID, String> fetchLastJoinedFromDatabase() {
        Map<UUID, String> lastJoined = new HashMap<>();
        String query = "SELECT MemberUUID, lastSeen FROM ClanMembers";

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        try (Connection connection = MySQL.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("MemberUUID"));
                Timestamp lastSeenTimestamp = resultSet.getTimestamp("lastSeen");

                String formattedLastSeen = dateFormat.format(lastSeenTimestamp);
                lastJoined.put(uuid, formattedLastSeen);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastJoined;
    }


    public Map<UUID, String> fetchPlayerNamesFromDatabase() {
        Map<UUID, String> playerNames = new HashMap<>();
        String query = "SELECT MemberUUID, PlayerName FROM ClanMembers";

        try (Connection connection = MySQL.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("MemberUUID"));
                playerNames.put(uuid, resultSet.getString("PlayerName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playerNames;
    }

    public List<UUID> fetchAllPlayerUUIDs() {
        List<UUID> uuids = new ArrayList<>();
        String query = "SELECT MemberUUID FROM ClanMembers";

        try (Connection connection = MySQL.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                uuids.add(UUID.fromString(resultSet.getString("MemberUUID")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uuids;
    }

    public int getAvailableChunks(String clanPrefix) {
        String query = "SELECT Chunks FROM Clans WHERE ClanPrefix = ?";
        try (Connection connection = MySQL.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, clanPrefix);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("Chunks");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void addChunkCount(String clanPrefix, int amount) {
        String query = "UPDATE Clans SET Chunks = Chunks + ? WHERE ClanPrefix = ?";
        try (Connection connection = MySQL.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, amount);
            statement.setString(2, clanPrefix);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeChunkCount(String clanPrefix, int amount) {
        String query = "UPDATE Clans SET Chunks = GREATEST(Chunks - ?, 0) WHERE ClanPrefix = ?";
        try (Connection connection = MySQL.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, amount);
            statement.setString(2, clanPrefix);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setChunks(String clanPrefix, int amount) {
        String query = "UPDATE Clans SET Chunks = ? WHERE ClanPrefix = ?";
        try (Connection connection = MySQL.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, amount);
            statement.setString(2, clanPrefix);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getChunksForNextLevel(int currentLevel) {
        return levelChunkMap.getOrDefault(currentLevel + 1, 0);
    }

    private static int getClaimedChunkCount(String clanPrefix) {
        String query = "SELECT COUNT(*) FROM ClaimedChunks WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = MySQL.getConnection().prepareStatement(query)) {
            stmt.setString(1, clanPrefix);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


}

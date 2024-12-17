package dev.subscripted.eloriseClans.manager;

import dev.subscripted.eloriseClans.Main;
import dev.subscripted.eloriseClans.database.MySQL;
import dev.subscripted.eloriseClans.utils.ChunkCache;
import dev.subscripted.eloriseClans.utils.ClanChunk;
import dev.subscripted.eloriseClans.utils.UUIDFetcher;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

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


    @SneakyThrows
    public void createClan(UUID owner, String clanPrefix, String clanName) throws SQLException, ExecutionException, InterruptedException {
        String createClanQuery = "INSERT INTO Clans (ClanPrefix, ClanName, ClanLevel) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(createClanQuery)) {
            stmt.setString(1, clanPrefix);
            stmt.setString(2, clanName);
            stmt.setInt(3, 1);
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

        removeMembersFromClan(clanPrefix);
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
    }

    @SneakyThrows
    public void removeMemberFromClan(String clanPrefix, UUID memberUUID) throws SQLException, ExecutionException, InterruptedException {
        String removeMemberQuery = "DELETE FROM ClanMembers WHERE ClanPrefix = ? AND MemberUUID = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(removeMemberQuery)) {
            stmt.setString(1, clanPrefix);
            stmt.setString(2, memberUUID.toString());
            stmt.executeUpdate();
        }
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
        String getClanQuery = "SELECT Clans.ClanPrefix, Clans.ClanName, Clans.OwnerUUID " +
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
        int currentLevel = getClanLevel(clanPrefix);
        int nextLevel = currentLevel + 1;
        String updateLevelQuery = "UPDATE Clans SET ClanLevel = ? WHERE ClanPrefix = ?";
        try (PreparedStatement stmt = mySQL.getConnection().prepareStatement(updateLevelQuery)) {
            stmt.setInt(1, nextLevel);
            stmt.setString(2, clanPrefix);
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

    public static boolean claimChunk(Player player, String clanPrefix) {
        Chunk chunk = player.getLocation().getChunk();
        String world = chunk.getWorld().getName();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        try (Connection connection = MySQL.getConnection()) {
            // Überprüfen, ob der Chunk bereits geclaimt ist
            if (ChunkCache.isChunkClaimed(world, chunkX, chunkZ)) {
                // Besitzer des Chunks ermitteln
                ClanChunk existingClaim = ChunkCache.getClaimedChunk(world, chunkX, chunkZ);
                if (existingClaim != null) {
                    if (existingClaim.getClanPrefix().equals(clanPrefix)) {
                        // Spieler versucht, einen Chunk zu claimen, der bereits seinem Clan gehört
                        player.sendMessage("Dieser Chunk gehört bereits deinem Clan!");
                    } else {
                        // Spieler versucht, einen Chunk zu claimen, der einem anderen Clan gehört
                        player.sendMessage("Dieser Chunk gehört bereits dem Clan: " + existingClaim.getClanPrefix());
                    }
                } else {
                    // Fallback, falls der Claim im Cache fehlt
                    player.sendMessage("Dieser Chunk wurde bereits geclaimt!");
                }
                return false;
            }

            // Chunk claimen
            PreparedStatement claimStatement = connection.prepareStatement(
                    "INSERT INTO ClaimedChunks (ClanPrefix, World, ChunkX, ChunkZ) VALUES (?, ?, ?, ?)"
            );
            claimStatement.setString(1, clanPrefix);
            claimStatement.setString(2, world);
            claimStatement.setInt(3, chunkX);
            claimStatement.setInt(4, chunkZ);

            claimStatement.executeUpdate();

            // Cache aktualisieren
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


}






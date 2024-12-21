package dev.subscripted.eloriseClans.events;

import dev.subscripted.eloriseClans.manager.ClanManager;
import dev.subscripted.eloriseClans.cache.ChunkCache;
import dev.subscripted.eloriseClans.utils.ClanChunk;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.SQLException;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ClanChunkInteract implements Listener {

    final ClanManager manager;

    private boolean isActionAllowed(Player player, ClanChunk claimedChunk) throws SQLException {
        String playerClan = manager.getClanPrefix(player.getUniqueId());


        if (playerClan == null) {
            player.sendMessage("§cDu gehörst keinem Clan an und kannst hier nichts machen! Dieser Chunk gehört dem Clan: §e" + claimedChunk.getClanPrefix());
            return false;
        }

        if (!player.hasPermission("clans.claim.bypass")
                && !playerClan.equals(claimedChunk.getClanPrefix())
                && !manager.isMemberOfClan(player.getUniqueId(), claimedChunk.getClanPrefix())) {
            player.sendMessage("§cDu hast keine Berechtigung, hier etwas zu machen! Dieser Chunk gehört dem Clan: §e" + claimedChunk.getClanPrefix());
            return false;
        }

        return true;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) throws SQLException {
        Player player = event.getPlayer();

        String world = event.getBlock().getWorld().getName();
        int chunkX = event.getBlock().getChunk().getX();
        int chunkZ = event.getBlock().getChunk().getZ();

        ClanChunk claimedChunk = ChunkCache.getClaimedChunk(world, chunkX, chunkZ);
        if (claimedChunk == null) {
            return;
        }

        if (!isActionAllowed(player, claimedChunk)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) throws SQLException {
        Player player = event.getPlayer();
        String world = event.getBlock().getWorld().getName();
        int chunkX = event.getBlock().getChunk().getX();
        int chunkZ = event.getBlock().getChunk().getZ();

        ClanChunk claimedChunk = ChunkCache.getClaimedChunk(world, chunkX, chunkZ);
        if (claimedChunk == null) {
            return;
        }

        if (!isActionAllowed(player, claimedChunk)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) throws SQLException {
        Player player = event.getPlayer();

        // Prüfen, ob ein Block angeklickt wurde
        if (event.getClickedBlock() == null) {
            return;
        }

        // Chunk-Informationen abrufen
        String world = player.getWorld().getName();
        int chunkX = event.getClickedBlock().getChunk().getX();
        int chunkZ = event.getClickedBlock().getChunk().getZ();

        // Prüfen, ob der Chunk geclaimed ist
        ClanChunk claimedChunk = ChunkCache.getClaimedChunk(world, chunkX, chunkZ);
        if (claimedChunk == null) {
            return; // Nicht geclaimter Chunk -> Spieler kann interagieren
        }

        // Prüfen, ob der Spieler berechtigt ist
        if (!isActionAllowed(player, claimedChunk)) {
            event.setCancelled(true);
        }
    }
}

package dev.subscripted.eloriseClans.events;

import dev.subscripted.eloriseClans.manager.ClanManager;
import dev.subscripted.eloriseClans.utils.ChunkCache;
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

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ClanChunkInteract implements Listener {

    final ClanManager manager;


    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String playerClan = manager.getClanPrefix(player.getUniqueId());

        String world = event.getBlock().getWorld().getName();
        int chunkX = event.getBlock().getChunk().getX();
        int chunkZ = event.getBlock().getChunk().getZ();

        ClanChunk claimedChunk = ChunkCache.getClaimedChunk(world, chunkX, chunkZ);
        if (claimedChunk == null) {
            return;
        }

        // Spieler darf nicht abbauen, wenn er keine Berechtigung hat UND nicht Mitglied des Clans ist
        if (!(player.hasPermission("clans.claim.bypass") || playerClan.equals(claimedChunk.getClanPrefix()))) {
            event.setCancelled(true);
            player.sendMessage("§cDu kannst hier nichts abbauen! Dieser Chunk gehört dem Clan: §e" + claimedChunk.getClanPrefix());
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String playerClan = manager.getClanPrefix(player.getUniqueId());

        String world = event.getBlock().getWorld().getName();
        int chunkX = event.getBlock().getChunk().getX();
        int chunkZ = event.getBlock().getChunk().getZ();

        ClanChunk claimedChunk = ChunkCache.getClaimedChunk(world, chunkX, chunkZ);
        if (claimedChunk == null) {
            return;
        }

        // Spieler darf nicht platzieren, wenn er keine Berechtigung hat UND nicht Mitglied des Clans ist
        if (!(player.hasPermission("clans.claim.bypass") || playerClan.equals(claimedChunk.getClanPrefix()))) {
            event.setCancelled(true);
            player.sendMessage("§cDu kannst hier nichts platzieren! Dieser Chunk gehört dem Clan: §e" + claimedChunk.getClanPrefix());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getClickedBlock() == null) {
            return;
        }

        String playerClan = manager.getClanPrefix(player.getUniqueId());
        String world = player.getWorld().getName();

        int chunkX = event.getClickedBlock().getChunk().getX();
        int chunkZ = event.getClickedBlock().getChunk().getZ();

        ClanChunk claimedChunk = ChunkCache.getClaimedChunk(world, chunkX, chunkZ);
        if (claimedChunk == null) {
            return;
        }

        // Spieler darf nicht interagieren, wenn er keine Berechtigung hat UND nicht Mitglied des Clans ist
        if (!(player.hasPermission("clans.claim.bypass") || playerClan.equals(claimedChunk.getClanPrefix()))) {
            event.setCancelled(true);
            player.sendMessage("§cDu kannst hier nicht interagieren! Dieser Chunk gehört dem Clan: §e" + claimedChunk.getClanPrefix());
        }
    }
}

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

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class BlockBreak implements Listener {

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

        if (playerClan == null || !playerClan.equals(claimedChunk.getClanPrefix())) {
            event.setCancelled(true);
            player.sendMessage("§cDu kannst hier nichts abbauen! Dieser Chunk gehört dem Clan: §e" + claimedChunk.getClanPrefix());
        }
    }
}

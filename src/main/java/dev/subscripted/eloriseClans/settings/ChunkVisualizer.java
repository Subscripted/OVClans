package dev.subscripted.eloriseClans.settings;

import dev.subscripted.eloriseClans.utils.ClanChunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.Particle.DustTransition;

public class ChunkVisualizer {

    public static void showChunkBorder(Player player, ClanChunk clanChunk) {
        Location location = player.getLocation();
        String worldName = location.getWorld().getName();

        if (!worldName.equals(clanChunk.getWorld())) {
            return;
        }

        int chunkX = clanChunk.getChunkX();
        int chunkZ = clanChunk.getChunkZ();

        int startX = chunkX * 16;
        int startZ = chunkZ * 16;
        int endX = startX + 15;
        int endZ = startZ + 15;
        int y = player.getLocation().getBlockY();

        DustTransition redstoneParticle = new DustTransition(
                Color.RED,
                Color.ORANGE,
                1.5F
        );

        for (int x = startX; x <= endX; x++) {
            spawnRedstoneParticle(player, new Location(player.getWorld(), x, y, startZ), redstoneParticle); // Obere Kante
            spawnRedstoneParticle(player, new Location(player.getWorld(), x, y, endZ), redstoneParticle);   // Untere Kante
        }
        for (int z = startZ; z <= endZ; z++) {
            spawnRedstoneParticle(player, new Location(player.getWorld(), startX, y, z), redstoneParticle); // Linke Kante
            spawnRedstoneParticle(player, new Location(player.getWorld(), endX, y, z), redstoneParticle);   // Rechte Kante
        }
    }

    private static void spawnRedstoneParticle(Player player, Location location, DustTransition redstoneParticle) {
        player.spawnParticle(Particle.DUST_COLOR_TRANSITION, location, 1, redstoneParticle);
    }
}

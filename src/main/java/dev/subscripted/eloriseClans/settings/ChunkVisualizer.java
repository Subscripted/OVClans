package dev.subscripted.eloriseClans.settings;

import dev.subscripted.eloriseClans.utils.ClanChunk;
import dev.subscripted.eloriseClans.utils.ChunkCache;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.Particle.DustTransition;

import java.util.HashSet;
import java.util.Set;

public class ChunkVisualizer {

    public static void showChunkBorder(Player player, ClanChunk clanChunk) {
        // Prüfe, ob der Chunk in der gleichen Welt wie der Spieler liegt
        Location location = player.getLocation();
        String worldName = location.getWorld().getName();
        if (!worldName.equals(clanChunk.getWorld())) {
            return;
        }

        // Finde alle zusammenhängenden Chunks, die den gleichen ClanPrefix haben
        Set<ClanChunk> connectedChunks = findConnectedChunks(clanChunk);

        // Berechne die Grenzen der zusammenhängenden Region
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (ClanChunk chunk : connectedChunks) {
            minX = Math.min(minX, chunk.getChunkX());
            maxX = Math.max(maxX, chunk.getChunkX());
            minZ = Math.min(minZ, chunk.getChunkZ());
            maxZ = Math.max(maxZ, chunk.getChunkZ());
        }

        // Zeichne die Partikelgrenzen für die gesamte Region
        DustTransition redstoneParticle = new DustTransition(
                Color.NAVY,
                Color.PURPLE,
                1.5F
        );

        drawRegion(player, minX, maxX, minZ, maxZ, redstoneParticle);
    }

    private static Set<ClanChunk> findConnectedChunks(ClanChunk startChunk) {
        // Lade alle Chunks der Welt aus dem Cache
        Set<ClanChunk> allChunks = ChunkCache.claimedChunks.getOrDefault(startChunk.getWorld(), new HashSet<>());

        // Finde alle verbundenen Chunks rekursiv
        Set<ClanChunk> connectedChunks = new HashSet<>();
        Set<ClanChunk> toProcess = new HashSet<>();
        toProcess.add(startChunk);

        while (!toProcess.isEmpty()) {
            ClanChunk current = toProcess.iterator().next();
            toProcess.remove(current);
            connectedChunks.add(current);

            for (ClanChunk chunk : allChunks) {
                if (!connectedChunks.contains(chunk) && isNeighbor(current, chunk) &&
                        current.getClanPrefix().equals(chunk.getClanPrefix())) {
                    toProcess.add(chunk);
                }
            }
        }

        return connectedChunks;
    }

    private static boolean isNeighbor(ClanChunk a, ClanChunk b) {
        // Prüft, ob zwei Chunks nebeneinander liegen
        return (Math.abs(a.getChunkX() - b.getChunkX()) <= 1 && a.getChunkZ() == b.getChunkZ()) ||
                (Math.abs(a.getChunkZ() - b.getChunkZ()) <= 1 && a.getChunkX() == b.getChunkX());
    }

    private static void drawRegion(Player player, int minX, int maxX, int minZ, int maxZ, DustTransition particle) {
        int startX = minX * 16;
        int endX = (maxX + 1) * 16;
        int startZ = minZ * 16;
        int endZ = (maxZ + 1) * 16;
        int y = player.getLocation().getBlockY();

        // Zeichne die Partikel an den Grenzen der Region
        for (int x = startX; x <= endX; x++) {
            spawnRedstoneParticle(player, new Location(player.getWorld(), x, y, startZ), particle); // Obere Kante
            spawnRedstoneParticle(player, new Location(player.getWorld(), x, y, endZ), particle);   // Untere Kante
        }
        for (int z = startZ; z <= endZ; z++) {
            spawnRedstoneParticle(player, new Location(player.getWorld(), startX, y, z), particle); // Linke Kante
            spawnRedstoneParticle(player, new Location(player.getWorld(), endX, y, z), particle);   // Rechte Kante
        }
    }

    private static void spawnRedstoneParticle(Player player, Location location, DustTransition particle) {
        player.spawnParticle(Particle.DUST_COLOR_TRANSITION, location, 1, particle);
    }
}

package dev.subscripted.eloriseClans.settings;

import dev.subscripted.eloriseClans.utils.ClanChunk;
import dev.subscripted.eloriseClans.cache.ChunkCache;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.entity.Player;

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

        // Partikeldefinition
        DustTransition redstoneParticle = new DustTransition(
                Color.NAVY,
                Color.PURPLE,
                1.5F
        );

        // Zeichne die verschmelzenden Grenzen für alle verbundenen Chunks
        drawConnectedChunkBorders(player, connectedChunks, redstoneParticle);
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
        return (Math.abs(a.getChunkX() - b.getChunkX()) == 1 && a.getChunkZ() == b.getChunkZ()) ||
                (Math.abs(a.getChunkZ() - b.getChunkZ()) == 1 && a.getChunkX() == b.getChunkX());
    }

    private static void drawConnectedChunkBorders(Player player, Set<ClanChunk> connectedChunks, DustTransition particle) {
        int y = player.getLocation().getBlockY();

        for (ClanChunk chunk : connectedChunks) {
            int startX = chunk.getChunkX() * 16;
            int endX = (chunk.getChunkX() + 1) * 16;
            int startZ = chunk.getChunkZ() * 16;
            int endZ = (chunk.getChunkZ() + 1) * 16;

            // Zeichne nur die äußeren Ränder (prüfe Nachbarn)
            if (!isChunkNeighborClaimed(chunk, connectedChunks, -1, 0)) { // Linker Rand
                for (int z = startZ; z <= endZ; z++) {
                    spawnRedstoneParticle(player, new Location(player.getWorld(), startX, y, z), particle);
                }
            }
            if (!isChunkNeighborClaimed(chunk, connectedChunks, 1, 0)) { // Rechter Rand
                for (int z = startZ; z <= endZ; z++) {
                    spawnRedstoneParticle(player, new Location(player.getWorld(), endX, y, z), particle);
                }
            }
            if (!isChunkNeighborClaimed(chunk, connectedChunks, 0, -1)) { // Oberer Rand
                for (int x = startX; x <= endX; x++) {
                    spawnRedstoneParticle(player, new Location(player.getWorld(), x, y, startZ), particle);
                }
            }
            if (!isChunkNeighborClaimed(chunk, connectedChunks, 0, 1)) { // Unterer Rand
                for (int x = startX; x <= endX; x++) {
                    spawnRedstoneParticle(player, new Location(player.getWorld(), x, y, endZ), particle);
                }
            }
        }
    }

    private static boolean isChunkNeighborClaimed(ClanChunk chunk, Set<ClanChunk> connectedChunks, int offsetX, int offsetZ) {
        int neighborX = chunk.getChunkX() + offsetX;
        int neighborZ = chunk.getChunkZ() + offsetZ;
        return connectedChunks.stream()
                .anyMatch(c -> c.getChunkX() == neighborX && c.getChunkZ() == neighborZ);
    }

    private static void spawnRedstoneParticle(Player player, Location location, DustTransition particle) {
        player.spawnParticle(Particle.DUST_COLOR_TRANSITION, location, 1, particle);
    }
}

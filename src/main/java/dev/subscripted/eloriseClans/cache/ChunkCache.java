package dev.subscripted.eloriseClans.cache;

import dev.subscripted.eloriseClans.database.MySQL;
import dev.subscripted.eloriseClans.utils.ClanChunk;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChunkCache {

    public ChunkCache() {
        loadChunks();
        System.out.printf("Chunks geladen");
    }

   public static final Map<String, Set<ClanChunk>> claimedChunks = new HashMap<>();

    @SneakyThrows
    public void loadChunks() {
        claimedChunks.clear();

        try (Connection connection = MySQL.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT World, ChunkX, ChunkZ, ClanPrefix FROM ClaimedChunks");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String world = resultSet.getString("World");
                int chunkX = resultSet.getInt("ChunkX");
                int chunkZ = resultSet.getInt("ChunkZ");
                String clanPrefix = resultSet.getString("ClanPrefix");

                ClanChunk clanChunk = new ClanChunk(world, chunkX, chunkZ, clanPrefix);
                claimedChunks.computeIfAbsent(world, k -> new HashSet<>()).add(clanChunk);
            }
        }
    }

    public static boolean isChunkClaimed(String world, int chunkX, int chunkZ) {
        Set<ClanChunk> chunks = claimedChunks.get(world);
        if (chunks == null) {
            return false;
        }
        return chunks.stream().anyMatch(chunk ->
                chunk.getChunkX()
                        == chunkX
                        &&
                        chunk.getChunkZ()
                                == chunkZ);
    }

    public static void addChunk(ClanChunk clanChunk) {
        claimedChunks.computeIfAbsent(clanChunk.getWorld(), k -> new HashSet<>()).add(clanChunk);
    }

    public static void removeChunk(String world, int chunkX, int chunkZ) {
        Set<ClanChunk> chunks = claimedChunks.get(world);
        if (chunks != null) {
            chunks.removeIf(chunk -> chunk.getChunkX() == chunkX && chunk.getChunkZ() == chunkZ);

            if (chunks.isEmpty()) {
                claimedChunks.remove(world);
            }
        }
    }

    public static ClanChunk getClaimedChunk(String world, int chunkX, int chunkZ) {
        Set<ClanChunk> chunks = claimedChunks.get(world);
        if (chunks == null) {
            return null;
        }

        // Suche den Chunk mit den passenden Koordinaten
        return chunks.stream()
                .filter(chunk -> chunk.getChunkX() == chunkX && chunk.getChunkZ() == chunkZ)
                .findFirst()
                .orElse(null);
    }

    public static Set<ClanChunk> getAllChunks() {
        Set<ClanChunk> allChunks = new HashSet<>();
        for (Set<ClanChunk> chunks : claimedChunks.values()) {
            allChunks.addAll(chunks);
        }
        return allChunks;
    }

    private static Set<ClanChunk> getAllChunksForWorld(String worldName) {
        return ChunkCache.claimedChunks.getOrDefault(worldName, new HashSet<>());
    }

}

package dev.subscripted.eloriseClans.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LastJoinedCache {

    private static final Map<UUID, String> lastJoinedCache = new HashMap<>();

    public static void initializeCache(Map<UUID, String> lastJoined) {
        lastJoinedCache.putAll(lastJoined);
    }

    public static String getLastJoined(UUID uuid) {
        return lastJoinedCache.getOrDefault(uuid, "N/A");
    }

    public static void updateCache(UUID uuid, String lastJoined) {
        lastJoinedCache.put(uuid, lastJoined);
    }
}

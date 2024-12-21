package dev.subscripted.eloriseClans.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RankCache {

    private static final Map<UUID, String> rankCache = new HashMap<>();

    /**
     * Initialisiert den Cache mit den Rängen der Spieler.
     *
     * @param ranks Map von UUID zu Rang.
     */
    public static void initializeCache(Map<UUID, String> ranks) {
        rankCache.putAll(ranks);
    }

    /**
     * Holt den Rang eines Spielers aus dem Cache.
     *
     * @param uuid Die UUID des Spielers.
     * @return Der Rang des Spielers oder "Unbekannt", falls nicht gefunden.
     */
    public static String getRank(UUID uuid) {
        return rankCache.getOrDefault(uuid, "Unbekannt");
    }

    /**
     * Aktualisiert den Rang eines Spielers im Cache.
     *
     * @param uuid Die UUID des Spielers.
     * @param newRank Der neue Rang des Spielers.
     */
    public static void updateCache(UUID uuid, String newRank) {
        rankCache.put(uuid, newRank);
    }

    /**
     * Entfernt einen Spieler aus dem Cache, falls er den Clan verlässt.
     *
     * @param uuid Die UUID des Spielers.
     */
    public static void removeFromCache(UUID uuid) {
        rankCache.remove(uuid);
    }
}

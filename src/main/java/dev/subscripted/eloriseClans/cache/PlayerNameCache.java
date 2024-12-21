package dev.subscripted.eloriseClans.cache;

import dev.subscripted.eloriseClans.utils.UUIDFetcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerNameCache {

    private static final Map<UUID, String> playerNameCache = new HashMap<>();

    /**
     * Initialisiert den Cache, indem Namen mithilfe des `UUIDFetcher` geladen werden.
     *
     * @param uuids Eine Liste von UUIDs der Spieler.
     */
    public static void initializeCache(Iterable<UUID> uuids) {
        for (UUID uuid : uuids) {
            String playerName = fetchPlayerName(uuid);
            if (playerName != null) {
                playerNameCache.put(uuid, playerName);
            }
        }
    }

    /**
     * Holt den Spielernamen aus dem Cache oder lädt ihn mit dem `UUIDFetcher`.
     *
     * @param uuid Die UUID des Spielers.
     * @return Der Spielername oder "Unknown", falls der Abruf fehlschlägt.
     */
    public static String getPlayerName(UUID uuid) {
        return playerNameCache.computeIfAbsent(uuid, PlayerNameCache::fetchPlayerName);
    }

    /**
     * Aktualisiert oder fügt einen neuen Eintrag im Cache hinzu.
     *
     * @param uuid  Die UUID des Spielers.
     * @param name  Der Spielername.
     */
    public static void updateCache(UUID uuid, String name) {
        playerNameCache.put(uuid, name);
    }

    /**
     * Holt den Spielernamen mithilfe des `UUIDFetcher`.
     *
     * @param uuid Die UUID des Spielers.
     * @return Der Spielername oder "Unknown", falls nicht gefunden.
     */
    private static String fetchPlayerName(UUID uuid) {
        try {
            return UUIDFetcher.getName(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}

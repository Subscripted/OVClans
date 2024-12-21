package dev.subscripted.eloriseClans.cache;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SkullTextureCache {

    private static final Map<UUID, String> skullTextureCache = new HashMap<>();

    /**
     * Initialisiert den Cache mit den SkullTextures für die Spieler.
     *
     * @param players Map von UUID zu Rängen oder Clans.
     */
    public static void initializeCache(Map<UUID, String> players) {
        for (UUID uuid : players.keySet()) {
            String texture = fetchSkullTexture(uuid);
            if (texture != null) {
                skullTextureCache.put(uuid, texture);
            }
        }
    }

    /**
     * Holt die SkullTexture aus dem Cache.
     *
     * @param uuid Die UUID des Spielers.
     * @return Die SkullTexture (Base64-String) oder null, falls nicht gefunden.
     */
    public static String getSkullTexture(UUID uuid) {
        return skullTextureCache.get(uuid);
    }

    /**
     * Holt die SkullTexture von der Mojang-API.
     *
     * @param uuid Die UUID des Spielers.
     * @return Die SkullTexture (Base64-String).
     */
    private static String fetchSkullTexture(UUID uuid) {
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", ""));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().reduce("", String::concat);
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                JsonArray properties = json.getAsJsonArray("properties");
                if (properties != null && properties.size() > 0) {
                    return properties.get(0).getAsJsonObject().get("value").getAsString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateCache(UUID uuid) {
        if (!skullTextureCache.containsKey(uuid)) {
            // Abrufen der SkullTexture für die neue UUID
            String texture = fetchSkullTexture(uuid);
            if (texture != null) {
                skullTextureCache.put(uuid, texture);
                Bukkit.getLogger().info("SkullTexture für UUID " + uuid + " wurde erfolgreich zum Cache hinzugefügt.");
            } else {
                Bukkit.getLogger().warning("SkullTexture für UUID " + uuid + " konnte nicht abgerufen werden.");
            }
        } else {
            Bukkit.getLogger().info("UUID " + uuid + " ist bereits im Cache vorhanden.");
        }
    }

    public static void updateCacheForClan(List<UUID> members) {
        for (UUID uuid : members) {
            updateCache(uuid);
        }
    }
}

package dev.subscripted.eloriseClans.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShowClaimsManager {

    private static final Map<UUID, Boolean> showClaimsMap = new HashMap<>();

    public static boolean isShowingClaims(UUID playerId) {
        return showClaimsMap.getOrDefault(playerId, false);
    }

    public static void toggleShowClaims(UUID playerId) {
        boolean currentStatus = isShowingClaims(playerId);
        showClaimsMap.put(playerId, !currentStatus);
    }

    public static void setShowClaims(UUID playerId, boolean status) {
        showClaimsMap.put(playerId, status);
    }
}

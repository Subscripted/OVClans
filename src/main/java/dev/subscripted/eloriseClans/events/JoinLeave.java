package dev.subscripted.eloriseClans.events;

import dev.subscripted.eloriseClans.manager.ClanManager;
import dev.subscripted.eloriseClans.utils.SmartConfig;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JoinLeave implements Listener {

    final ClanManager clanManager;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        SmartConfig config = new SmartConfig("test.yml");
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String clanPrefix = clanManager.getClanPrefix(playerUUID);
        config.setString("test.t", playerUUID.toString());
        clanManager.saveLastSeen(playerUUID, clanPrefix);
    }
}

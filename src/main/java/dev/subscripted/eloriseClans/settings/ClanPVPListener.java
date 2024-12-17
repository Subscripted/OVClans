// ClanPVPListener.java
package dev.subscripted.eloriseClans.settings;


import dev.subscripted.eloriseClans.manager.ClanManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;


@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ClanPVPListener implements Listener {

     final ClanManager clanManager;

    @EventHandler
    public void onPlayerDamageByPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player damaged = (Player) event.getEntity();
            try {
                String damagerClan = clanManager.getMemberClan(damager.getUniqueId());
                String damagedClan = clanManager.getMemberClan(damaged.getUniqueId());

                if (damagerClan != null && damagerClan.equals(damagedClan)) {
                    if (!clanManager.isClanPVPEnabled(damagerClan)) {
                        event.setCancelled(true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

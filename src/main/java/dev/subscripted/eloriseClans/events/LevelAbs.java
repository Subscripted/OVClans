package dev.subscripted.eloriseClans.events;

import dev.subscripted.eloriseClans.manager.ClanManager;
import dev.subscripted.eloriseClans.utils.ClanLevelModifier;
import dev.subscripted.eloriseClans.utils.SoundLibrary;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LevelAbs implements Listener {

    final SoundLibrary library;
    final ClanManager clanManager;


    @SneakyThrows
    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {

        Entity damager = event.getDamager();
        Entity victim = event.getEntity();
        if (damager instanceof Player) {
            Player attacker = (Player) damager;
            int clanLevel = getClanLevel(attacker);

            double damageMultiplier = ClanLevelModifier.getDamageMultiplier(clanLevel);

            double originalDamage = event.getDamage();
            double increasedDamage = originalDamage * damageMultiplier;
            UUID uuid = damager.getUniqueId();
            String clanPrefix = clanManager.getClanPrefix(uuid);
            if (clanManager.isMemberOfClan(damager.getUniqueId(), clanPrefix)) {
                event.setDamage(increasedDamage);
                System.out.println(event.getDamage());
            }
        }
    }

    @SneakyThrows
    public int getClanLevel(Player p) {
        UUID uuid = p.getUniqueId();
        String clanPrefix = clanManager.getClanPrefix(uuid);
        return clanManager.getClanLevel(clanPrefix);
    }
}



package dev.subscripted.eloriseClans.utils;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClanInviteManager {

    final Plugin plugin; // Dein Plugin-Instance
    final Map<UUID, BukkitRunnable> inviteTasks = new HashMap<>();

    public ClanInviteManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Sendet einem Spieler eine Einladung und setzt eine Metadata.
     * @param inviter Der Spieler, der die Einladung sendet.
     * @param invitee Der Spieler, der eingeladen wird.
     */
    public void invitePlayer(Player inviter, Player invitee) {
        if (invitee.hasMetadata("clan_invite")) {
            inviter.sendMessage("Dieser Spieler hat bereits eine Einladung.");
            return;
        }

        invitee.setMetadata("clan_invite", new FixedMetadataValue(plugin, inviter.getUniqueId().toString()));

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (invitee.isOnline() && invitee.hasMetadata("clan_invite")) {
                    invitee.removeMetadata("clan_invite", plugin);
                    invitee.sendMessage("Deine Claneinladung ist abgelaufen.");
                }
                inviteTasks.remove(invitee.getUniqueId());
            }
        };
        task.runTaskLater(plugin, 20 * 30); // 30 Sekunden Verzögerung
        inviteTasks.put(invitee.getUniqueId(), task);

        inviter.sendMessage("Du hast " + invitee.getName() + " eingeladen.");
        invitee.sendMessage("Du wurdest von " + inviter.getName() + " in einen Clan eingeladen. Die Einladung läuft in 30 Sekunden ab.");
    }

    /**
     * Entfernt die Einladung eines Spielers, z.B. wenn er den Server verlässt.
     * @param player Der Spieler, dessen Einladung entfernt werden soll.
     */
    public void removeInvite(Player player) {
        if (player.hasMetadata("clan_invite")) {
            player.removeMetadata("clan_invite", plugin);
            player.sendMessage("Deine Claneinladung wurde entfernt.");
        }
        if (inviteTasks.containsKey(player.getUniqueId())) {
            inviteTasks.get(player.getUniqueId()).cancel();
            inviteTasks.remove(player.getUniqueId());
        }
    }

    /**
     * Prüft, ob ein Spieler eine Einladung hat.
     * @param player Der zu prüfende Spieler.
     * @return True, wenn der Spieler eine Einladung hat, ansonsten False.
     */
    public boolean hasInvite(Player player) {
        return player.hasMetadata("clan_invite");
    }
}

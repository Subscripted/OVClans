package dev.subscripted.eloriseClans.commands;

import dev.subscripted.eloriseClans.Main;
import dev.subscripted.eloriseClans.database.connections.Coins;
import dev.subscripted.eloriseClans.gui.ClanMenus;
import dev.subscripted.eloriseClans.manager.ClanManager;
import dev.subscripted.eloriseClans.utils.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ClanCommand implements CommandExecutor, TabCompleter {

    final ClanManager manager;
    final ClanMenus menus;
    final SoundLibrary library;

    static final Pattern VALID_CLAN_PREFIX_PATTERN = Pattern.compile("^[a-zA-Z&]*$");
    static final Pattern VALID_CLAN_NAME_PATTERN = Pattern.compile("^[a-zA-Z& ]*$");
    final Map<UUID, BukkitRunnable> inviteTasks = new HashMap<>();
    final String clanChatPrefix = "§x§C§5§C§3§7§5§lC§x§C§5§C§3§7§5§ll§x§C§5§C§3§7§5§la§x§C§5§C§3§7§5§ln§x§C§5§C§3§7§5§lc§x§C§5§C§3§7§5§lh§x§C§5§C§3§7§5§la§x§C§5§C§3§7§5§lt §8▪ ";

    @Override
    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }


        Player cSender = (Player) sender;
        UUID cSender_uuid = cSender.getUniqueId();
        Coins coins = Main.getInstance().getCoins();
        int buyprice = 50000;

        if (args.length == 0) {
            if (!manager.isClanMember(cSender_uuid)) {
                sendActionBar(cSender, "§7Verwende einen Unterbefehl wie §e/clan create §c<prefix> <name>");
            } else {
                menus.openClanMenu(cSender);
                library.playLibrarySound(cSender, CustomSound.CLAN_OPEN, 1f, 2f);
            }
            return true;
        }

        String subcommand = args[0].toLowerCase();
        try {
            switch (subcommand) {
                case "create":
                    if (args.length == 3) {
                        String clanPrefix = args[1];
                        String clanName = args[2];

                        String clanNameStripped = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', clanName));

                        if (clanPrefix.length() > 12 || clanNameStripped.length() > 12) {
                            sendActionBar(cSender, "§7Der Clan-Prefix und Name dürfen maximal 12 Zeichen lang sein!");
                            library.playLibrarySound(cSender, CustomSound.NO_PERMISSION, 1f, 1f);
                            return true;
                        }


                        if (manager.clanExists(clanPrefix)) {
                            sendActionBar(cSender, "§7Ein Clan mit dem Prefix §r" + clanPrefix + " §7existiert bereits!");
                            library.playLibrarySound(cSender, CustomSound.NO_PERMISSION, 1f, 2f);
                            return true;
                        }

                        if (manager.isClanNameTaken(clanName)) {
                            sendActionBar(cSender, "§7Ein Clan mit dem Namen §r" + clanName.replace("&", "§") + " §7existiert bereits!");
                            library.playLibrarySound(cSender, CustomSound.NO_PERMISSION, 1f, 2f);
                            return true;
                        }

                        if (manager.isClanMember(cSender_uuid)) {
                            sendActionBar(cSender, "§7Du bist schon Mitglied in einem Clan!");
                            library.playLibrarySound(cSender, CustomSound.NO_PERMISSION, 1f, 2f);
                            return true;
                        }

                        int playerCoins = coins.getCoins(cSender_uuid);
                        if (playerCoins < buyprice) {
                            int mCoins = buyprice - playerCoins;
                            String missingCoins = CoinFormatter.formatCoins(mCoins);
                            sendActionBar(cSender, "§7Du hast nicht genügend Geld, um einen Clan zu erstellen! Du benötigst noch §e" + missingCoins + " €.");
                            library.playLibrarySound(cSender, CustomSound.NO_PERMISSION, 1f, 1f);
                            return true;
                        }

                        manager.createClan(cSender_uuid, clanPrefix, clanName);
                        manager.setMemberRank(cSender_uuid, clanPrefix, "Owner");
                        coins.removeCoins(cSender_uuid, buyprice);
                        sendActionBar(cSender, "§7Du hast erfolgreich deinen Clan gegründet! §8(§7Name: " + ChatColor.translateAlternateColorCodes('&', clanName) + " §8| §7Prefix: §e" + clanPrefix + "§8)");
                        manager.startShowingClaims(cSender, getClanPrefix(cSender));
                        library.playLibrarySound(cSender, CustomSound.SUCCESSFULL, 1f, 1f);
                    } else {
                        sendActionBar(cSender, "§7Benutze §e/clan create §c<prefix> <name>");
                        library.playLibrarySound(cSender, CustomSound.WRONG_USAGE, 1f, 1f);
                    }
                    break;
                case "myclan":
                    String clanInfo = manager.getClan(cSender_uuid);
                    if (clanInfo != null) {
                        sendActionBar(cSender, "§7Du bist in diesem Clan: §e" + ChatColor.translateAlternateColorCodes('&', clanInfo.replace("-", "|")));
                        library.playLibrarySound(cSender, CustomSound.LOADING_FINISHED, 1f, 1f);
                    } else {
                        sendActionBar(cSender, "§7Du bist in keinem Clan!");
                        library.playLibrarySound(cSender, CustomSound.NO_PERMISSION, 1f, 1f);
                    }
                    break;
                case "leave":
                    if (args.length == 2) {
                        String clanPrefix = args[1];
                        String currentClan = manager.getMemberClan(cSender_uuid);
                        String clanName = manager.getClanName(cSender_uuid);

                        if (currentClan != null && currentClan.equals(clanPrefix)) {
                            if (manager.isOwnerOfClan(cSender_uuid, clanPrefix)) {
                                sendActionBar(cSender, "§7Du kannst deinen eigenen Clan nicht verlassen §8- §eTransferiere §7die Führung an ein §eMitglied §7oder §cLösche §7den Clan!");
                                library.playLibrarySound(cSender, CustomSound.NOT_ALLOWED, 1f, 1f);
                            } else {
                                sendActionBar(cSender, "§7Du hast den Clan §e" + (clanName != null ? ChatColor.translateAlternateColorCodes('&', clanName) : "N/A") + " §7verlassen!");
                                library.playLibrarySound(cSender, CustomSound.SUCCESSFULL, 1f, 1f);
                                manager.leaveClan(cSender_uuid);
                            }
                        } else {
                            sendActionBar(cSender, "§7Du bist nicht im Clan - §e" + clanPrefix);
                            library.playLibrarySound(cSender, CustomSound.NOT_ALLOWED, 1f, 1f);
                        }
                    } else {
                        sendActionBar(cSender, "§7Benutze §7/clan leave §c<clanPrefix>");
                        library.playLibrarySound(cSender, CustomSound.WRONG_USAGE, 1f, 1f);
                    }
                    break;
                case "delete":
                    if (args.length == 2) {
                        String clanPrefix = args[1];
                        String currentClan = manager.getMemberClan(cSender_uuid);
                        String clanName = manager.getClanNameByPrefix(clanPrefix); // Clan-Namen anhand Prefix laden

                        // Existiert der Clan überhaupt?
                        if (clanName == null) {
                            sendActionBar(cSender, "§7Der Clan §c" + clanPrefix + " §7existiert nicht!");
                            library.playLibrarySound(cSender, CustomSound.NOT_ALLOWED, 1f, 1f);
                            break;
                        }

                        // Wenn der Spieler der Owner des Clans ist
                        if (manager.isOwnerOfClan(cSender_uuid, clanPrefix)) {
                            sendActionBar(cSender, "§7Du hast deinen Clan §c" + ChatColor.translateAlternateColorCodes('&', clanName) + " §7aufgelöst!");
                            manager.deleteClan(clanPrefix);
                            library.playSoundForAll(CustomSound.WARNING, 1f, 2f);

                            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                                p.sendMessage(" ");
                                p.sendMessage(Main.getInstance().getPrefix() + "§7Der Clan §c" + ChatColor.translateAlternateColorCodes('&', clanName) + " §7wurde aufgelöst!");
                                p.sendMessage(" ");
                            }
                            break;
                        }

                        // Wenn ein Teammitglied den Clan löscht
                        if (cSender.hasPermission("survival.clans.delete")) {
                            sendActionBar(cSender, "§7Du hast den Clan §c" + ChatColor.translateAlternateColorCodes('&', clanName) + " §7gelöscht!");
                            manager.deleteClan(clanPrefix);
                            library.playSoundForAll(CustomSound.WARNING, 1f, 2f);

                            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                                p.sendMessage(" ");
                                p.sendMessage(Main.getInstance().getPrefix() + "§7Der Clan §c" + ChatColor.translateAlternateColorCodes('&', clanName) + " §7wurde von einem §c§lTeammitglied §7aufgelöst!");
                                p.sendMessage(" ");
                            }
                            break;
                        }

                        // Wenn der Spieler nicht berechtigt ist oder kein Mitglied
                        sendActionBar(cSender, "§7Du bist nicht berechtigt, diesen Clan zu löschen oder bist kein Mitglied davon!");
                        library.playLibrarySound(cSender, CustomSound.NOT_ALLOWED, 1f, 1f);
                        break;

                    } else {
                        // Falsche Syntax
                        sendActionBar(cSender, "§7Benutze §7/clan delete §c<clanPrefix>");
                        library.playLibrarySound(cSender, CustomSound.WRONG_USAGE, 1f, 1f);
                        break;
                    }

                case "chat":
                    if (args.length >= 2) {
                        if (manager.isClanMember(cSender_uuid)) {
                            String clanPrefix = manager.getClanPrefix(cSender_uuid);
                            String memberRank = manager.getMemberRank(cSender_uuid, clanPrefix).replace("Owner", "§4Owner").replace("Vize", "§cVize").replace("Ältester", "§6Ältester").replace("Mitglied", "§7Mitglied");
                            if (manager.isMemberOfClan(cSender_uuid, clanPrefix)) {
                                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    if (manager.isMemberOfClan(p.getUniqueId(), clanPrefix)) {
                                        p.sendMessage(clanChatPrefix + memberRank + " §8| §e" + sender.getName() + " §8» §x§C§5§C§3§7§5" + message);
                                    }
                                }
                            }
                        } else {
                            sendActionBar(cSender, "§7Du bist in §ckeinen §7Clan!");
                        }
                    } else {
                        sendActionBar(cSender, "§7Benutze §e/clan chat <message>");
                    }
                    break;

                case "invite":
                    if (args.length == 2) {
                        String targetPlayerName = args[1];
                        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

                        // Prüfen, ob der Spieler im gleichen Clan ist
                        String playerClan = manager.getMemberClan(cSender_uuid);
                        if (playerClan == null) {
                            sendActionBar(cSender, "§cDu bist in keinem Clan und kannst daher niemanden einladen!");
                            library.playLibrarySound(cSender, CustomSound.NOT_ALLOWED, 1f, 1f);
                            return true;
                        }

                        // Prüfen, ob der Zielspieler online ist
                        if (targetPlayer == null || !targetPlayer.isOnline()) {
                            sendActionBar(cSender, "§cDer Spieler §e" + targetPlayerName + " §cist nicht online!");
                            library.playLibrarySound(cSender, CustomSound.NOT_ALLOWED, 1f, 1f);
                            return true;
                        }


                        // Prüfen, ob das Ziel bereits in einem Clan ist
                        if (manager.isClanMember(targetPlayer.getUniqueId())) {
                            sendActionBar(cSender, "§e" + targetPlayerName + " §cist bereits Mitglied eines Clans!");
                            library.playLibrarySound(cSender, CustomSound.NOT_ALLOWED, 1f, 1f);
                            return true;
                        }

                        //Prüfen ob es der owner oder Vize ist
                        if (!manager.getMemberRank(cSender_uuid, getClanPrefix(cSender)).equals("Owner")
                                || !manager.getMemberRank(cSender_uuid, getClanPrefix(cSender)).equals("Vize")) {
                            cSender.sendMessage(Main.getInstance().getPrefix() + "§cDu hast keine Berechtigung Spieler in deinen Clan einzuladen!");
                            library.playLibrarySound(cSender, CustomSound.NO_PERMISSION, 1f, 1f);
                            return true;
                        }


                        if (hasInvite(targetPlayer)) {
                            sendActionBar(cSender, "§e" + targetPlayerName + " §chat bereits eine Einladung!");
                            library.playLibrarySound(cSender, CustomSound.NOT_ALLOWED, 1f, 1f);
                            return true;
                        }

                        invitePlayer(cSender, targetPlayer);
                        sendActionBar(cSender, "§aDu hast §e" + targetPlayerName + " §ain deinen Clan eingeladen!");
                        targetPlayer.sendMessage("§7[Clan Invite] §e" + cSender.getName() + " §7hat dich in seinen Clan eingeladen!");


                        //todo: Beitrits Mechanismus


                        library.playLibrarySound(cSender, CustomSound.SUCCESSFULL, 1f, 1f);
                    } else {
                        sendActionBar(cSender, "§7Benutze §e/clan invite §c<Spieler>");
                        library.playLibrarySound(cSender, CustomSound.WRONG_USAGE, 1f, 1f);
                    }
                    break;


                case "gui":
                    menus.openClanMenu(cSender);
                    library.playLibrarySound(cSender, CustomSound.CLAN_OPEN, 1f, 2f);
                    break;

                case "claim":
                    manager.claimChunk(cSender, getClanPrefix(cSender));

                    break;

                case "delclaim":
                    manager.delClaim(cSender, getClanPrefix(cSender));
                    break;
                default:
                    cSender.sendActionBar("§7Du hast ein §cUngültigen §eSyntax §7genutzt!");
            }
        } catch (SQLException | ExecutionException |
                 InterruptedException e) {
            e.printStackTrace();
            cSender.sendMessage("An error occurred while executing the command.");
        }
        return true;
    }

    @Override
    @Nullable
    @SneakyThrows
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String
            s, @NotNull String[] args) {
        List<String> tab = new ArrayList<>();
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (args.length == 1) {
            if (!manager.isClanMember(playerUUID)) {
                tab.add("create");
            } else if (manager.isClanMember(playerUUID)) {
                tab.add("invite");
                tab.add("myclan");
                tab.add("leave");
                tab.add("chat");
                tab.add("gui");
            }
            if (manager.isClanMember(playerUUID) || player.hasPermission("survival.clans.delete") || manager.isClanOwner(playerUUID)) {
                tab.add("delete");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("delete") && player.hasPermission("survival.clans.delete")) {
                try {
                    tab.addAll(manager.getClans());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (args[0].equalsIgnoreCase("invite")) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!manager.isClanMember(onlinePlayer.getUniqueId())) {
                        tab.add(onlinePlayer.getName());
                    }
                }
            }
        }

        return tab;
    }

    private boolean isValidClanPrefix(String prefix) {
        return VALID_CLAN_PREFIX_PATTERN.matcher(prefix).matches() && !prefix.matches(".*\\d+.*");
    }

    private boolean isValidClanName(String name) {
        return VALID_CLAN_NAME_PATTERN.matcher(name).matches() && !name.matches(".*\\d+.*");
    }

    public void sendActionBar(Player player, String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        player.sendActionBar(Main.getInstance().getPrefix() + message);
    }

    public void invitePlayer(Player inviter, Player invitee) {
        if (invitee.hasMetadata("clan_invite")) {
            inviter.sendMessage("Dieser Spieler hat bereits eine Einladung.");
            return;
        }

        invitee.setMetadata("clan_invite", new FixedMetadataValue(Main.getInstance(), inviter.getUniqueId().toString()));

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (invitee.isOnline() && invitee.hasMetadata("clan_invite")) {
                    invitee.removeMetadata("clan_invite", Main.getInstance());
                    invitee.sendMessage("Deine Claneinladung ist abgelaufen.");
                }
                inviteTasks.remove(invitee.getUniqueId());
            }
        };
        task.runTaskLater(Main.getInstance(), 20 * 30); // 30 Sekunden Verzögerung
        inviteTasks.put(invitee.getUniqueId(), task);

        inviter.sendMessage("Du hast " + invitee.getName() + " eingeladen.");
        invitee.sendMessage("Du wurdest von " + inviter.getName() + " in einen Clan eingeladen. Die Einladung läuft in 30 Sekunden ab.");
    }

    public void removeInvite(Player player) {
        if (player.hasMetadata("clan_invite")) {
            player.removeMetadata("clan_invite", Main.getInstance());
            player.sendMessage("Deine Claneinladung wurde entfernt.");
        }
        if (inviteTasks.containsKey(player.getUniqueId())) {
            inviteTasks.get(player.getUniqueId()).cancel();
            inviteTasks.remove(player.getUniqueId());
        }
    }

    public boolean hasInvite(Player player) {
        return player.hasMetadata("clan_invite");
    }

    private String getClanPrefix(Player player) {
        return manager.getClanPrefix(player.getUniqueId());
    }

}

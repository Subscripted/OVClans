package dev.subscripted.eloriseClans.manager;

import dev.subscripted.eloriseClans.Main;
import dev.subscripted.eloriseClans.database.connections.Coins;
import dev.subscripted.eloriseClans.gui.ClanMenus;
import dev.subscripted.eloriseClans.utils.CoinFormatter;
import dev.subscripted.eloriseClans.utils.CustomSound;
import dev.subscripted.eloriseClans.utils.SoundLibrary;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class BankUIListener implements Listener {

    final SoundLibrary library;
    final ClanManager manager;
    final ClanMenus menus;
    static final int DEPOSIT_SLOT = 11;
    static final int WITHDRAW_SLOT = 15;

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (manager.isAddingMoney(player)) {
            event.setCancelled(true);
            manager.removeFromAddingMoney(player);
            try {
                int amount = Integer.parseInt(message);
                processClanDeposit(player, amount);
            } catch (NumberFormatException e) {
                player.sendMessage(Main.getInstance().getPrefix() + "§cUngültige Zahl, bitte versuche es erneut.");
                library.playLibrarySound(player, CustomSound.NOT_ALLOWED, 1f, 1f);
            }
        } else if (manager.isRemovingMoney(player)) {
            event.setCancelled(true);
            manager.removeFromRemovingMoney(player);
            try {
                int amount = Integer.parseInt(message);
                processClanWithdrawal(player, amount);
            } catch (NumberFormatException e) {
                player.sendMessage(Main.getInstance().getPrefix() + "§cUngültige Zahl, bitte versuche es erneut.");
                library.playLibrarySound(player, CustomSound.NOT_ALLOWED, 1f, 1f);
            }
        }
    }

    @SneakyThrows
    private void processClanDeposit(Player player, int amount) {
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            Coins coins = Main.getInstance().getCoins();
            UUID playerUID = player.getUniqueId();
            String clanPrefix = null;
            try {
                clanPrefix = manager.getClanPrefix(playerUID);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            String coinspayedin = CoinFormatter.formatCoins(amount);

            if (coins.getCoins(playerUID) >= amount) {
                coins.removeCoins(playerUID, amount);
                try {
                    manager.addMoneyToClanBank(clanPrefix, amount);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                String ClanBankCoins = null;
                try {
                    ClanBankCoins = CoinFormatter.formatCoins(manager.getClanEco(clanPrefix));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                player.sendMessage(Main.getInstance().getPrefix() + "§7Du hast erfolgreich §e" + coinspayedin + "€ §7auf dein Bankkonto eingezahlt.");
                player.sendMessage(Main.getInstance().getPrefix() + "§7Deine Clanbank beträgt nun einen Stand von §e" + ClanBankCoins + "€");
                library.playLibrarySound(player, CustomSound.SUCCESSFULL, 1f, 1f);
                menus.openClanBank(player);
                library.playLibrarySound(player, CustomSound.PAGE_TURN, 1f, 1f);
            } else {
                player.sendMessage(Main.getInstance().getPrefix() + "§cDu hast nicht genug Geld, um diesen Betrag einzuzahlen.");
                library.playLibrarySound(player, CustomSound.NOT_ALLOWED, 1f, 1f);
            }
        });
    }

    @SneakyThrows
    private void processClanWithdrawal(Player player, int amount) {
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            Coins coins = Main.getInstance().getCoins();
            UUID playerUID = player.getUniqueId();
            String clanPrefix = null;
            clanPrefix = manager.getClanPrefix(playerUID);

            String coinspayedin = CoinFormatter.formatCoins(amount);

            try {
                if (manager.getClanEco(clanPrefix) >= amount) {
                    try {
                        manager.removeMoneyFromClanBank(clanPrefix, amount);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    coins.addCoins(playerUID, amount);
                    String ClanBankCoins = null;
                    try {
                        ClanBankCoins = CoinFormatter.formatCoins(manager.getClanEco(clanPrefix));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    player.sendMessage(Main.getInstance().getPrefix() + "§7Du hast erfolgreich §e" + coinspayedin + "€ §7von deinem Bankkonto abgehoben.");
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Deine Clanbank beträgt nun einen Stand von §e" + ClanBankCoins + "€");
                    library.playLibrarySound(player, CustomSound.SUCCESSFULL, 1f, 1f);
                    menus.openClanBank(player);
                    library.playLibrarySound(player, CustomSound.PAGE_TURN, 1f, 1f);
                } else {
                    player.sendMessage(Main.getInstance().getPrefix() + "§cDu hast nicht genug Geld auf deinem Clankonto, um diesen Betrag abzuheben.");
                    library.playLibrarySound(player, CustomSound.NOT_ALLOWED, 1f, 1f);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

}

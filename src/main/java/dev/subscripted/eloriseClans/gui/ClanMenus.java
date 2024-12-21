package dev.subscripted.eloriseClans.gui;

import dev.subscripted.eloriseClans.cache.LastJoinedCache;
import dev.subscripted.eloriseClans.cache.PlayerNameCache;
import dev.subscripted.eloriseClans.cache.RankCache;
import dev.subscripted.eloriseClans.cache.SkullTextureCache;
import dev.subscripted.eloriseClans.database.MySQL;
import dev.subscripted.eloriseClans.manager.ClanManager;
import dev.subscripted.eloriseClans.utils.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ClanMenus {

    final MySQL mySQL;
    final ClanManager clanManager;
    private static final Map<Integer, String> CLAN_LEVEL_ICONS = new HashMap<>();
    private static final List<String> RANKS = Arrays.asList("Mitglied", "Ältester", "Vize", "Owner");


    static {

        CLAN_LEVEL_ICONS.put(1, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDBhNDcxZTM3YzUxM2ZkNzVmM2YxYWEzZWUyYzVkNzEwZjYxZGE1YmIxYTJjZTg0NDI2OWEyZTRkYjEyYTAwZCJ9fX0=");
        CLAN_LEVEL_ICONS.put(2, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2JiNzk5YWRmMTIxMDZlMDJhMjJmZDFmMDgzMzM3NTk0Y2JlY2Y3ZDQ4NTdlNDM2NDg1ODk1Yjc5NmFjZjIzMyJ9fX0=");
        CLAN_LEVEL_ICONS.put(3, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTgyZDcyYjU2ZTY4NzliMmJkZjc5NTIyNGRkZmQyODRjMjI1MzQwNWQyMDRmNWJkYzZkZjEwNjVjYmUzY2RmOSJ9fX0=");
        CLAN_LEVEL_ICONS.put(4, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQzMzJjZDgzNWQwNWJmY2EzZjBlZjQ0NWQ4MmNhMjEyY2Q5MTAyNDljYzczNzE2NTNhYjdiMjk3MGQxYzBmMyJ9fX0=");
        CLAN_LEVEL_ICONS.put(5, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzRmNmFhNGVmNjI0Y2YyZDgxNDkyMjJiOGU4MThjZWUzZTBiMDBiMzQxMzY2ZDI1NjBiMzE5ZmE0ZDk4NzA4ZCJ9fX0=");
        CLAN_LEVEL_ICONS.put(6, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjAyYTExNjkzMDlmMDVlZjJmMDYxYjFmYTBmZTIyNWYyOWQ3M2EyNGY4ZjA3Y2NjMmE3MDVkZWVhY2EwNjlkMSJ9fX0=");
    }

    @SneakyThrows
    public void openClanMenu(Player player) {
        String clanName = ChatColor.translateAlternateColorCodes('&', clanManager.getClanName(player.getUniqueId()));
        String clanPrefix = clanManager.getClanPrefix(player.getUniqueId());
        String rang = clanManager.isClanOwner(player.getUniqueId()) ? "§c§lOwner" : "§7§lMitglied";
        UUID uuid = player.getUniqueId();
        Inventory inventory = Bukkit.createInventory(player, 6 * 9, "§x§6§0§6§0§6§0§lClan §8» " + clanName + " §8| §x§6§0§6§0§6§0§lRang §8» " + rang);
        int clanLevel = clanManager.getClanLevel(clanPrefix);
        String levelTexture = CLAN_LEVEL_ICONS.getOrDefault(clanLevel, CLAN_LEVEL_ICONS.get(1));

        ItemBuilder pattern = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).setDisplayName(" ");
        if (clanLevel == 6) {
            pattern.addEnchant(Enchantment.SILK_TOUCH, 1).addItemFlag(ItemFlag.HIDE_ENCHANTS);
        }
        ItemBuilder corners = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ");

        ItemBuilder settings = new ItemBuilder(Material.REPEATER).setDisplayName("§x§F§A§4§7§4§7§lE§x§F§6§4§E§4§E§li§x§F§3§5§5§5§5§ln§x§E§F§5§C§5§C§ls§x§E§C§6§3§6§3§lt§x§E§8§6§A§6§A§le§x§E§5§7§1§7§1§ll§x§E§1§7§8§7§8§ll§x§D§D§7§F§7§F§lu§x§D§A§8§6§8§6§ln§x§D§6§8§D§8§D§lg§x§D§3§9§4§9§4§le§x§C§F§9§B§9§B§ln")
                .addLoreLine("§8» §7Der §cInhaber §7des Clans kann hier den §eClan §7verwalten§8. ")
                .addLoreLine(" ")
                .addLoreLine("§8| §aMitglieder§7, §aClan§7, §aLöschen");

        // Clanbank-Icon
        ItemBuilder bank = new ItemBuilder(Material.PLAYER_HEAD)
                .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWZkMTA4MzgzZGZhNWIwMmU4NjYzNTYwOTU0MTUyMGU0ZTE1ODk1MmQ2OGMxYzhmOGYyMDBlYzdlODg2NDJkIn19fQ==")
                .setDisplayName("§x§E§9§F§A§4§7§lClanbank")
                .addLoreLine("§8» §7Hier kannst du §eGeld §7in die §eClanbank §7einzahlen§8.")
                .addLoreLine("")
                .addLoreLine("§8| §7Das eingezahlte §eGeld §7kann von dem §cClaninhaber §7wieder §aausgezahlt §7werden")
                .addLoreLine("§7oder in die §eWeiterentwicklung §7des §aClanlevels §7gesteckt werden§8.")
                .addLoreLine(" ")
                .addLoreLine("§8| §7Deine Berechtigungen : " + (clanManager.isClanMember(player.getUniqueId()) ? "§aEinzahlen" : "§a§mEinzahlen") + " §8| " + (clanManager.isClanOwner(player.getUniqueId()) ? "§eAuszahlen" : "§e§mAuszahlen"));

        // Mitglieder-Icon
        UUID clanOwnerUUID = clanManager.getClanOwner("Owner", clanPrefix);
        ItemBuilder clanMember = new ItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName("§b§lMitglieder")
                .setSkullTexture(SkullTextureCache.getSkullTexture(clanOwnerUUID));

        // Clan-Level-Icon
        ItemBuilder clanLevelItem = new ItemBuilder(Material.PLAYER_HEAD)
                .setSkullTexture(levelTexture)
                .setDisplayName("§x§6§E§F§A§4§7§lClanlevel")
                .addLoreLine("§8• §eInformationen")
                .addLoreLine("§8| §7Hier kannst du das §aLevel §7deines §eClans §7einsehen.")
                .addLoreLine(" ")
                .addLoreLine("§8| §7Dein §eClan §7hat ein §eLevel §7von §e" + clanLevel + "§8.")
                .addLoreLine("§8| §7Der §cInhaber §7kann das Clanlevel §aweiterentwickeln.")
                .addLoreLine("§8| §7Deine Berechtigungen : " + (clanManager.isClanOwner(player.getUniqueId()) ? "§aZugriff" : "§cKeinen Zugriff"));

        // Menü erstellen
        InventoryAdvancer.makePattern(inventory, pattern);
        InventoryAdvancer.fillCorners(inventory, corners);
        inventory.setItem(14, settings.build());
        inventory.setItem(22, bank.build());
        inventory.setItem(11, clanMember.build());
        inventory.setItem(40, clanLevelItem.build());
        player.openInventory(inventory);
    }


    @SneakyThrows
    public void openClanSettings(Player player) {
        String deleteClanValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==";
        String clanName = ChatColor.translateAlternateColorCodes('&', clanManager.getClanName(player.getUniqueId()));
        String clanPrefix = clanManager.getClanPrefix(player.getUniqueId());
        String rang = clanManager.isClanOwner(player.getUniqueId()) ? "§c§lOwner" : "§7§lMitglied";
        UUID uuid = player.getUniqueId();
        Inventory inventory = Bukkit.createInventory(player, 6 * 9, "§x§6§0§6§0§6§0§lClan §8» " + clanName + " §8| §x§6§0§6§0§6§0§lEinstellungen");
        ItemBuilder corners = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ");
        ItemBuilder fill = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName(" ");
        ItemBuilder deleteclan = new ItemBuilder(Material.PLAYER_HEAD).setSkullTexture(deleteClanValue).setDisplayName("§7Clan §cLöschen");
        ItemBuilder member = new ItemBuilder(Material.PLAYER_HEAD).setSkullOwner(UUIDFetcher.getName(clanManager.getClanOwner("Owner", clanPrefix))).setDisplayName("§b§lMitglieder");
        ItemBuilder pvp = new ItemBuilder(Material.IRON_SWORD).setDisplayName("§c§lPVP")
                .addLoreLine(clanManager.isClanPVPEnabled(clanPrefix) ? "§aAktiv" : "§cAus");

        ItemBuilder claimborders = new ItemBuilder(Material.GLOWSTONE_DUST).setDisplayName("§6§lClaim-Border")
                .addLoreLine(clanManager.isClanBorderShown(clanPrefix) ? "§aAktiv" : "§cAus");

        ItemBuilder clancolor = new ItemBuilder(Material.LOOM).setDisplayName("§x§F§F§0§0§0§0§lC§x§F§F§7§F§0§0§ll§x§F§F§B§F§0§0§la§x§F§F§F§F§0§0§ln§x§0§0§F§F§0§0§lf§x§0§0§0§0§F§F§la§x§2§6§0§0§C§1§lr§x§4§B§0§0§8§2§lb§x§9§4§0§0§D§3§le")
                .addLoreLine("§8» §7Der §cInhaber §7des Clans kann hier einen Clankrieg ")
                .addLoreLine("§8| §7zwischen seinem und dem §eGegnerclan §7verwalten§8.")
                .addLoreLine(" ")
                .addLoreLine("§8| §7Ist der Clankrieg vorbei, werden hier die §eStatistiken §7angezeigt§8.")
                .addLoreLine(" ")
                .addLoreLine("§8| §8§lStatus§8 : §b§lEntwicklung...");

        InventoryAdvancer.fillCorners(inventory, corners);
        InventoryAdvancer.makePattern(inventory, fill);
        inventory.setItem(49, backitem());
        inventory.setItem(15, pvp.build());
        inventory.setItem(14, claimborders.build());
        inventory.setItem(40, deleteclan.build());
        inventory.setItem(42, clancolor.build());
        inventory.setItem(38, member.build());
        setClanSettingsMiddle(inventory, fill);


        player.openInventory(inventory);
    }

    @SneakyThrows
    public void openDeleteMenu(Player player) {
        String deleteClanYes = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDMxMmNhNDYzMmRlZjVmZmFmMmViMGQ5ZDdjYzdiNTVhNTBjNGUzOTIwZDkwMzcyYWFiMTQwNzgxZjVkZmJjNCJ9fX0=";
        String deleteClanNo = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==";
        String clanName = ChatColor.translateAlternateColorCodes('&', clanManager.getClanName(player.getUniqueId()));
        String clanPrefix = clanManager.getClanPrefix(player.getUniqueId());
        String rang = clanManager.isClanOwner(player.getUniqueId()) ? "§c§lOwner" : "§7§lMitglied";
        UUID uuid = player.getUniqueId();
        Inventory inventory = Bukkit.createInventory(player, 3 * 9, "§8Clan Löschen");
        ItemBuilder greystainedglass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" ");
        ItemBuilder explain = new ItemBuilder(Material.PLAYER_HEAD).setSkullTexture(exclamation()).setDisplayName("§6§lErklärung")
                .addLoreLine(" §8• §eInformationen")
                .addLoreLine("§8| §7Hier entscheidest §edu §7ob du deinen §eClan§8.")
                .addLoreLine(" ")
                .addLoreLine("§8» §cLöschen§8.")
                .addLoreLine(" ")
                .addLoreLine("§7 - oder§8.")
                .addLoreLine(" ")
                .addLoreLine("§8» §7nicht §eLöschen §7möchtest§8")
                .addLoreLine(" ")
                .addLoreLine("§8| §eKlicke §7auf das §aGrüne Häckchen§7, zum §elöschen §7des §eClans§8.")
                .addLoreLine("§8| §eKlicke §7auf das §cRote Kreuz§7, um den §eClan §7nicht zu §elöschen§8.");
        ItemBuilder delete = new ItemBuilder(Material.PLAYER_HEAD).setSkullTexture(deleteClanYes).setDisplayName("§8» §7Clan §aLöschen");
        ItemBuilder dontdelete = new ItemBuilder(Material.PLAYER_HEAD).setSkullTexture(deleteClanNo).setDisplayName("§8» §7Clan nicht §cLöschen");
        ItemBuilder green = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).setDisplayName(" ");
        ItemBuilder red = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName(" ");
        InventoryAdvancer.fillNulledInventory(greystainedglass, inventory);
        inventory.setItem(11, delete.build());
        inventory.setItem(15, dontdelete.build());
        inventory.setItem(4, explain.build());
        fillSoroundingsinDelete(inventory, green);
        fillSoroundingsinNoDelete(inventory, red);
        player.openInventory(inventory);
    }

    @SneakyThrows
    public void openClanLevel(Player player) {
        String clanName = ChatColor.translateAlternateColorCodes('&', clanManager.getClanName(player.getUniqueId()));
        String clanPrefix = clanManager.getClanPrefix(player.getUniqueId());
        UUID uuid = player.getUniqueId();
        int level = clanManager.getClanLevel(clanPrefix);
        int nextLevelCost = getLevelUpCost(level);
        int currentEco = clanManager.getClanEco(clanPrefix);

        double percentage = Math.min((currentEco / (double) nextLevelCost) * 100, 100);

        Inventory inventory = Bukkit.createInventory(player, 6 * 9, "§x§6§0§6§0§6§0§lClan §8» " + clanName + " §8| §x§6§0§6§0§6§0§lLevel: §e" + level);

        Material glassMaterial = switch (level) {
            case 1 -> Material.LIME_STAINED_GLASS_PANE;
            case 2 -> Material.GREEN_STAINED_GLASS_PANE;
            case 3 -> Material.BLUE_STAINED_GLASS_PANE;
            case 4 -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
            case 5 -> Material.CYAN_STAINED_GLASS_PANE;
            case 6 -> Material.ORANGE_STAINED_GLASS_PANE;
            default -> Material.WHITE_STAINED_GLASS_PANE;
        };

        ItemBuilder pattern = new ItemBuilder(glassMaterial).setDisplayName(" ");
        ItemBuilder corners = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ");
        InventoryAdvancer.makePattern(inventory, pattern);
        InventoryAdvancer.fillCorners(inventory, corners);

        String levelTexture = CLAN_LEVEL_ICONS.getOrDefault(level, CLAN_LEVEL_ICONS.get(1));
        ItemBuilder clanLevelItem = new ItemBuilder(Material.PLAYER_HEAD).setSkullTexture(levelTexture).setDisplayName("§a§lLevel");
        ItemBuilder upgrade = new ItemBuilder(Material.PLAYER_HEAD).setSkullTexture(upgrade());

        if (level == 6) {
            upgrade.setDisplayName("§c§l§mUpgrade")
                    .addLoreLine("§8» §7Hier hast du die §eMöglichkeit §7das §eLevel §7deines §eClans §7zu §aerhöhen§8.")
                    .addLoreLine(" ")
                    .addLoreLine("§c§l! §7Dein §eClan §7ist schon auf dem §6Maximalen §eLevel§8.");
        } else {
            upgrade.setDisplayName("§a§lUpgrade")
                    .addLoreLine("§8» §7Hier hast du die §eMöglichkeit §7das §eLevel §7deines §eClans §7zu §aerhöhen§8.")
                    .addLoreLine(" ")
                    .addLoreLine("§8| §eKlicke §7um deinen §eClan §aaufzuleveln§8.")
                    .addLoreLine("§8| §7Dein Clan ist §aLevel " + level + "§8.")
                    .addLoreLine("§8| §7Kosten für das nächste Level: §e" + CoinFormatter.formatCoins(nextLevelCost) + "€§8.")
                    .addLoreLine("§8| §7Aktueller Stand der Clanbank: §e" + CoinFormatter.formatCoins(currentEco) + "€§8.")
                    .addLoreLine("§8| §7Bereits gesammelte %: §e" + String.format("%.2f", percentage) + "%§8.");
        }

        inventory.setItem(13, clanLevelItem.build());

        for (int i = 2; i <= 6; i++) {
            ItemBuilder shard = new ItemBuilder(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE).setDisplayName("§e§lLevel " + i).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addItemFlag(ItemFlag.HIDE_ITEM_SPECIFICS)
                    .addLoreLine("§8§l» §c§l\uD83D\uDDE1§7 : §e" + DamageModifier.getDamageMultiplier(i))
                    .addLoreLine(" ");

            if (i <= level) {
                shard.addLoreLine("§aFreigeschaltet");
                shard.addEnchant(Enchantment.SILK_TOUCH, 1).addItemFlag(ItemFlag.HIDE_ENCHANTS);
            } else {
                shard.addLoreLine("§cNicht verfügbar");
            }
            setLevelItemShard(inventory, shard, i - 2);
        }
        inventory.setItem(49, backitem());
        inventory.setItem(31, upgrade.build());

        player.openInventory(inventory);
    }

    private int getLevelUpCost(int currentLevel) {
        return switch (currentLevel) {
            case 1 -> 100000;
            case 2 -> 500000;
            case 3 -> 600000;
            case 4 -> 700000;
            case 5 -> 1200000;
            default -> 0;
        };
    }


    @SneakyThrows
    public void openClanBank(Player player) {
        String clanName = ChatColor.translateAlternateColorCodes('&', clanManager.getClanName(player.getUniqueId()));
        String clanPrefix = clanManager.getClanPrefix(player.getUniqueId());
        String rang = clanManager.isClanOwner(player.getUniqueId()) ? "§c§lOwner" : "§7§lMitglied";
        UUID uuid = player.getUniqueId();
        int clanbankguthaben = clanManager.getClanEco(clanPrefix);
        String claneco = CoinFormatter.formatCoins(clanbankguthaben);
        Inventory inventory = Bukkit.createInventory(player, 6 * 9, "§x§6§0§6§0§6§0§lClanbank §8» " + clanName);

        ItemBuilder coins = new ItemBuilder(Material.PLAYER_HEAD)
                .setSkullTexture(valueOfCoins())
                .setDisplayName("§x§F§A§C§A§4§7§lC§x§E§E§B§C§4§3§ll§x§E§2§A§E§4§0§la§x§D§6§A§0§3§C§ln§x§C§B§9§2§3§8§lb§x§B§F§8§4§3§4§la§x§B§3§7§6§3§1§ln§x§A§7§6§8§2§D§lk")
                .addLoreLine("§8» §7Hier siehst du wie viel §eGeld §7auf der §eClanbank §cdeponiert §7ist§8.")
                .addLoreLine(" ")
                .addLoreLine("§8| §7Clanbankguthaben §8» §e" + claneco + "€");

        ItemBuilder pattern = new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE).setDisplayName(" ");
        ItemBuilder corners = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ");

        ItemBuilder minus = new ItemBuilder(Material.PLAYER_HEAD)
                .setSkullTexture(widhtdraw());
        if (clanManager.isClanOwner(player.getUniqueId())) {
            minus.setDisplayName("§c§lAuszahlen")
                    .addLoreLine("§8» §cGeld abheben")
                    .addLoreLine("§8| §7Klicke um §eGeld §7von deiner §eClanbank §7abzuheben§8.")
                    .addLoreLine(" ")
                    .addLoreLine("§8| §7Du wirst aufgefordert den gewünschten Betrag im §eChat §7zu schreiben§8.")
                    .addLoreLine("§8| §7Weitere Informationen zur Transaktion erhältst du im Chat§8.");
        } else {
            minus.setDisplayName("§c§l§mAuszahlen")
                    .addLoreLine("§8» §cGeld abheben")
                    .addLoreLine("§8| §7Du hast §ckeine §eBerechtigung §eGeld §7von der §eClanbank §cabzuheben§8.");
        }

        ItemBuilder plus = new ItemBuilder(Material.PLAYER_HEAD)
                .setSkullTexture(deposit())
                .setDisplayName("§a§lEinzahlen")
                .addLoreLine("§8» §aGeld lagern")
                .addLoreLine("§8| §7Klicke um §eGeld §7auf deiner §eClanbank §7zu §alagern§8.")
                .addLoreLine(" ")
                .addLoreLine("§8| §7Du wirst aufgefordert den gewünschten Betrag im §eChat §7zu schreiben§8.")
                .addLoreLine("§8| §7Weitere Informationen zur Transaktion erhältst du im Chat§8.");

        InventoryAdvancer.fillCorners(inventory, corners);
        InventoryAdvancer.makePattern(inventory, pattern);
        inventory.setItem(13, coins.build());
        inventory.setItem(49, backitem());
        inventory.setItem(29, plus.build());
        inventory.setItem(33, minus.build());

        player.openInventory(inventory);
    }

    @SneakyThrows
    public void openClanMemberMenu(Player player) {
        String clanName = ChatColor.translateAlternateColorCodes('&', clanManager.getClanName(player.getUniqueId()));
        String clanPrefix = clanManager.getClanPrefix(player.getUniqueId());
        String rang = clanManager.isClanOwner(player.getUniqueId()) ? "§c§lOwner" : "§7§lMitglied";
        UUID uuid = player.getUniqueId();
        int clanbankguthaben = clanManager.getClanEco(clanPrefix);
        String claneco = CoinFormatter.formatCoins(clanbankguthaben);
        List<UUID> clanMembers = clanManager.getClanMembers(clanPrefix);
        int membercount = clanMembers.size();

        Inventory inventory = Bukkit.createInventory(player, 6 * 9, "§x§6§0§6§0§6§0§lClan §8» " + clanName + " §8| §x§6§0§6§0§6§0§lMitglieder");

        ItemBuilder pattern = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).setDisplayName(" ");
        ItemBuilder corners = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ");

        InventoryAdvancer.makePattern(inventory, pattern);
        InventoryAdvancer.fillCorners(inventory, corners);

        UUID clanOwnerUUID = clanManager.getClanOwner("Owner", clanPrefix);
        String clanOwnerName = UUIDFetcher.getName(clanOwnerUUID);


        String ownerRank = clanManager.getMemberRank(clanOwnerUUID, clanPrefix);

        ownerRank = switch (ownerRank) {
            case "Owner" -> "§4Owner";
            case "Vize" -> "§cVize";
            case "Ältester" -> "§6Ältester";
            case "Mitglied" -> "§eMitglied";
            default -> ownerRank;
        };


        String lastJoinedOwner = LastJoinedCache.getLastJoined(clanOwnerUUID);

        ItemBuilder ownerHead = new ItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName(clanOwnerName != null ? "§7" + clanOwnerName : "§7Unknown")
                .setSkullTexture(SkullTextureCache.getSkullTexture(clanOwnerUUID))
                .addLoreLine("§8» §7Rank: " + ownerRank)
                .addLoreLine("§8» §7Status: " + (isPlayerOnline(clanOwnerUUID) ? "§aOnline" : "§cOffline"))
                .addLoreLine("§8» §7Zuletzt: §e" + (isPlayerOnline(clanOwnerUUID) ? "§aAktiv" : lastJoinedOwner));
        inventory.setItem(13, ownerHead.build());

        clanMembers.remove(clanOwnerUUID);

        int[] memberSlots = {21, 22, 23, 28, 29, 30, 31, 32, 33, 34};
        int slotIndex = 0;


        for (UUID memberUUID : clanMembers) {
            if (slotIndex >= memberSlots.length) {
                break;
            }


            String memberName = PlayerNameCache.getPlayerName(memberUUID);
            String memberRank = RankCache.getRank(memberUUID);

            // Rang formatieren
            memberRank = switch (memberRank) {
                case "Owner" -> "§4Owner";
                case "Vize" -> "§cVize";
                case "Ältester" -> "§6Ältester";
                case "Mitglied" -> "§eMitglied";
                default -> memberRank;
            };

            // SkullTexture aus dem Cache abrufen
            String skullTexture = SkullTextureCache.getSkullTexture(memberUUID);

            // Spieler-Kopf erstellen


            String lastJoined = LastJoinedCache.getLastJoined(memberUUID);

            ItemBuilder memberHead = new ItemBuilder(Material.PLAYER_HEAD)
                    .setDisplayName(memberName != null ? "§7" + memberName : "§7Unknown")
                    .setSkullTexture(skullTexture) // Textur statt Name verwenden
                    .addLoreLine("§8» §7Rank: " + memberRank)
                    .addLoreLine("§8» §7Status: " + (isPlayerOnline(memberUUID) ? "§aOnline" : "§cOffline"))
                    .addLoreLine("§8» §7Zuletzt: §e" + (isPlayerOnline(memberUUID) ? "§aAktiv" : lastJoined ));

            // Item in das Inventar setzen
            inventory.setItem(memberSlots[slotIndex], memberHead.build());
            slotIndex++;
        }


        inventory.setItem(49, backitem());
        player.openInventory(inventory);
    }


    @SneakyThrows
    public void openMemberManager(Player player, UUID memberUUID) {
        String clanPrefix = clanManager.getClanPrefix(memberUUID);
        String memberName = UUIDFetcher.getName(memberUUID);
        String memberRank = "§7" + clanManager.getMemberRank(memberUUID, clanManager.getClanPrefix(memberUUID));
        String clanName = ChatColor.translateAlternateColorCodes('&', clanManager.getClanName(player.getUniqueId()));
        String currentRank = clanManager.getMemberRank(memberUUID, clanPrefix);
        String nextRank = getNextRank(currentRank);

        // Erstelle das Inventar
        Inventory inventory = Bukkit.createInventory(player, 6 * 9, "§x§6§0§6§0§6§0§lVerwalten von Mitglied §8» §7" + (memberName != null ? memberName : "Unknown"));

        // Spieler-Kopf mit Texture
        ItemBuilder memberHead = new ItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName(memberName != null ? "§7" + memberName : "§7Unknown")
                .setSkullTexture(SkullTextureCache.getSkullTexture(memberUUID)) // Texture aus dem Cache
                .addLoreLine("§8» §7Rank: " + memberRank);

        // Weitere Items
        ItemBuilder pattern = new ItemBuilder(Material.PINK_STAINED_GLASS_PANE).setDisplayName("");
        ItemBuilder corner = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName("");

        ItemBuilder promoteItem = new ItemBuilder(Material.PLAYER_HEAD)
                .setSkullTexture(promote()) // Textur für Beförderung
                .setDisplayName("§aBefördern zum " + nextRank)
                .addLoreLine("§7Befördere dieses Mitglied zum Clan-" + nextRank + ".");

        ItemBuilder demoteItem = new ItemBuilder(Material.PLAYER_HEAD)
                .setSkullTexture(demote()) // Textur für Degradierung
                .setDisplayName(getDownrankRank(currentRank) != null ? "§cDegradieren zum " + getDownrankRank(currentRank) : "§cDegradieren nicht möglich!")
                .addLoreLine(getDownrankRank(currentRank) != null ? "§7Degradiere dieses Mitglied zum Clan-" + getDownrankRank(currentRank) + "." : "§7" + currentRank);

        ItemBuilder kickItem = new ItemBuilder(Material.PLAYER_HEAD)
                .setSkullTexture(widhtdraw()) // Textur für Ausschluss
                .setDisplayName("§cAus Clan werfen")
                .addLoreLine("§8| §7Werfe dieses Mitglied aus dem Clan.");

        // Muster und Ecken setzen
        InventoryAdvancer.makePattern(inventory, pattern);
        InventoryAdvancer.fillCorners(inventory, corner);

        // Items ins Inventar setzen
        inventory.setItem(13, memberHead.build());
        inventory.setItem(30, promoteItem.build());
        inventory.setItem(32, demoteItem.build());
        inventory.setItem(31, kickItem.build());
        inventory.setItem(49, backitem());

        // Öffne das Inventar
        player.openInventory(inventory);
    }


    @SneakyThrows
    public void openClanWarMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 5 * 9, "§x§6§0§6§0§6§0§lClankrieg §8» §7Verwaltung");

        player.openInventory(inventory);

    }


    public void setClanSettingsMiddle(Inventory inventory, ItemBuilder item) {
        int[] slots = {13, 22, 31, 30, 32, 39, 41};
        for (int slot : slots) {
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, item.build());
            }
        }
    }

    public void fillSoroundingsinDelete(Inventory inventory, ItemBuilder item) {
        int[] slots = {0, 1, 2, 3, 9, 10, 12, 18, 19, 20, 21};
        for (int slot : slots) {
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, item.build());
            }
        }
    }

    public void fillSoroundingsinNoDelete(Inventory inventory, ItemBuilder item) {
        int[] slots = {5, 6, 7, 8, 14, 16, 17, 23, 24, 25, 26};
        for (int slot : slots) {
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, item.build());
            }
        }
    }

    public void setLevelItemShard(Inventory inventory, ItemBuilder item, int index) {
        int[] slots = {38, 39, 40, 41, 42};
        if (index < slots.length) {
            int slot = slots[index];
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, item.build());
            }
        }
    }

    private String backHeadValue() {
        return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY5NzFkZDg4MWRiYWY0ZmQ2YmNhYTkzNjE0NDkzYzYxMmY4Njk2NDFlZDU5ZDFjOTM2M2EzNjY2YTVmYTYifX19";
    }

    private String exclamation() {
        return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmJjM2U2NzdiYzM1ZmY4OGFiZDRiNmRjYTU0ZjAwYWRhNDQwNzA2YjExY2VjODEyOWM3Zjg4MGJkNjVmNjBkIn19fQ==";
    }

    private String valueOfCoins() {
        return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2QyNGRjNTgwNjljMTIxMmI1MjlhNGFlNWQ0ZTczYmUwOTkwZDQ2ZmU5MzcxYjFmNzllODE2NGI0Mjg1OWFjOCJ9fX0=";
    }

    private ItemStack backitem() {
        return new ItemBuilder(Material.PLAYER_HEAD).setSkullTexture(backHeadValue()).setDisplayName("§8» §cZurück").build();
    }

    private String upgrade() {
        return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWRhMDI3NDc3MTk3YzZmZDdhZDMzMDE0NTQ2ZGUzOTJiNGE1MWM2MzRlYTY4YzhiN2JjYzAxMzFjODNlM2YifX19";
    }

    private String deposit() {
        return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWRmZmYxYjNjNWQ4NWZlM2NkZDU2NTY4NjliYWEwZWFkZTVlNTNhY2E5ZDU2MTQyNzY0OGNjNzJmNWUyNWE5In19fQ==";
    }

    private String widhtdraw() {
        return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGU0YjhiOGQyMzYyYzg2NGUwNjIzMDE0ODdkOTRkMzI3MmE2YjU3MGFmYmY4MGMyYzViMTQ4Yzk1NDU3OWQ0NiJ9fX0=";
    }


    private String promote() {
        return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWM1YzdlNTM2OTVkODhmNGQzMWY1MmY0M2ZhYzYwOWFkOWU2MmJjOTdkNDlmYzUwNDE3NGRmZGI4NDE1MGMzOSJ9fX0=";
    }

    private String demote() {
        return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGM4OWFhMjljYjUwMzU5YzlhYzU3M2U4NmM4ZWIwNzIxMTk3Nzk2Y2ZhYjQzZmY1Y2UxNzMzODViOWRmMmVkMSJ9fX0=";
    }

    public boolean isPlayerOnline(UUID playerUUID) {
        return Bukkit.getOnlinePlayers().stream()
                .anyMatch(player -> player.getUniqueId().equals(playerUUID));
    }

    public String getNextRank(String currentRank) {
        int currentIndex = RANKS.indexOf(currentRank);
        if (currentIndex >= 0 && currentIndex < RANKS.size() - 1) {
            return RANKS.get(currentIndex + 1);
        }
        return null;
    }

    public String getDownrankRank(String currentRank) {
        int currentIndex = RANKS.indexOf(currentRank);
        if (currentIndex > 0) {
            return RANKS.get(currentIndex - 1);
        }
        return null;
    }

}

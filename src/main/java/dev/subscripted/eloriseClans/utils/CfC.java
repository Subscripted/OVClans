package dev.subscripted.eloriseClans.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CfC {

    static SmartConfig config;
    static String MESSAGES_PATH = "messages.";
    static String COMMAND = "clancommand.";

    public static void createSomeDefaults() {
        config = new SmartConfig("mysql.yml");
        config = new SmartConfig("messages.yml");
    }

    public static void setSomeDefaults() {
        config = SmartConfig.load("mysql.yml");
        config.setString("host", "localhost");
        config.setInt("port", 3306);
        config.setString("password", "");
        config.setString("username", "root");
        config.setString("database", "survival");


        config = SmartConfig.load("messages.yml");
        config.setString("PluginPrefix", "§x§4§B§6§3§7§E§lC§x§6§D§7§2§8§1§ll§x§8§F§8§1§8§5§la§x§B§0§8§F§8§8§ln§x§D§2§9§E§8§B§ls §8▪ ");
        config.setString("ClanChatPrefix", "§x§C§5§C§3§7§5§lC§x§C§5§C§3§7§5§ll§x§C§5§C§3§7§5§la§x§C§5§C§3§7§5§ln§x§C§5§C§3§7§5§lc§x§C§5§C§3§7§5§lh§x§C§5§C§3§7§5§la§x§C§5§C§3§7§5§lt §8▪ ");
        config.setString(MESSAGES_PATH + COMMAND + "unterbefehl", "§7Verwende einen Unterbefehl wie §e/clan create §c<prefix> <name>");
    }
}

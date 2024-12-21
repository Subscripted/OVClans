package dev.subscripted.eloriseClans.utils;

import dev.subscripted.eloriseClans.Main;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CfC {

    static SmartConfig config;

    public enum ConfigPath {
        MESSAGES_COMMAND("messages.clancommand."),
        MESSAGES_COMMAND_UNTERBEFEHL(MESSAGES_COMMAND.path + "unterbefehl"),
        MESSAGES_COMMAND_MAX_TWOLF_CHARACTERS(MESSAGES_COMMAND.path + "maxtwolfcharacters"),
        CLAN_PRICE("clanprice"),
        MYSQL_HOST("host"),
        MYSQL_PORT("port"),
        MYSQL_PASSWORD("password"),
        MYSQL_USERNAME("username"),
        MYSQL_DATABASE("database"),
        PLUGIN_PREFIX("PluginPrefix"),
        CLAN_CHAT_PREFIX("ClanChatPrefix");

        private final String path;

        ConfigPath(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    public static void createSomeDefaults() {
        config = new SmartConfig("mysql.yml");
        config = new SmartConfig("messages.yml");
        config = new SmartConfig("general.yml");
    }

    public static void setSomeDefaults() {
        config = SmartConfig.load("general.yml");
        config.setInt(ConfigPath.CLAN_PRICE.getPath(), 500000);

        config = SmartConfig.load("mysql.yml");
        config.setString(ConfigPath.MYSQL_HOST.getPath(), "localhost");
        config.setInt(ConfigPath.MYSQL_PORT.getPath(), 3306);
        config.setString(ConfigPath.MYSQL_PASSWORD.getPath(), "");
        config.setString(ConfigPath.MYSQL_USERNAME.getPath(), "root");
        config.setString(ConfigPath.MYSQL_DATABASE.getPath(), "survival");

        config = SmartConfig.load("messages.yml");
        config.setString(ConfigPath.PLUGIN_PREFIX.getPath(), "§x§4§B§6§3§7§E§lC§x§6§D§7§2§8§1§ll§x§8§F§8§1§8§5§la§x§B§0§8§F§8§8§ln§x§D§2§9§E§8§B§ls §8▪ ");
        config.setString(ConfigPath.CLAN_CHAT_PREFIX.getPath(), "§x§C§5§C§3§7§5§lC§x§C§5§C§3§7§5§ll§x§C§5§C§3§7§5§la§x§C§5§C§3§7§5§ln§x§C§5§C§3§7§5§lc§x§C§5§C§3§7§5§lh§x§C§5§C§3§7§5§la§x§C§5§C§3§7§5§lt §8▪ ");
        config.setString(ConfigPath.MESSAGES_COMMAND_UNTERBEFEHL.getPath(), "§7Verwende einen Unterbefehl wie §e/clan create §c<prefix> <name>");
        config.setString(ConfigPath.MESSAGES_COMMAND_MAX_TWOLF_CHARACTERS.getPath(), "§7Der Clan-Prefix und Name dürfen maximal 12 Zeichen lang sein!");
    }
}

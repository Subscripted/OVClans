package dev.subscripted.eloriseClans;

import dev.subscripted.eloriseClans.commands.ClanCommand;
import dev.subscripted.eloriseClans.database.MySQL;
import dev.subscripted.eloriseClans.database.connections.Coins;
import dev.subscripted.eloriseClans.events.BlockBreak;
import dev.subscripted.eloriseClans.events.ClanMenuInteractions;
import dev.subscripted.eloriseClans.events.JoinLeave;
import dev.subscripted.eloriseClans.events.LevelAbs;
import dev.subscripted.eloriseClans.gui.ClanMenus;
import dev.subscripted.eloriseClans.manager.BankUIListener;
import dev.subscripted.eloriseClans.manager.ClanManager;
import dev.subscripted.eloriseClans.utils.ChunkCache;
import dev.subscripted.eloriseClans.utils.SoundLibrary;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    @Getter
    private static Main instance;
    @Getter
    private MySQL mySQL;
    @Getter
    private Coins coins;
    @Getter
    private SoundLibrary library;
    @Getter
    public String prefix = ChatColor.translateAlternateColorCodes('&', "&8» &8| &x&F&3&9&E&D&C&lA&x&E&7&9&8&D&B&ls&x&D&B&9&2&D&9&lc&x&D&0&8&C&D&8&la&x&C&4&8&6&D&7&ll&x&B&8&8&0&D&6&lt&x&A&C&7&9&D&4&la&x&A&0&7&3&D&3&lr&x&9&5&6&D&D&2&l &8» ");
    @Getter
    private ClanManager clanManager;
    @Getter
    private ChunkCache chunkCache;

    @Override
    public void onEnable() {


        getLogger().info("\n" +
                "[]=====================================================[] \n" +
                "    _   __           _ __                   __   \n" +
                "   / | / /___ _   __(_) /_  ___  _____ ____/ /__ \n" +
                "  /  |/ / __ \\ | / / / __ \\/ _ \\/ ___// __  / _ \\\n" +
                " / /|  / /_/ / |/ / / /_/ /  __(__  )/ /_/ /  __/\n" +
                "/_/ |_/\\____/|___/_/_.___/\\___/____(_)__,_/\\___/ \n" +
                "                                                 \n" +
                "[]=====================================================[] \n");

        instance = this;
        mySQL = new MySQL();
        coins = new Coins(mySQL);
        library = new SoundLibrary();
        clanManager = new ClanManager(mySQL);
        MySQL.connect();
        MySQL.createTable();
        chunkCache = new ChunkCache();



        getLogger().info("\n" +
                "[]=====================================================[] \n" +
                "  _____                _ _               _____                                          _     \n" +
                " |  __ \\              (_) |             / ____|                                        | |    \n" +
                " | |__) |___  __ _ ___ _| |_ ___ _ __  | |     ___  _ __ ___  _ __ ___   __ _ _ __   __| |___ \n" +
                " |  _  // _ \\/ _` / __| | __/ _ \\ '__| | |    / _ \\| '_ ` _ \\| '_ ` _ \\ / _` | '_ \\ / _` / __|\n" +
                " | | \\ \\  __/ (_| \\__ \\ | ||  __/ |    | |___| (_) | | | | | | | | | | | (_| | | | | (_| \\__ \\\n" +
                " |_|  \\_\\___|\\__, |___/_|\\__\\___|_|     \\_____\\___/|_| |_| |_|_| |_| |_|\\__,_|_| |_|\\__,_|___/\n" +
                "              __/ |                                                                           \n" +
                "             |___/                                                                            \n" +
                "[]=====================================================[]\n");

        getCommand("clan").setExecutor(new ClanCommand(clanManager, new ClanMenus(mySQL, clanManager), library));

        getLogger().info("\n" +
                "[]=====================================================[] \n" +
                "  _____                _ _              _      _     _                       \n" +
                " |  __ \\              (_) |            | |    (_)   | |                      \n" +
                " | |__) |___  __ _ ___ _| |_ ___ _ __  | |     _ ___| |_ ___ _ __   ___ _ __ \n" +
                " |  _  // _ \\/ _` / __| | __/ _ \\ '__| | |    | / __| __/ _ \\ '_ \\ / _ \\ '__|\n" +
                " | | \\ \\  __/ (_| \\__ \\ | ||  __/ |    | |____| \\__ \\ ||  __/ | | |  __/ |   \n" +
                " |_|  \\_\\___|\\__, |___/_|\\__\\___|_|    |______|_|___/\\__\\___|_| |_|\\___|_|   \n" +
                "              __/ |                                                          \n" +
                "             |___/                                                           \n" +
                "[]=====================================================[] \n");
        getServer().getPluginManager().registerEvents(new ClanMenuInteractions(clanManager, library, new ClanMenus(mySQL, clanManager)), instance);
        getServer().getPluginManager().registerEvents(new LevelAbs(library, clanManager), instance);
        getServer().getPluginManager().registerEvents(new BankUIListener(library, clanManager, new ClanMenus(mySQL, clanManager)), instance);
        getServer().getPluginManager().registerEvents(new LevelAbs(library, clanManager), instance);
        getServer().getPluginManager().registerEvents(new BlockBreak(clanManager), instance);
        getServer().getPluginManager().registerEvents(new JoinLeave(clanManager), instance);
    }

    @Override
    public void onDisable() {
    }
}


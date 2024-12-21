package dev.subscripted.eloriseClans;

import dev.subscripted.eloriseClans.commands.ClanCommand;
import dev.subscripted.eloriseClans.database.MySQL;
import dev.subscripted.eloriseClans.database.connections.Coins;
import dev.subscripted.eloriseClans.events.*;
import dev.subscripted.eloriseClans.gui.ClanMenus;
import dev.subscripted.eloriseClans.manager.BankUIListener;
import dev.subscripted.eloriseClans.manager.ClanManager;
import dev.subscripted.eloriseClans.utils.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

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
    private String prefix;
    @Getter
    private ClanManager clanManager;
    @Getter
    private ChunkCache chunkCache;
    private SmartConfig config;

    @Override
    public void onEnable() {
        instance = this;
        initializeDefaults();
        setupDatabase();
        setupCaches();
        registerCommands();
        registerEvents();
        initializeOnlinePlayers();
    }

    @Override
    public void onDisable() {
    }

    /**
     * Initialisiert Standardwerte.
     */
    private void initializeDefaults() {
        CfC.createSomeDefaults();
        CfC.setSomeDefaults();
        config = SmartConfig.load("messages.yml");
        prefix = config.getString("PluginPrefix");
    }

    /**
     * Initialisiert die Datenbankverbindung und erstellt Tabellen.
     */
    private void setupDatabase() {
        mySQL = new MySQL();
        MySQL.connect();
        MySQL.createTable();
        coins = new Coins(mySQL);
        clanManager = new ClanManager(mySQL);
    }

    /**
     * Initialisiert benötigte Caches.
     */
    private void setupCaches() {
        library = new SoundLibrary();
        chunkCache = new ChunkCache();

        Map<UUID, String> players = clanManager.fetchPlayersFromDatabase();
        SkullTextureCache.initializeCache(players);
    }

    /**
     * Registriert Befehle.
     */
    private void registerCommands() {
        ClanMenus clanMenus = new ClanMenus(mySQL, clanManager);
        getCommand("clan").setExecutor(new ClanCommand(clanManager, clanMenus, library));
    }

    /**
     * Registriert alle Event-Listener.
     */
    private void registerEvents() {
        ClanMenus clanMenus = new ClanMenus(mySQL, clanManager);

        getServer().getPluginManager().registerEvents(new ClanMenuInteractions(clanManager, library, clanMenus), this);
        getServer().getPluginManager().registerEvents(new LevelAbs(library, clanManager), this);
        getServer().getPluginManager().registerEvents(new BankUIListener(library, clanManager, clanMenus), this);
        getServer().getPluginManager().registerEvents(new ClanChunkInteract(clanManager), this);
        getServer().getPluginManager().registerEvents(new JoinLeave(clanManager), this);
    }

    /**
     * Initialisiert bereits eingeloggte Spieler.
     */
    private void initializeOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                String clanPrefix = clanManager.getClanPrefix(player.getUniqueId());
                if (clanManager.isMemberOfClan(player.getUniqueId(), clanPrefix)) {
                    clanManager.startShowingClaims(player, clanPrefix);
                }
            } catch (SQLException e) {
                getLogger().severe("Fehler beim Initialisieren des Spielers: " + player.getName());
                e.printStackTrace();
            }
        }
    }
}

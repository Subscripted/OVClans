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
import dev.subscripted.eloriseClans.utils.CfC;
import dev.subscripted.eloriseClans.utils.ChunkCache;
import dev.subscripted.eloriseClans.utils.SoundLibrary;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

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
        instance = this;
        CfC.createSomeDefaults();
        CfC.setSomeDefaults();
        mySQL = new MySQL();
        coins = new Coins(mySQL);
        library = new SoundLibrary();
        clanManager = new ClanManager(mySQL);
        MySQL.connect();
        MySQL.createTable();
        chunkCache = new ChunkCache();
        getCommand("clan").setExecutor(new ClanCommand(clanManager, new ClanMenus(mySQL, clanManager), library));
        getServer().getPluginManager().registerEvents(new ClanMenuInteractions(clanManager, library, new ClanMenus(mySQL, clanManager)), instance);
        getServer().getPluginManager().registerEvents(new LevelAbs(library, clanManager), instance);
        getServer().getPluginManager().registerEvents(new BankUIListener(library, clanManager, new ClanMenus(mySQL, clanManager)), instance);
        getServer().getPluginManager().registerEvents(new LevelAbs(library, clanManager), instance);
        getServer().getPluginManager().registerEvents(new BlockBreak(clanManager), instance);
        getServer().getPluginManager().registerEvents(new JoinLeave(clanManager), instance);


        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                if (clanManager.isMemberOfClan(p.getUniqueId(), clanManager.getClanPrefix(p.getUniqueId()))) {
                    clanManager.startShowingClaims(p, clanManager.getClanPrefix(p.getUniqueId()));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public void onDisable() {
    }
}


package dev.subscripted.eloriseClans.database;

import dev.subscripted.eloriseClans.Main;
import dev.subscripted.eloriseClans.utils.SmartConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MySQL {

    static Connection connection;
    static SmartConfig config = SmartConfig.load("mysql.yml");

    public static String username = config.getString("username");
    public static String password = config.getString("password");
    public static String database = config.getString("database");
    public static String host = config.getString("host");
    public static String port = config.getString("port");

    public static Connection getConnection() {
        if (!isConnected()) {
            connect();
        }
        return connection;
    }

    @SneakyThrows
    public static void connect() {
        if (isConnected()) {
            return;
        }

        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
        } catch (SQLException e) {
            Main.getInstance().getLogger().info(Main.getInstance().getPrefix() + "MySQL Connection could not be established. Check your MySQL configuration.");
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public static void close() {
        if (isConnected()) {
            connection.close();
            Main.getInstance().getLogger().info(Main.getInstance().getPrefix() + "MySQL connection closed!");
        }
    }

    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @SneakyThrows
    public static void createTable() {
        if (isConnected()) {
            try {
                connection.createStatement().executeUpdate(
                        "CREATE TABLE IF NOT EXISTS Clans (" +
                                "ClanPrefix VARCHAR(50) PRIMARY KEY, " +
                                "ClanName VARCHAR(255) NOT NULL, " +
                                "ClanLevel INT NOT NULL," +
                                "Chunks INT DEFAULT 4," +
                                "ClanbankEco INT DEFAULT 0)"
                );
                connection.createStatement().executeUpdate(
                        "CREATE TABLE IF NOT EXISTS ClanMembers (" +
                                "MemberID INT PRIMARY KEY AUTO_INCREMENT, " +
                                "ClanPrefix VARCHAR(50) NOT NULL, " +
                                "MemberUUID CHAR(36) NOT NULL, " +
                                "Rank ENUM('Mitglied', 'Ältester', 'Vize', 'Owner') NOT NULL DEFAULT 'Mitglied', " +
                                "lastSeen TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (ClanPrefix) REFERENCES Clans(ClanPrefix) ON DELETE CASCADE)"
                );

                connection.createStatement().executeUpdate(
                        "CREATE TABLE IF NOT EXISTS ClanSettings (" +
                                "ClanPrefix VARCHAR(50) NOT NULL, " +
                                "pvp BOOLEAN NOT NULL DEFAULT FALSE, " +
                                "clanborder BOOLEAN NOT NULL DEFAULT true, " +
                                "PRIMARY KEY (ClanPrefix), " +
                                "FOREIGN KEY (ClanPrefix) REFERENCES Clans(ClanPrefix) ON DELETE CASCADE)"
                );

                connection.createStatement().executeUpdate(
                        "CREATE TABLE IF NOT EXISTS ClaimedChunks (" +
                                "ChunkID INT PRIMARY KEY AUTO_INCREMENT, " +
                                "ClanPrefix VARCHAR(50) NOT NULL, " +
                                "World VARCHAR(255) NOT NULL, " +
                                "ChunkX INT NOT NULL, " +
                                "ChunkZ INT NOT NULL, " +
                                "FOREIGN KEY (ClanPrefix) REFERENCES Clans(ClanPrefix) ON DELETE CASCADE, " +
                                "UNIQUE(World, ChunkX, ChunkZ))"
                );

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @SneakyThrows
    public static void update(String query) {
        if (isConnected()) {
            connection.createStatement().executeUpdate(query);
        }
    }
}
package me.maybeizen.EasyTPA.manager;

import me.maybeizen.EasyTPA.EasyTPA;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DatabaseManager {
    private final EasyTPA plugin;
    private Connection connection;
    private final File databaseFile;

    public DatabaseManager(EasyTPA plugin) {
        this.plugin = plugin;
        this.databaseFile = new File(plugin.getDataFolder(), "easytpa.db");
    }

    public void initialize() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            
            createTables();
            plugin.getLogger().info("Database initialized successfully.");
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }
    }

    private void createTables() throws SQLException {
        String userPreferencesTable = """
            CREATE TABLE IF NOT EXISTS user_preferences (
                uuid TEXT PRIMARY KEY,
                requests_enabled INTEGER NOT NULL DEFAULT 1,
                last_request_time INTEGER DEFAULT 0
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(userPreferencesTable);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initialize();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reconnect to database", e);
        }
        return connection;
    }

    public CompletableFuture<Void> setRequestsEnabled(UUID uuid, boolean enabled) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement stmt = getConnection().prepareStatement(
                    "INSERT OR REPLACE INTO user_preferences (uuid, requests_enabled) VALUES (?, ?)")) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, enabled ? 1 : 0);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to update user preferences", e);
            }
        }, plugin.getExecutor());
    }

    public CompletableFuture<Boolean> areRequestsEnabled(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement stmt = getConnection().prepareStatement(
                    "SELECT requests_enabled FROM user_preferences WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("requests_enabled") == 1;
                }
                return true;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to check user preferences", e);
                return true;
            }
        }, plugin.getExecutor());
    }

    public CompletableFuture<Void> updateLastRequestTime(UUID uuid, long time) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement stmt = getConnection().prepareStatement(
                    "INSERT OR REPLACE INTO user_preferences (uuid, last_request_time) VALUES (?, ?)")) {
                stmt.setString(1, uuid.toString());
                stmt.setLong(2, time);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to update last request time", e);
            }
        }, plugin.getExecutor());
    }

    public CompletableFuture<Long> getLastRequestTime(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement stmt = getConnection().prepareStatement(
                    "SELECT last_request_time FROM user_preferences WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getLong("last_request_time");
                }
                return 0L;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get last request time", e);
                return 0L;
            }
        }, plugin.getExecutor());
    }

}


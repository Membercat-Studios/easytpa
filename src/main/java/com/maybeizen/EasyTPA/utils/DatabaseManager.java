package com.maybeizen.EasyTPA.utils;

import com.maybeizen.EasyTPA.EasyTPA;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;


public class DatabaseManager {
    private final EasyTPA plugin;
    private Connection connection;
    private final String dbFile;
    private final Object dbLock = new Object();
    
    private static final String CREATE_TOGGLE_TABLE = 
            "CREATE TABLE IF NOT EXISTS toggle_states (" +
            "uuid VARCHAR(36) PRIMARY KEY, " +
            "enabled BOOLEAN NOT NULL DEFAULT 1)";
    
    private static final String INSERT_TOGGLE = 
            "INSERT OR REPLACE INTO toggle_states (uuid, enabled) VALUES (?, ?)";
    
    private static final String SELECT_ALL_TOGGLES = 
            "SELECT uuid, enabled FROM toggle_states";
    
    private static final String SELECT_TOGGLE = 
            "SELECT enabled FROM toggle_states WHERE uuid = ?";
    
    public DatabaseManager(EasyTPA plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "easytpa.db").getAbsolutePath();

        initialize();
    }
    
    private void initialize() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            Class.forName("org.sqlite.JDBC");
            
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            
            try (Statement statement = connection.createStatement()) {
                statement.execute(CREATE_TOGGLE_TABLE);
            }
            
            plugin.getLogger().info("Database connection established successfully");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "SQLite JDBC driver not found", e);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not connect to SQLite database", e);
        }
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error closing database connection", e);
        }
    }
    
    public void saveToggleState(UUID uuid, boolean enabled) {
        saveToggleStateAsync(uuid, enabled, null);
    }
    
    public void saveToggleStateAsync(UUID uuid, boolean enabled, Runnable callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (dbLock) {
                if (connection == null) {
                    plugin.getLogger().warning("Cannot save toggle state: database connection is null");
                    if (callback != null) {
                        plugin.getServer().getScheduler().runTask(plugin, callback);
                    }
                    return;
                }
                try (PreparedStatement statement = connection.prepareStatement(INSERT_TOGGLE)) {
                    statement.setString(1, uuid.toString());
                    statement.setBoolean(2, enabled);
                    statement.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error saving toggle state for " + uuid, e);
                } finally {
                    if (callback != null) {
                        plugin.getServer().getScheduler().runTask(plugin, callback);
                    }
                }
            }
        });
    }

    public Map<UUID, Boolean> loadAllToggleStates() {
        return loadAllToggleStatesAsync(null);
    }
    
    public Map<UUID, Boolean> loadAllToggleStatesAsync(java.util.function.Consumer<Map<UUID, Boolean>> callback) {
        Map<UUID, Boolean> toggleStates = new HashMap<>();
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<UUID, Boolean> loadedStates = new HashMap<>();
            synchronized (dbLock) {
                if (connection == null) {
                    plugin.getLogger().warning("Cannot load toggle states: database connection is null");
                    if (callback != null) {
                        plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(loadedStates));
                    }
                    return;
                }
                
                try (Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery(SELECT_ALL_TOGGLES)) {
                    
                    while (resultSet.next()) {
                        try {
                            UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                            boolean enabled = resultSet.getBoolean("enabled");
                            loadedStates.put(uuid, enabled);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid UUID in database: " + resultSet.getString("uuid"));
                        }
                    }
                    
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error loading toggle states", e);
                }
            }
            
            if (callback != null) {
                Map<UUID, Boolean> finalStates = new HashMap<>(loadedStates);
                plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(finalStates));
            }
        });
        
        return toggleStates;
    }

    public boolean getToggleState(UUID uuid) {
        return getToggleStateAsync(uuid, null);
    }
    
    public boolean getToggleStateAsync(UUID uuid, java.util.function.Consumer<Boolean> callback) {
        final boolean[] result = {true};
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean queryResult = true;
            synchronized (dbLock) {
                if (connection == null) {
                    plugin.getLogger().warning("Cannot get toggle state: database connection is null");
                    if (callback != null) {
                        plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(true));
                    }
                    return;
                }
                try (PreparedStatement statement = connection.prepareStatement(SELECT_TOGGLE)) {
                    statement.setString(1, uuid.toString());
                    
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            queryResult = resultSet.getBoolean("enabled");
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error getting toggle state for " + uuid, e);
                }
            }
            
            if (callback != null) {
                boolean finalResult = queryResult;
                plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(finalResult));
            }
            result[0] = queryResult;
        });
        
        return result[0];
    }
    
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    public void reconnectIfNeeded() {
        try {
            if (connection == null || connection.isClosed()) {
                initialize();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error checking database connection", e);
            initialize();
        }
    }
} 
package com.maybeizen.EasyTPA.utils;

import com.maybeizen.EasyTPA.EasyTPA;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;

public class DataMigrationUtil {
    private final EasyTPA plugin;
    private final DatabaseManager databaseManager;
    
    public DataMigrationUtil(EasyTPA plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }
    
    public int migrateToggleData() {
        File yamlFile = new File(plugin.getDataFolder(), "toggle_data.yml");
        if (!yamlFile.exists()) {
            plugin.getLogger().info("No YAML toggle data file found, skipping migration");
            return 0;
        }
        
        try {
            YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(yamlFile);
            if (!yamlConfig.contains("toggle_states") || !yamlConfig.isConfigurationSection("toggle_states")) {
                plugin.getLogger().info("No toggle states found in YAML file, skipping migration");
                return 0;
            }
            
            int migratedCount = 0;
            for (String uuidStr : yamlConfig.getConfigurationSection("toggle_states").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    boolean enabled = yamlConfig.getBoolean("toggle_states." + uuidStr);
                    
                    databaseManager.saveToggleState(uuid, enabled);
                    migratedCount++;
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in YAML file: " + uuidStr);
                }
            }
            
            plugin.getLogger().info("Successfully migrated " + migratedCount + " toggle states from YAML to SQLite");
            
            if (migratedCount > 0) {
                File backupFile = new File(plugin.getDataFolder(), "toggle_data.yml.bak");
                if (backupFile.exists()) {
                    backupFile.delete();
                }
                yamlFile.renameTo(backupFile);
                plugin.getLogger().info("Renamed old YAML file to toggle_data.yml.bak");
            }
            
            return migratedCount;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error migrating toggle data from YAML to SQLite", e);
            return 0;
        }
    }
} 
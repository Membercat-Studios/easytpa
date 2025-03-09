package com.maybeizen.EasyTPA.managers;

import com.maybeizen.EasyTPA.EasyTPA;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class ToggleManager {
    private final EasyTPA plugin;
    private final HashMap<UUID, Boolean> toggleStates;
    private final File dataFile;
    private FileConfiguration data;

    public ToggleManager(EasyTPA plugin) {
        this.plugin = plugin;
        this.toggleStates = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "toggle_data.yml");
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                if (!dataFile.getParentFile().exists()) {
                    dataFile.getParentFile().mkdirs();
                }
                
                dataFile.createNewFile();
                
                data = new YamlConfiguration();
                data.createSection("toggle_states");
                data.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create toggle_data.yml: " + e.getMessage());
                data = new YamlConfiguration();
                return;
            }
        } else {
            data = YamlConfiguration.loadConfiguration(dataFile);
        }
        
        toggleStates.clear();
        
        if (data.contains("toggle_states") && data.isConfigurationSection("toggle_states")) {
            for (String uuidStr : data.getConfigurationSection("toggle_states").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    boolean state = data.getBoolean("toggle_states." + uuidStr);
                    toggleStates.put(uuid, state);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in toggle_data.yml: " + uuidStr);
                }
            }
        } else {
            data.createSection("toggle_states");
            try {
                data.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save toggle data: " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Loaded " + toggleStates.size() + " toggle states");
    }

    public void saveData() {
        try {
            if (!data.contains("toggle_states")) {
                data.createSection("toggle_states");
            }
            
            for (UUID uuid : toggleStates.keySet()) {
                data.set("toggle_states." + uuid.toString(), toggleStates.get(uuid));
            }
            
            data.save(dataFile);
            plugin.getLogger().info("Saved " + toggleStates.size() + " toggle states");
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save toggle data: " + e.getMessage());
        }
    }

    public boolean isTPEnabled(Player player) {
        return toggleStates.getOrDefault(player.getUniqueId(), true);
    }

    public boolean toggleTP(Player player) {
        boolean newState = !isTPEnabled(player);
        toggleStates.put(player.getUniqueId(), newState);
        saveData();
        return newState;
    }

    public void cleanup() {
        saveData();
        toggleStates.clear();
    }

    public void reload() {
        loadData();
    }
} 
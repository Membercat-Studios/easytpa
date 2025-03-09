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
            plugin.saveResource("toggle_data.yml", false);
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        
        if (data.contains("toggle_states")) {
            for (String uuidStr : data.getConfigurationSection("toggle_states").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                boolean state = data.getBoolean("toggle_states." + uuidStr);
                toggleStates.put(uuid, state);
            }
        }
    }

    public void saveData() {
        try {
            for (UUID uuid : toggleStates.keySet()) {
                data.set("toggle_states." + uuid.toString(), toggleStates.get(uuid));
            }
            data.save(dataFile);
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
} 
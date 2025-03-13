package com.maybeizen.EasyTPA.managers;

import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.DatabaseManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ToggleManager {
    private final EasyTPA plugin;
    private final HashMap<UUID, Boolean> toggleStates;
    private final DatabaseManager databaseManager;

    public ToggleManager(EasyTPA plugin) {
        this.plugin = plugin;
        this.toggleStates = new HashMap<>();
        this.databaseManager = plugin.getDatabaseManager();
        loadData();
    }

    private void loadData() {
        toggleStates.clear();
        
        databaseManager.reconnectIfNeeded();
        
        Map<UUID, Boolean> loadedStates = databaseManager.loadAllToggleStates();
        toggleStates.putAll(loadedStates);
        
    }

    public void saveData() {
        databaseManager.reconnectIfNeeded();
        
        for (Map.Entry<UUID, Boolean> entry : toggleStates.entrySet()) {
            databaseManager.saveToggleState(entry.getKey(), entry.getValue());
        }
        
    }

    public boolean isTPEnabled(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (toggleStates.containsKey(uuid)) {
            return toggleStates.get(uuid);
        }
        
        boolean enabled = databaseManager.getToggleState(uuid);
        toggleStates.put(uuid, enabled);
        return enabled;
    }

    public boolean toggleTP(Player player) {
        UUID uuid = player.getUniqueId();
        boolean newState = !isTPEnabled(player);
        
        toggleStates.put(uuid, newState);
        
        databaseManager.saveToggleState(uuid, newState);
        
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
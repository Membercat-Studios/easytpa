package com.maybeizen.EasyTPA.managers;

import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.DatabaseManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ToggleManager {
    private final EasyTPA plugin;
    private final ConcurrentHashMap<UUID, Boolean> toggleStates;
    private final DatabaseManager databaseManager;

    public ToggleManager(EasyTPA plugin) {
        this.plugin = plugin;
        this.toggleStates = new ConcurrentHashMap<>();
        this.databaseManager = plugin.getDatabaseManager();
        loadData();
    }

    private void loadData() {
        toggleStates.clear();
        
        databaseManager.reconnectIfNeeded();
        
        databaseManager.loadAllToggleStatesAsync(loadedStates -> {
            toggleStates.putAll(loadedStates);
        });
    }

    public void saveData() {
        databaseManager.reconnectIfNeeded();
        
        for (Map.Entry<UUID, Boolean> entry : toggleStates.entrySet()) {
            databaseManager.saveToggleStateAsync(entry.getKey(), entry.getValue(), null);
        }
    }

    public boolean isTPEnabled(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (toggleStates.containsKey(uuid)) {
            return toggleStates.get(uuid);
        }
        
        boolean enabled = databaseManager.getToggleState(uuid);
        toggleStates.put(uuid, enabled);
        
        databaseManager.getToggleStateAsync(uuid, result -> {
            toggleStates.put(uuid, result);
        });
        
        return enabled;
    }

    public boolean toggleTP(Player player) {
        UUID uuid = player.getUniqueId();
        boolean newState = !isTPEnabled(player);
        
        toggleStates.put(uuid, newState);
        
        databaseManager.saveToggleStateAsync(uuid, newState, null);
        
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
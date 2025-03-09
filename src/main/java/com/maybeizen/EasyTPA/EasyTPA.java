package com.maybeizen.EasyTPA;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.maybeizen.EasyTPA.commands.*;
import com.maybeizen.EasyTPA.utils.ConfigManager;
import com.maybeizen.EasyTPA.managers.TPAManager;
import com.maybeizen.EasyTPA.managers.CooldownManager;
import com.maybeizen.EasyTPA.managers.ToggleManager;
import com.maybeizen.EasyTPA.utils.VersionAdapter;

public class EasyTPA extends JavaPlugin {
    private ConfigManager configManager;
    private TPAManager tpaManager;
    private CooldownManager cooldownManager;
    private ToggleManager toggleManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        this.configManager = ConfigManager.getInstance(this);
        this.tpaManager = new TPAManager(this);
        this.cooldownManager = new CooldownManager(getConfig().getLong("cooldown-seconds", 30));
        this.toggleManager = new ToggleManager(this);
        
        // Register commands
        getCommand("tpa").setExecutor(new TPACommand(this));
        getCommand("tpaccept").setExecutor(new TPAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TPDenyCommand(this));
        getCommand("tptoggle").setExecutor(new TPToggleCommand(this));
        
        // Register admin command with tab completion
        EasyTPACommand adminCommand = new EasyTPACommand(this);
        getCommand("easytpa").setExecutor(adminCommand);
        getCommand("easytpa").setTabCompleter(adminCommand);
        
        // Log startup information
        Bukkit.getLogger().info("EasyTPA has been enabled!");
        Bukkit.getLogger().info("Running on server version: " + VersionAdapter.getServerVersion());
    }   

    @Override
    public void onDisable() {
        if (tpaManager != null) {
            tpaManager.clearAllRequests();
        }
        if (toggleManager != null) {
            toggleManager.cleanup();
        }
        Bukkit.getLogger().info("EasyTPA has been disabled!");
    }

    public void reloadConfiguration() {
        // Reload config.yml
        reloadConfig();
        
        // Reinitialize managers with new config values
        this.configManager = ConfigManager.getInstance(this);
        this.cooldownManager = new CooldownManager(getConfig().getLong("cooldown-seconds", 30));
        
        // Save current toggle states and reinitialize toggle manager
        if (toggleManager != null) {
            toggleManager.saveData();
        }
        this.toggleManager = new ToggleManager(this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TPAManager getTPAManager() {
        return tpaManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ToggleManager getToggleManager() {
        return toggleManager;
    }
}


package com.maybeizen.EasyTPA;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.maybeizen.EasyTPA.commands.*;
import com.maybeizen.EasyTPA.utils.ConfigManager;
import com.maybeizen.EasyTPA.managers.TPAManager;
import com.maybeizen.EasyTPA.managers.CooldownManager;
import com.maybeizen.EasyTPA.managers.ToggleManager;
import com.maybeizen.EasyTPA.utils.MessageUtils;
import com.maybeizen.EasyTPA.utils.VersionAdapter;

public class EasyTPA extends JavaPlugin {
    private ConfigManager configManager;
    private TPAManager tpaManager;
    private CooldownManager cooldownManager;
    private ToggleManager toggleManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        this.configManager = ConfigManager.getInstance(this);
        
        MessageUtils.initialize(this);
        
        this.tpaManager = new TPAManager(this);
        this.cooldownManager = new CooldownManager(configManager.getCooldown());
        this.toggleManager = new ToggleManager(this);
        
        getCommand("tpa").setExecutor(new TPACommand(this));
        getCommand("tpaccept").setExecutor(new TPAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TPDenyCommand(this));
        getCommand("tptoggle").setExecutor(new TPToggleCommand(this));
        
        EasyTPACommand adminCommand = new EasyTPACommand(this);
        getCommand("easytpa").setExecutor(adminCommand);
        getCommand("easytpa").setTabCompleter(adminCommand);
        
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
        if (toggleManager != null) {
            toggleManager.saveData();
        }
        
        reloadConfig();
        
        configManager.reloadConfig();
        
        this.cooldownManager = new CooldownManager(configManager.getCooldown());
        
        if (toggleManager != null) {
            toggleManager.reload();
        } else {
            this.toggleManager = new ToggleManager(this);
        }
        
        MessageUtils.initialize(this);
        
        Bukkit.getLogger().info("EasyTPA configuration has been reloaded!");
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


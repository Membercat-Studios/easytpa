package com.maybeizen.EasyTPA;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.maybeizen.EasyTPA.commands.*;
import com.maybeizen.EasyTPA.utils.ConfigManager;
import com.maybeizen.EasyTPA.utils.DatabaseManager;
import com.maybeizen.EasyTPA.managers.TPAManager;
import com.maybeizen.EasyTPA.managers.CooldownManager;
import com.maybeizen.EasyTPA.managers.ToggleManager;
import com.maybeizen.EasyTPA.utils.MessageUtils;
import com.maybeizen.EasyTPA.utils.VersionAdapter;

public class EasyTPA extends JavaPlugin {
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private TPAManager tpaManager;
    private CooldownManager cooldownManager;
    private ToggleManager toggleManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        this.configManager = ConfigManager.getInstance(this);
        this.databaseManager = new DatabaseManager(this);
        
        MessageUtils.initialize(this);
        
        this.tpaManager = new TPAManager(this);
        this.cooldownManager = new CooldownManager(configManager.getCooldown());
        this.toggleManager = new ToggleManager(this);
        
        getCommand("tpa").setExecutor(new TPACommand(this));
        getCommand("tpaccept").setExecutor(new TPAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TPDenyCommand(this));
        getCommand("tptoggle").setExecutor(new TPToggleCommand(this));
        getCommand("tpcancel").setExecutor(new TPCancelCommand(this));
        getCommand("tplist").setExecutor(new TPListCommand(this));
        
        EasyTPACommand adminCommand = new EasyTPACommand(this);
        getCommand("easytpa").setExecutor(adminCommand);
        getCommand("easytpa").setTabCompleter(adminCommand);
        
        startCleanupTask();
        
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
        if (databaseManager != null) {
            databaseManager.closeConnection();
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
        
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
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
    
    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (cooldownManager != null) {
                    int before = cooldownManager.getActiveCooldownCount();
                    cooldownManager.cleanupExpired();
                    int after = cooldownManager.getActiveCooldownCount();
                    if (before > after) {
                        getLogger().fine("Cleaned up " + (before - after) + " expired cooldowns");
                    }
                }
            }
        }.runTaskTimer(this, 6000L, 6000L); // Run every 5 minutes (6000 ticks)
    }
}


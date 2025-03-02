package com.maybeizen.EasyTPA;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.maybeizen.EasyTPA.commands.TPACommand;
import com.maybeizen.EasyTPA.commands.TPAcceptCommand;
import com.maybeizen.EasyTPA.commands.TPDenyCommand;
import com.maybeizen.EasyTPA.utils.ConfigManager;
import com.maybeizen.EasyTPA.managers.TPAManager;
import com.maybeizen.EasyTPA.managers.CooldownManager;

public class EasyTPA extends JavaPlugin {
    private ConfigManager configManager;
    private TPAManager tpaManager;
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        this.configManager = ConfigManager.getInstance(this);
        this.tpaManager = new TPAManager(this);
        this.cooldownManager = new CooldownManager(getConfig().getLong("cooldown-seconds", 30));
        
        getCommand("tpa").setExecutor(new TPACommand(this));
        getCommand("tpaccept").setExecutor(new TPAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TPDenyCommand(this));
        
        Bukkit.getLogger().info("EasyTPA has been enabled!");
    }   

    @Override
    public void onDisable() {
        if (tpaManager != null) {
            tpaManager.clearAllRequests();
        }
        Bukkit.getLogger().info("EasyTPA has been disabled!");
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
}


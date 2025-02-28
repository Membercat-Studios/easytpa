package com.maybeizen.EasyTPA;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EasyTPA extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("EasyTPA has been enabled!");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("EasyTPA has been disabled!");
    }
}


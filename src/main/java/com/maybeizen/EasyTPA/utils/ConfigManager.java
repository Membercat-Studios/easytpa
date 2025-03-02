package com.maybeizen.EasyTPA.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;

public class ConfigManager {
    private static ConfigManager instance;
    private final Plugin plugin;
    private FileConfiguration config;

    private ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public static ConfigManager getInstance(Plugin plugin) {
        if (instance == null) {
            instance = new ConfigManager(plugin);
        }
        return instance;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public String getMessage(String path) {
        String message = config.getString("messages." + path, "Message not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String path, String... placeholders) {
        if (placeholders.length % 2 != 0) {
            throw new IllegalArgumentException("Placeholders must be in pairs!");
        }

        String message = config.getString("messages." + path, "Message not found: " + path);
        
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public int getRequestTimeout() {
        return config.getInt("settings.request-timeout", 60);
    }

    public int getCooldown() {
        return config.getInt("settings.cooldown", 30);
    }

    public boolean getSoundsEnabled() {
        return config.getBoolean("settings.enable-sounds", true);
    }

    public FileConfiguration getConfig() {
        return config;
    }
} 
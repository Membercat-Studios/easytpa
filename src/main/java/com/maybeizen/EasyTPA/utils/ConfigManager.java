package com.maybeizen.EasyTPA.utils;

import com.maybeizen.EasyTPA.EasyTPA;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ConfigManager {
    private static ConfigManager instance;
    private final EasyTPA plugin;
    private FileConfiguration config;

    private ConfigManager(EasyTPA plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public static ConfigManager getInstance(EasyTPA plugin) {
        if (instance == null) {
            instance = new ConfigManager(plugin);
        } else {
            instance.loadConfig();
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
        this.config = plugin.getConfig();
    }

    public String getMessage(String path) {
        return config.getString("messages." + path, "Message not found: " + path);
    }

    public String getMessage(String path, Player player) {
        return getMessage(path);
    }

    public String getMessage(String path, String... placeholders) {
        if (placeholders.length % 2 != 0) {
            throw new IllegalArgumentException("Placeholders must be in pairs of key-value");
        }

        String message = getMessage(path);
        
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
        }
        
        return message;
    }

    public String getMessage(String path, Player player, String... placeholders) {
        return getMessage(path, placeholders);
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

    public long getCooldownSeconds() {
        return config.getLong("cooldown-seconds", 30);
    }

    public String getDefaultPrefix() {
        return "&7[&6EasyTPA&7] &r";
    }

    public FileConfiguration getConfig() {
        return config;
    }
} 
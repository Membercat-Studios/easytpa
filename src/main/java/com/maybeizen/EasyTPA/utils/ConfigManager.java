package com.maybeizen.EasyTPA.utils;

import com.maybeizen.EasyTPA.EasyTPA;
import org.bukkit.configuration.file.FileConfiguration;

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

    public String getMessage(String path, String... placeholders) {
        String message = getMessage(path);
        return message.contains("%") ? message : message;
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
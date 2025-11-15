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
        String message = config.getString("messages." + path);
        if (message == null || message.isEmpty()) {
            return getDefaultMessage(path);
        }
        return message;
    }
    
    private String getDefaultMessage(String path) {
        switch (path) {
            case "prefix":
                return "&7[&6EasyTPA&7] &r";
            case "player-only":
                return "&cThis command can only be used by players.";
            case "no-permission":
                return "&cYou don't have permission to use this command.";
            case "tpa-usage":
                return "&cUsage: /tpa <player>";
            case "player-not-found":
                return "&cPlayer not found.";
            case "cannot-teleport-self":
                return "&cYou cannot send a teleport request to yourself.";
            case "request-sent":
                return "&aYou have sent a teleport request to &e%player%&a.";
            case "request-received":
                return "&e%player% &ahas requested to teleport to you.";
            case "already-has-request":
                return "&cThis player already has a pending teleport request!";
            case "request-expired":
                return "&cThe teleport request has expired.";
            case "request-expired-target":
                return "&cThe teleport request has expired!";
            case "no-pending-request":
                return "&cYou don't have any pending teleport requests!";
            case "no-pending-requests":
                return "&cYou have no pending teleport requests.";
            case "request-accepted":
                return "&aYour teleport request has been accepted!";
            case "request-accepted-target":
                return "&aYou accepted &e%player%&a's teleport request.";
            case "request-denied":
                return "&cYour teleport request has been denied.";
            case "request-denied-target":
                return "&cYou denied &e%player%&c's teleport request.";
            case "player-offline":
                return "&cThe player who sent the request is no longer online!";
            case "cooldown":
                return "&cYou must wait &e%time% &cseconds before sending another request!";
            case "teleport-success":
                return "&aYou have been teleported to &e%player%&a.";
            case "toggle-enabled":
                return "&aTeleport requests have been &2enabled&a.";
            case "toggle-disabled":
                return "&cTeleport requests have been &4disabled&c.";
            case "target-has-tp-disabled":
                return "&c%player% has teleport requests disabled.";
            case "chat-button-accept":
                return "&a[Accept]";
            case "chat-button-deny":
                return "&c[Deny]";
            case "hover-text-accept":
                return "&aClick to accept the teleport request";
            case "hover-text-deny":
                return "&cClick to deny the teleport request";
            case "config-reloading":
                return "&eReloading EasyTPA configuration...";
            case "config-reloaded":
                return "&aConfiguration has been reloaded successfully!";
            case "teleport-unsafe":
                return "&cCannot teleport: The target location is unsafe!";
            case "teleport-unsafe-target":
                return "&cTeleport cancelled: The location became unsafe!";
            case "teleport-starting":
                return "&eTeleporting in &6%time% &eseconds... Don't move!";
            case "teleport-starting-target":
                return "&e%player% &eis teleporting to you in &6%time% &eseconds.";
            case "teleport-countdown":
                return "&eTeleporting in &6%time% &eseconds...";
            case "teleport-cancelled-moved":
                return "&cTeleport cancelled! You moved.";
            case "teleport-cancelled-moved-target":
                return "&c%player%'s teleport was cancelled (they moved).";
            case "teleport-already-pending":
                return "&cYou already have a teleport in progress!";
            case "teleport-cancelled":
                return "&aTeleport cancelled.";
            case "request-cancelled":
                return "&aYour teleport request has been cancelled.";
            case "nothing-to-cancel":
                return "&cYou have nothing to cancel.";
            case "request-list-header":
                return "&6--- Your Teleport Requests ---";
            case "request-list-received":
                return "&7Received from: &e%player%";
            case "request-list-sent":
                return "&7Sent to: &e%player%";
            default:
                return "&c[EasyTPA] Message not configured: " + path;
        }
    }

    public String getMessage(String path, Player player) {
        return getMessage(path);
    }

    public String getMessage(String path, String... placeholders) {
        if (placeholders.length % 2 != 0) {
            throw new IllegalArgumentException("Placeholders must be in pairs of key-value");
        }

        String message = getMessage(path);
        
        if (placeholders.length == 0) {
            return message;
        }
        
        StringBuilder result = new StringBuilder(message);
        for (int i = 0; i < placeholders.length; i += 2) {
            String placeholder = "%" + placeholders[i] + "%";
            String replacement = placeholders[i + 1];
            int index = 0;
            while ((index = result.indexOf(placeholder, index)) != -1) {
                result.replace(index, index + placeholder.length(), replacement);
                index += replacement.length();
            }
        }
        
        return result.toString();
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
        return config.getLong("settings.cooldown", 30);
    }
    
    public int getTeleportDelay() {
        return config.getInt("settings.teleport-delay", 3);
    }

    public String getDefaultPrefix() {
        return "&7[&6EasyTPA&7] &r";
    }

    public FileConfiguration getConfig() {
        return config;
    }
} 
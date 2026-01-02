package me.maybeizen.EasyTPA.manager;

import me.maybeizen.EasyTPA.EasyTPA;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {
    private final EasyTPA plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private File configFile;
    private File messagesFile;

    public ConfigManager(EasyTPA plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        config = plugin.getConfig();

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveDefaultMessages();
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void saveDefaultMessages() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        if (!messagesFile.exists()) {
            try (InputStream in = plugin.getResource("messages.yml")) {
                if (in != null) {
                    Files.copy(in, messagesFile.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save default messages.yml", e);
            }
        }
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public int getRequestTimeout() {
        return config.getInt("settings.request-timeout", 60);
    }

    public int getCooldown() {
        return config.getInt("settings.cooldown", 30);
    }

    public int getTeleportDelay() {
        return config.getInt("settings.teleport-delay", 3);
    }

    public boolean areSoundsEnabled() {
        return config.getBoolean("settings.enable-sounds", true);
    }

    public boolean captureLocationOnAccept() {
        return config.getBoolean("settings.capture-location-on-accept", true);
    }

    public String getMessage(String path) {
        return messages.getString(path, "&cMessage not found: " + path);
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        return message;
    }

    public String getPrefix() {
        return getMessage("general.prefix", null);
    }
}


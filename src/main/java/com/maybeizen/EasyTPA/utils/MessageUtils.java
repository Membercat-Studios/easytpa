package com.maybeizen.EasyTPA.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import com.maybeizen.EasyTPA.EasyTPA;

import java.util.HashMap;
import java.util.Map;

public class MessageUtils {
    private static EasyTPA plugin;
    
    public static void initialize(EasyTPA pluginInstance) {
        plugin = pluginInstance;
    }
    
    private static String getPrefix() {
        if (plugin == null) {
            return ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "EasyTPA" + ChatColor.DARK_AQUA + "] " + ChatColor.RESET;
        }
        
        String prefix = plugin.getConfigManager().getMessage("prefix");
        if (prefix.equals("Message not found: prefix")) {
            prefix = plugin.getConfigManager().getDefaultPrefix();
        }
        return ChatColor.translateAlternateColorCodes('&', prefix);
    }

    public static String formatMessage(String message) {
        return getPrefix() + ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String formatMessage(String message, String... placeholders) {
        if (placeholders.length % 2 != 0) {
            throw new IllegalArgumentException("Placeholders must be in pairs of key-value");
        }

        Map<String, String> replacements = new HashMap<>();
        for (int i = 0; i < placeholders.length; i += 2) {
            replacements.put(placeholders[i], placeholders[i + 1]);
        }

        String formattedMessage = message;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            formattedMessage = formattedMessage.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        return getPrefix() + ChatColor.translateAlternateColorCodes('&', formattedMessage);
    }

    public static void sendTeleportRequest(Player sender, Player target) {
        String requestMessage = plugin.getConfigManager().getMessage("request-received");
        requestMessage = requestMessage.replace("%player%", sender.getName());
        
        TextComponent message = new TextComponent(getPrefix() + 
            ChatColor.translateAlternateColorCodes('&', requestMessage + "\n"));
        
        TextComponent accept = new TextComponent(ChatColor.GREEN + "[Accept] ");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + sender.getName()));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("Click to accept the teleport request").color(ChatColor.GREEN).create()));

        TextComponent deny = new TextComponent(ChatColor.RED + "[Deny]");
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + sender.getName()));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("Click to deny the teleport request").color(ChatColor.RED).create()));

        target.spigot().sendMessage(message, accept, deny);
        
        if (plugin.getConfigManager().getSoundsEnabled()) {
            VersionAdapter.playRequestSound(target);
        }
    }

    public static void playTeleportEffect(Player player) {
        if (plugin != null && plugin.getConfigManager().getSoundsEnabled()) {
            VersionAdapter.playTeleportSound(player);
        }
    }
} 
package com.maybeizen.EasyTPA.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MessageUtils {
    private static final String PREFIX = ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "EasyTPA" + ChatColor.DARK_AQUA + "] " + ChatColor.RESET;

    public static String formatMessage(String message) {
        return PREFIX + ChatColor.translateAlternateColorCodes('&', message);
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

        return PREFIX + ChatColor.translateAlternateColorCodes('&', formattedMessage);
    }

    public static void sendTeleportRequest(Player sender, Player target) {
        TextComponent message = new TextComponent(formatMessage(sender.getName() + " has requested to teleport to you!\n"));
        
        TextComponent accept = new TextComponent(ChatColor.GREEN + "[Accept] ");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + sender.getName()));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("Click to accept the teleport request").color(ChatColor.GREEN).create()));

        TextComponent deny = new TextComponent(ChatColor.RED + "[Deny]");
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + sender.getName()));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("Click to deny the teleport request").color(ChatColor.RED).create()));

        target.spigot().sendMessage(message, accept, deny);
        VersionAdapter.playRequestSound(target);
    }

    public static void playTeleportEffect(Player player) {
        VersionAdapter.playTeleportSound(player);
    }
} 
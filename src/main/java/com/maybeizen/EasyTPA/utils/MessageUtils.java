package com.maybeizen.EasyTPA.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class MessageUtils {
    private static final String PREFIX = ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "EasyTPA" + ChatColor.DARK_AQUA + "] " + ChatColor.RESET;

    public static String formatMessage(String message) {
        return PREFIX + ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendTeleportRequest(Player sender, Player target) {
        TextComponent message = new TextComponent(PREFIX + ChatColor.YELLOW + sender.getName() + " has requested to teleport to you!\n");
        
        TextComponent accept = new TextComponent(ChatColor.GREEN + "[Accept] ");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + sender.getName()));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("Click to accept the teleport request").color(ChatColor.GREEN).create()));

        TextComponent deny = new TextComponent(ChatColor.RED + "[Deny]");
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + sender.getName()));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("Click to deny the teleport request").color(ChatColor.RED).create()));

        target.spigot().sendMessage(message, accept, deny);
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }

    public static void playTeleportEffect(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }
} 
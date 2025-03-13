package com.maybeizen.EasyTPA.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import com.maybeizen.EasyTPA.EasyTPA;

import java.util.HashMap;
import java.util.Map;

public class MessageUtils {
    private static EasyTPA plugin;
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();
    
    public static void initialize(EasyTPA pluginInstance) {
        plugin = pluginInstance;
    }
    
    private static Component getPrefix() {        
        String prefix = plugin.getConfigManager().getMessage("prefix");
        if (prefix.equals("Message not found: prefix")) {
            prefix = plugin.getConfigManager().getDefaultPrefix();
        }
        return LEGACY_SERIALIZER.deserialize(prefix);
    }

    public static Component formatMessage(String message) {
        return getPrefix().append(LEGACY_SERIALIZER.deserialize(message));
    }

    public static Component formatMessage(String message, String... placeholders) {
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

        return getPrefix().append(LEGACY_SERIALIZER.deserialize(formattedMessage));
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(formatMessage(message));
    }

    public static void sendMessage(Player player, String message, String... placeholders) {
        player.sendMessage(formatMessage(message, placeholders));
    }

    public static void sendTeleportRequest(Player sender, Player target) {
        String requestMessage = plugin.getConfigManager().getMessage("request-received");
        requestMessage = requestMessage.replace("%player%", sender.getName());
        
        Component message = getPrefix().append(LEGACY_SERIALIZER.deserialize(requestMessage))
                .append(Component.newline());
        
        String acceptButtonText = plugin.getConfigManager().getMessage("chat-button-accept");
        String acceptHoverText = plugin.getConfigManager().getMessage("hover-text-accept");
        Component accept = LEGACY_SERIALIZER.deserialize(acceptButtonText)
                .clickEvent(ClickEvent.runCommand("/tpaccept " + sender.getName()))
                .hoverEvent(HoverEvent.showText(LEGACY_SERIALIZER.deserialize(acceptHoverText)));

        String denyButtonText = plugin.getConfigManager().getMessage("chat-button-deny");
        String denyHoverText = plugin.getConfigManager().getMessage("hover-text-deny");
        Component deny = LEGACY_SERIALIZER.deserialize(denyButtonText)
                .clickEvent(ClickEvent.runCommand("/tpdeny " + sender.getName()))
                .hoverEvent(HoverEvent.showText(LEGACY_SERIALIZER.deserialize(denyHoverText)));

        target.sendMessage(message.append(accept).append(Component.space()).append(deny));
        
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
package me.maybeizen.EasyTPA.util;

import me.maybeizen.EasyTPA.EasyTPA;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static Component toComponent(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        
        if (containsLegacyColorCodes(message)) {
            return LEGACY_SERIALIZER.deserialize(message);
        }

        try {
            return MINI_MESSAGE.deserialize(message);
        } catch (Exception e) {
            return LEGACY_SERIALIZER.deserialize(message);
        }
    }

    private static boolean containsLegacyColorCodes(String message) {
        return message.matches(".*&[0-9a-fk-orA-FK-OR].*");
    }

    public static void sendMessage(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        Component component = toComponent(message);
        player.sendMessage(component);
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        Component component = toComponent(message);
        sender.sendMessage(component);
    }

    public static void sendMessageWithPlaceholders(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        if (EasyTPA.getInstance().isPlaceholderAPIEnabled()) {
            message = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, message);
        }

        sendMessage(player, message);
    }

    public static String toLegacyString(Component component) {
        return LEGACY_SERIALIZER.serialize(component);
    }
}


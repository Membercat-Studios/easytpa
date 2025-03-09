package com.maybeizen.EasyTPA.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class VersionAdapter {
    private static final String SERVER_VERSION;
    private static final boolean IS_LEGACY;

    static {
        String packageName = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
        SERVER_VERSION = packageName.substring(packageName.lastIndexOf('.') + 1);
        IS_LEGACY = SERVER_VERSION.startsWith("v1_19");
    }

    public static void playRequestSound(Player player) {
        if (IS_LEGACY) {
            player.playSound(player.getLocation(), Sound.valueOf("BLOCK_NOTE_BLOCK_PLING"), 1.0f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        }
    }

    public static void playTeleportSound(Player player) {
        if (IS_LEGACY) {
            player.playSound(player.getLocation(), Sound.valueOf("ENTITY_ENDERMAN_TELEPORT"), 1.0f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }
    }

    public static String getServerVersion() {
        return SERVER_VERSION;
    }
} 
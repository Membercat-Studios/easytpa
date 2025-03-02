package com.maybeizen.EasyTPA.managers;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.ConfigManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPAManager {
    private final EasyTPA plugin;
    private final Map<UUID, UUID> tpaRequests; // target -> sender
    private final Map<UUID, Long> cooldowns;

    public TPAManager(EasyTPA plugin) {
        this.plugin = plugin;
        this.tpaRequests = new HashMap<>();
        this.cooldowns = new HashMap<>();
    }

    public boolean sendRequest(Player sender, Player target) {
        UUID senderUUID = sender.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (isOnCooldown(sender)) {
            long remainingTime = (cooldowns.get(senderUUID) - System.currentTimeMillis()) / 1000;
            sender.sendMessage(plugin.getConfigManager().getMessage("cooldown", "time", String.valueOf(remainingTime)));
            return false;
        }

        if (tpaRequests.containsKey(targetUUID)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("already-has-request"));
            return false;
        }

        tpaRequests.put(targetUUID, senderUUID);
        
        setCooldown(sender);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (tpaRequests.containsKey(targetUUID) && tpaRequests.get(targetUUID).equals(senderUUID)) {
                    tpaRequests.remove(targetUUID);
                    sender.sendMessage(plugin.getConfigManager().getMessage("request-expired"));
                    if (target.isOnline()) {
                        target.sendMessage(plugin.getConfigManager().getMessage("request-expired-target"));
                    }
                }
            }
        }.runTaskLater(plugin, plugin.getConfigManager().getRequestTimeout() * 20L);

        return true;
    }

    public boolean acceptRequest(Player target) {
        UUID targetUUID = target.getUniqueId();
        if (!tpaRequests.containsKey(targetUUID)) {
            target.sendMessage(plugin.getConfigManager().getMessage("no-pending-request"));
            return false;
        }

        UUID senderUUID = tpaRequests.get(targetUUID);
        Player sender = plugin.getServer().getPlayer(senderUUID);

        if (sender == null || !sender.isOnline()) {
            target.sendMessage(plugin.getConfigManager().getMessage("player-offline"));
            tpaRequests.remove(targetUUID);
            return false;
        }

        sender.teleport(target.getLocation());
        
        tpaRequests.remove(targetUUID);

        return true;
    }

    public boolean denyRequest(Player target) {
        UUID targetUUID = target.getUniqueId();
        if (!tpaRequests.containsKey(targetUUID)) {
            target.sendMessage(plugin.getConfigManager().getMessage("no-pending-request"));
            return false;
        }

        UUID senderUUID = tpaRequests.get(targetUUID);
        Player sender = plugin.getServer().getPlayer(senderUUID);

        tpaRequests.remove(targetUUID);

        if (sender != null && sender.isOnline()) {
            sender.sendMessage(plugin.getConfigManager().getMessage("request-denied"));
        }

        return true;
    }

    public void clearAllRequests() {
        tpaRequests.clear();
        cooldowns.clear();
    }

    private boolean isOnCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        return cooldowns.containsKey(uuid) && cooldowns.get(uuid) > System.currentTimeMillis();
    }

    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (plugin.getConfigManager().getCooldown() * 1000L));
    }
} 
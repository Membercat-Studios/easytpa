package com.maybeizen.EasyTPA.managers;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.ConfigManager;
import com.maybeizen.EasyTPA.utils.MessageUtils;

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
            sender.sendMessage(MessageUtils.formatMessage(
                plugin.getConfigManager().getMessage("cooldown"), 
                "time", String.valueOf(remainingTime)
            ));
            return false;
        }

        if (tpaRequests.containsKey(targetUUID)) {
            sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("already-has-request")));
            return false;
        }

        tpaRequests.put(targetUUID, senderUUID);
        
        setCooldown(sender);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (tpaRequests.containsKey(targetUUID) && tpaRequests.get(targetUUID).equals(senderUUID)) {
                    tpaRequests.remove(targetUUID);
                    sender.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("request-expired")));
                    if (target.isOnline()) {
                        target.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("request-expired-target")));
                    }
                }
            }
        }.runTaskLater(plugin, plugin.getConfigManager().getRequestTimeout() * 20L);

        return true;
    }

    public boolean acceptRequest(Player target) {
        UUID targetUUID = target.getUniqueId();
        if (!tpaRequests.containsKey(targetUUID)) {
            target.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("no-pending-request")));
            return false;
        }

        UUID senderUUID = tpaRequests.get(targetUUID);
        Player sender = plugin.getServer().getPlayer(senderUUID);

        if (sender == null || !sender.isOnline()) {
            target.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("player-offline")));
            tpaRequests.remove(targetUUID);
            return false;
        }

        sender.teleport(target.getLocation());
        
        // Send success messages
        sender.sendMessage(MessageUtils.formatMessage(
            plugin.getConfigManager().getMessage("request-accepted"), 
            "player", target.getName()
        ));
        
        target.sendMessage(MessageUtils.formatMessage(
            plugin.getConfigManager().getMessage("request-accepted-target"), 
            "player", sender.getName()
        ));
        
        // Play teleport effect
        MessageUtils.playTeleportEffect(sender);
        
        tpaRequests.remove(targetUUID);

        return true;
    }

    /**
     * Denies the most recent teleport request
     * @param target The player denying the request
     * @return The name of the player who sent the request, or null if no request exists
     */
    public String denyRequest(Player target) {
        UUID targetUUID = target.getUniqueId();
        if (!tpaRequests.containsKey(targetUUID)) {
            return null;
        }

        UUID senderUUID = tpaRequests.get(targetUUID);
        Player sender = plugin.getServer().getPlayer(senderUUID);
        String senderName = sender != null ? sender.getName() : "Unknown";

        tpaRequests.remove(targetUUID);

        if (sender != null && sender.isOnline()) {
            sender.sendMessage(MessageUtils.formatMessage(
                plugin.getConfigManager().getMessage("request-denied"), 
                "player", target.getName()
            ));
        }

        return senderName;
    }
    
    /**
     * Denies a teleport request from a specific player
     * @param target The player denying the request
     * @param requester The player who sent the request
     * @return true if the request was denied, false if no request exists
     */
    public boolean denyRequestFrom(Player target, Player requester) {
        UUID targetUUID = target.getUniqueId();
        UUID requesterUUID = requester.getUniqueId();
        
        if (!tpaRequests.containsKey(targetUUID) || !tpaRequests.get(targetUUID).equals(requesterUUID)) {
            target.sendMessage(MessageUtils.formatMessage(plugin.getConfigManager().getMessage("no-pending-request")));
            return false;
        }
        
        tpaRequests.remove(targetUUID);
        
        requester.sendMessage(MessageUtils.formatMessage(
            plugin.getConfigManager().getMessage("request-denied"), 
            "player", target.getName()
        ));
        
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
        UUID uuid = player.getUniqueId();
        cooldowns.put(uuid, System.currentTimeMillis() + (plugin.getConfigManager().getCooldown() * 1000L));
    }
} 
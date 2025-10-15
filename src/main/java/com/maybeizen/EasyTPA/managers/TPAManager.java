package com.maybeizen.EasyTPA.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.bukkit.scheduler.BukkitRunnable;
import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.MessageUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPAManager {
    private final EasyTPA plugin;
    private final Map<UUID, TPARequest> tpaRequests; // target > TPARequest
    private final Map<UUID, Long> cooldowns;

    public TPAManager(EasyTPA plugin) {
        this.plugin = plugin;
        this.tpaRequests = new HashMap<>();
        this.cooldowns = new HashMap<>();
    }

    public boolean sendRequest(Player sender, Player target) {
        UUID senderUUID = sender.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (!plugin.getToggleManager().isTPEnabled(target) && !sender.hasPermission("easytpa.bypass")) {
            MessageUtils.sendMessage(sender,
                plugin.getConfigManager().getMessage("target-has-tp-disabled"), 
                "player", target.getName()
            );
            return false;
        }

        if (!sender.hasPermission("easytpa.cooldown.bypass") && isOnCooldown(sender)) {
            long remainingTime = (cooldowns.get(senderUUID) - System.currentTimeMillis()) / 1000;
            MessageUtils.sendMessage(sender,
                plugin.getConfigManager().getMessage("cooldown"), 
                "time", String.valueOf(remainingTime)
            );
            return false;
        }

        if (tpaRequests.containsKey(targetUUID)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("already-has-request"));
            return false;
        }

        TPARequest request = new TPARequest(senderUUID, targetUUID, target.getLocation());
        tpaRequests.put(targetUUID, request);
        
        if (!sender.hasPermission("easytpa.cooldown.bypass")) {
            setCooldown(sender);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                TPARequest request = tpaRequests.get(targetUUID);
                if (request != null && request.isFrom(senderUUID)) {
                    tpaRequests.remove(targetUUID);
                    MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("request-expired"));
                    if (target.isOnline()) {
                        MessageUtils.sendMessage(target, plugin.getConfigManager().getMessage("request-expired-target"));
                    }
                }
            }
        }.runTaskLater(plugin, plugin.getConfigManager().getRequestTimeout() * 20L);

        return true;
    }

    public boolean acceptRequest(Player target) {
        UUID targetUUID = target.getUniqueId();
        TPARequest request = tpaRequests.get(targetUUID);
        if (request == null) {
            MessageUtils.sendMessage(target, plugin.getConfigManager().getMessage("no-pending-request"));
            return false;
        }

        Player sender = plugin.getServer().getPlayer(request.getSenderUUID());
        if (sender == null || !sender.isOnline()) {
            MessageUtils.sendMessage(target, plugin.getConfigManager().getMessage("player-offline"));
            tpaRequests.remove(targetUUID);
            return false;
        }

        return performTeleport(sender, request.getTargetLocation(), target);
    }
    
    public boolean acceptRequestFrom(Player target, Player requester) {
        UUID targetUUID = target.getUniqueId();
        UUID requesterUUID = requester.getUniqueId();
        
        TPARequest request = tpaRequests.get(targetUUID);
        if (request == null || !request.isFrom(requesterUUID)) {
            MessageUtils.sendMessage(target, plugin.getConfigManager().getMessage("no-pending-request"));
            return false;
        }
        
        return performTeleport(requester, request.getTargetLocation(), target);
    }

    private boolean performTeleport(Player sender, Location targetLocation, Player target) {
        sender.teleport(targetLocation);
        
        MessageUtils.sendMessage(sender,
            plugin.getConfigManager().getMessage("request-accepted"), 
            "player", target.getName()
        );
        
        MessageUtils.sendMessage(target,
            plugin.getConfigManager().getMessage("request-accepted-target"), 
            "player", sender.getName()
        );
        
        MessageUtils.playTeleportEffect(sender);
        
        tpaRequests.remove(target.getUniqueId());
        
        return true;
    }

    public String denyRequest(Player target) {
        UUID targetUUID = target.getUniqueId();
        TPARequest request = tpaRequests.get(targetUUID);
        if (request == null) {
            return null;
        }

        Player sender = plugin.getServer().getPlayer(request.getSenderUUID());
        String senderName = sender != null ? sender.getName() : "Unknown";

        tpaRequests.remove(targetUUID);

        if (sender != null && sender.isOnline()) {
            MessageUtils.sendMessage(sender,
                plugin.getConfigManager().getMessage("request-denied"), 
                "player", target.getName()
            );
        }

        return senderName;
    }
    
    public boolean denyRequestFrom(Player target, Player requester) {
        UUID targetUUID = target.getUniqueId();
        UUID requesterUUID = requester.getUniqueId();
        
        TPARequest request = tpaRequests.get(targetUUID);
        if (request == null || !request.isFrom(requesterUUID)) {
            MessageUtils.sendMessage(target, plugin.getConfigManager().getMessage("no-pending-request"));
            return false;
        }
        
        tpaRequests.remove(targetUUID);
        
        MessageUtils.sendMessage(requester,
            plugin.getConfigManager().getMessage("request-denied"), 
            "player", target.getName()
        );
        
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
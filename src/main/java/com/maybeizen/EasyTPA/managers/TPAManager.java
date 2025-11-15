package com.maybeizen.EasyTPA.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.bukkit.scheduler.BukkitRunnable;
import com.maybeizen.EasyTPA.EasyTPA;
import com.maybeizen.EasyTPA.utils.MessageUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TPAManager {
    private final EasyTPA plugin;
    private final Map<UUID, TPARequest> tpaRequests; // target > TPARequest
    private final Map<UUID, BukkitRunnable> pendingTeleports; // sender > teleport task

    public TPAManager(EasyTPA plugin) {
        this.plugin = plugin;
        this.tpaRequests = new ConcurrentHashMap<>();
        this.pendingTeleports = new ConcurrentHashMap<>();
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

        if (!sender.hasPermission("easytpa.cooldown.bypass") && 
            plugin.getCooldownManager().hasCooldown(senderUUID)) {
            long remainingTime = plugin.getCooldownManager().getRemainingTime(senderUUID) / 1000;
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
            plugin.getCooldownManager().setCooldown(senderUUID);
        }

        UUID finalTargetUUID = targetUUID;
        new BukkitRunnable() {
            @Override
            public void run() {
                TPARequest request = tpaRequests.get(finalTargetUUID);
                if (request != null && request.isFrom(senderUUID)) {
                    tpaRequests.remove(finalTargetUUID);
                    Player senderPlayer = plugin.getServer().getPlayer(senderUUID);
                    Player targetPlayer = plugin.getServer().getPlayer(finalTargetUUID);
                    if (senderPlayer != null && senderPlayer.isOnline()) {
                        MessageUtils.sendMessage(senderPlayer, plugin.getConfigManager().getMessage("request-expired"));
                    }
                    if (targetPlayer != null && targetPlayer.isOnline()) {
                        MessageUtils.sendMessage(targetPlayer, plugin.getConfigManager().getMessage("request-expired-target"));
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
        UUID senderUUID = sender.getUniqueId();
        
        if (pendingTeleports.containsKey(senderUUID)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("teleport-already-pending"));
            return false;
        }
        
        if (!isSafeLocation(targetLocation)) {
            MessageUtils.sendMessage(sender, 
                plugin.getConfigManager().getMessage("teleport-unsafe", "player", target.getName()));
            MessageUtils.sendMessage(target,
                plugin.getConfigManager().getMessage("teleport-unsafe-target", "player", sender.getName()));
            tpaRequests.remove(target.getUniqueId());
            return false;
        }
        
        int delay = plugin.getConfigManager().getTeleportDelay();
        tpaRequests.remove(target.getUniqueId());
        
        if (delay <= 0 || sender.hasPermission("easytpa.delay.bypass")) {
            executeTeleport(sender, targetLocation, target);
            return true;
        }
        
        MessageUtils.sendMessage(sender,
            plugin.getConfigManager().getMessage("teleport-starting", "time", String.valueOf(delay)));
        MessageUtils.sendMessage(target,
            plugin.getConfigManager().getMessage("teleport-starting-target", "player", sender.getName(), "time", String.valueOf(delay)));
        
        Location startLocation = sender.getLocation().clone();
        BukkitRunnable teleportTask = new BukkitRunnable() {
            private int countdown = delay;
            
            @Override
            public void run() {
                if (!sender.isOnline() || !target.isOnline()) {
                    cancelTeleport(senderUUID);
                    return;
                }
                
                if (!sender.getLocation().getWorld().equals(startLocation.getWorld()) ||
                    sender.getLocation().distanceSquared(startLocation) > 1.0) {
                    cancelTeleport(senderUUID);
                    MessageUtils.sendMessage(sender, plugin.getConfigManager().getMessage("teleport-cancelled-moved"));
                    MessageUtils.sendMessage(target, 
                        plugin.getConfigManager().getMessage("teleport-cancelled-moved-target", "player", sender.getName()));
                    return;
                }
                
                countdown--;
                if (countdown <= 0) {
                    executeTeleport(sender, targetLocation, target);
                    pendingTeleports.remove(senderUUID);
                    this.cancel();
                } else if (countdown <= 3) {
                    MessageUtils.sendMessage(sender,
                        plugin.getConfigManager().getMessage("teleport-countdown", "time", String.valueOf(countdown)));
                }
            }
        };
        
        pendingTeleports.put(senderUUID, teleportTask);
        teleportTask.runTaskTimer(plugin, 0L, 20L);
        
        return true;
    }
    
    private void executeTeleport(Player sender, Location targetLocation, Player target) {
        if (!isSafeLocation(targetLocation)) {
            MessageUtils.sendMessage(sender, 
                plugin.getConfigManager().getMessage("teleport-unsafe", "player", target.getName()));
            MessageUtils.sendMessage(target,
                plugin.getConfigManager().getMessage("teleport-unsafe-target", "player", sender.getName()));
            return;
        }
        
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
    }
    
    public boolean cancelTeleport(UUID senderUUID) {
        BukkitRunnable task = pendingTeleports.remove(senderUUID);
        if (task != null) {
            task.cancel();
            return true;
        }
        return false;
    }
    
    private boolean isSafeLocation(Location location) {
        if (location == null) {
            return false;
        }
        
        org.bukkit.block.Block blockAtFeet = location.getBlock();
        org.bukkit.block.Block blockAtHead = location.clone().add(0, 1, 0).getBlock();
        
        if (blockAtFeet.getType().isSolid() || blockAtHead.getType().isSolid()) {
            return false;
        }
        
        org.bukkit.block.Block blockBelow = location.clone().subtract(0, 1, 0).getBlock();
        if (blockBelow.getType().isAir()) {
            return false;
        }
        
        return true;
    }
    
    public TPARequest getRequestBySender(UUID senderUUID) {
        for (TPARequest request : tpaRequests.values()) {
            if (request.isFrom(senderUUID)) {
                return request;
            }
        }
        return null;
    }
    
    public boolean cancelRequestBySender(UUID senderUUID) {
        TPARequest request = getRequestBySender(senderUUID);
        if (request != null) {
            tpaRequests.remove(request.getTargetUUID());
            return true;
        }
        return false;
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
        for (BukkitRunnable task : pendingTeleports.values()) {
            task.cancel();
        }
        pendingTeleports.clear();
    }
    
    public java.util.List<TPARequest> getRequestsForPlayer(UUID playerUUID) {
        java.util.List<TPARequest> requests = new java.util.ArrayList<>();
        for (TPARequest request : tpaRequests.values()) {
            if (request.getTargetUUID().equals(playerUUID) || request.isFrom(playerUUID)) {
                requests.add(request);
            }
        }
        return requests;
    }
} 
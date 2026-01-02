package me.maybeizen.EasyTPA.manager;

import me.maybeizen.EasyTPA.EasyTPA;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportRequestManager {
    private final EasyTPA plugin;
    private final DatabaseManager database;
    
    // active requests: requester > target
    private final Map<UUID, UUID> activeRequests = new ConcurrentHashMap<>();
    // pending teleports: player > teleport info
    private final Map<UUID, PendingTeleport> pendingTeleports = new ConcurrentHashMap<>();
    // cooldowns: player > last request time
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public TeleportRequestManager(EasyTPA plugin, DatabaseManager database) {
        this.plugin = plugin;
        this.database = database;
    }

    public CompletableFuture<RequestResult> sendRequest(Player requester, Player target) {
        UUID requesterId = requester.getUniqueId();
        UUID targetId = target.getUniqueId();

        if (activeRequests.containsKey(requesterId) && activeRequests.get(requesterId).equals(targetId)) {
            return CompletableFuture.completedFuture(RequestResult.ALREADY_HAS_REQUEST);
        }

        if (!requester.hasPermission("easytpa.cooldown.bypass")) {
            long lastRequest = cooldowns.getOrDefault(requesterId, 0L);
            long cooldownMs = plugin.getConfigManager().getCooldown() * 1000L;
            if (System.currentTimeMillis() - lastRequest < cooldownMs) {
                return CompletableFuture.completedFuture(RequestResult.ON_COOLDOWN);
            }
        }

        return database.areRequestsEnabled(targetId).thenCompose(enabled -> {
            if (!enabled && !requester.hasPermission("easytpa.bypass")) {
                return CompletableFuture.completedFuture(RequestResult.REQUESTS_DISABLED);
            }

            long requestTime = System.currentTimeMillis();
            activeRequests.put(requesterId, targetId);
            cooldowns.put(requesterId, requestTime);
            
            database.updateLastRequestTime(requesterId, requestTime);
            
            return CompletableFuture.completedFuture(RequestResult.SUCCESS);
        });
    }

    public enum RequestResult {
        SUCCESS,
        ALREADY_HAS_REQUEST,
        ON_COOLDOWN,
        REQUESTS_DISABLED
    }

    public CompletableFuture<Boolean> acceptRequest(Player accepter, UUID requesterId) {
        UUID accepterId = accepter.getUniqueId();
        
        if (!activeRequests.containsKey(requesterId) || !activeRequests.get(requesterId).equals(accepterId)) {
            return CompletableFuture.completedFuture(false);
        }

        Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null || !requester.isOnline()) {
            activeRequests.remove(requesterId);
            return CompletableFuture.completedFuture(false);
        }

        activeRequests.remove(requesterId);
        
        final boolean captureLocation = plugin.getConfigManager().captureLocationOnAccept();
        final Location capturedDestination = captureLocation ? accepter.getLocation().clone() : null;
        final UUID accepterIdForValidation = accepter.getUniqueId();
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player requesterNow = Bukkit.getPlayer(requesterId);
            Player accepterNow = Bukkit.getPlayer(accepterIdForValidation);
            
            if (requesterNow == null || !requesterNow.isOnline()) {
                return;
            }
            
            Location teleportDestination;
            if (captureLocation && capturedDestination != null) {
                if (capturedDestination.getWorld() != null && Bukkit.getWorld(capturedDestination.getWorld().getUID()) != null) {
                    teleportDestination = capturedDestination;
                } else {
                    if (accepterNow != null && accepterNow.isOnline()) {
                        teleportDestination = accepterNow.getLocation();
                    } else {
                        return;
                    }
                }
            } else {
                if (accepterNow != null && accepterNow.isOnline()) {
                    teleportDestination = accepterNow.getLocation();
                } else {
                    return;
                }
            }
            
            teleportPlayer(requesterNow, teleportDestination);
        });
        
        return CompletableFuture.completedFuture(true);
    }

    public CompletableFuture<Boolean> denyRequest(Player denier, UUID requesterId) {
        UUID denierId = denier.getUniqueId();
        
        if (!activeRequests.containsKey(requesterId) || !activeRequests.get(requesterId).equals(denierId)) {
            return CompletableFuture.completedFuture(false);
        }

        activeRequests.remove(requesterId);
        return CompletableFuture.completedFuture(true);
    }

    public boolean cancelRequest(UUID requesterId) {
        if (activeRequests.containsKey(requesterId)) {
            activeRequests.remove(requesterId);
            return true;
        }
        return false;
    }

    public void teleportPlayer(Player player, Location destination) {
        UUID playerId = player.getUniqueId();
        
        if (pendingTeleports.containsKey(playerId)) {
            return;
        }

        int delay = plugin.getConfigManager().getTeleportDelay();
        if (delay > 0 && !player.hasPermission("easytpa.delay.bypass")) {
            Location startLocation = player.getLocation().clone();
            PendingTeleport pending = new PendingTeleport(player, destination, startLocation, delay);
            pendingTeleports.put(playerId, pending);
            
            java.util.Map<String, String> placeholders = new java.util.HashMap<>();
            placeholders.put("time", String.valueOf(delay));
            me.maybeizen.EasyTPA.util.MessageUtil.sendMessageWithPlaceholders(
                player,
                plugin.getConfigManager().getPrefix() + 
                plugin.getConfigManager().getMessage("teleport.starting", placeholders)
            );
            
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                PendingTeleport tp = pendingTeleports.get(playerId);
                if (tp == null || !tp.isValid()) {
                    pendingTeleports.remove(playerId);
                    return;
                }
                
                tp.tick();
            }, 20L, 20L); 
            
            pending.setTask(task);
        } else {
            performTeleport(player, destination);
        }
    }

    private void performTeleport(Player player, Location destination) {
        if (destination == null) {
            return;
        }
        
        if (destination.getWorld() == null || Bukkit.getWorld(destination.getWorld().getUID()) == null) {
            return;
        }
            
        if (!player.isOnline()) {
            return;
        }
        
        if (!isSafeLocation(destination)) {
            return;
        }
        
        player.teleportAsync(destination).thenAccept(success -> {
            if (success && plugin.getConfigManager().areSoundsEnabled()) {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
        });
    }

    private boolean isSafeLocation(Location location) {
        org.bukkit.block.Block feetBlock = location.getBlock();
        org.bukkit.block.Block headBlock = location.clone().add(0, 1, 0).getBlock();
        org.bukkit.block.Block groundBlock = location.clone().subtract(0, 1, 0).getBlock();
        
        boolean feetSafe = feetBlock.getType().isAir() || !feetBlock.getType().isSolid();
        boolean headSafe = headBlock.getType().isAir() || !headBlock.getType().isSolid();
        
        boolean hasGround = groundBlock.getType().isSolid();
        
        return feetSafe && headSafe && hasGround;
    }

    public void cancelTeleport(UUID playerId) {
        PendingTeleport pending = pendingTeleports.remove(playerId);
        if (pending != null) {
            pending.cancel();
        }
    }

    public Set<UUID> getPendingTeleports() {
        return new HashSet<>(pendingTeleports.keySet());
    }

    public List<UUID> getPendingRequestsFor(UUID playerId) {
        List<UUID> requests = new ArrayList<>();
        for (Map.Entry<UUID, UUID> entry : activeRequests.entrySet()) {
            if (entry.getValue().equals(playerId)) {
                requests.add(entry.getKey());
            }
        }
        return requests;
    }

    public List<UUID> getSentRequestsBy(UUID playerId) {
        List<UUID> requests = new ArrayList<>();
        if (activeRequests.containsKey(playerId)) {
            requests.add(activeRequests.get(playerId));
        }
        return requests;
    }

    public long getCooldown(UUID playerId) {
        return cooldowns.getOrDefault(playerId, 0L);
    }

    public void cleanupExpiredRequests() {
        List<UUID> toRemove = new ArrayList<>();
        
        for (UUID requesterId : toRemove) {
            activeRequests.remove(requesterId);
        }
    }

    public static class PendingTeleport {
        private final Player player;
        private final Location destination;
        private final Location startLocation;
        private int remainingSeconds;
        private BukkitTask task;

        public PendingTeleport(Player player, Location destination, Location startLocation, int delaySeconds) {
            this.player = player;
            this.destination = destination;
            this.startLocation = startLocation;
            this.remainingSeconds = delaySeconds;
        }

        public void tick() {
            if (!player.isOnline()) {
                cancel();
                return;
            }

            Location current = player.getLocation();
            if (current.getWorld() != startLocation.getWorld() ||
                current.distance(startLocation) > 0.5) {
                cancel();
                EasyTPA plugin = EasyTPA.getInstance();
                me.maybeizen.EasyTPA.util.MessageUtil.sendMessageWithPlaceholders(
                    player, 
                    plugin.getConfigManager().getPrefix() + 
                    plugin.getConfigManager().getMessage("teleport.cancelled-moved")
                );
                return;
            }

            EasyTPA plugin = EasyTPA.getInstance();
            if (remainingSeconds > 0) {
                java.util.Map<String, String> placeholders = new java.util.HashMap<>();
                placeholders.put("time", String.valueOf(remainingSeconds));
                me.maybeizen.EasyTPA.util.MessageUtil.sendMessageWithPlaceholders(
                    player,
                    plugin.getConfigManager().getPrefix() + 
                    plugin.getConfigManager().getMessage("teleport.countdown", placeholders)
                );
            }

            remainingSeconds--;
            if (remainingSeconds <= 0) {
                complete();
            }
        }

        private void complete() {
            if (task != null) {
                task.cancel();
            }
            TeleportRequestManager manager = EasyTPA.getInstance().getTeleportManager();
            manager.removePendingTeleport(player.getUniqueId());
            manager.performTeleportDirect(player, destination);
        }

        public void cancel() {
            if (task != null) {
                task.cancel();
            }
            EasyTPA.getInstance().getTeleportManager().removePendingTeleport(player.getUniqueId());
            if (player.isOnline()) {
                EasyTPA plugin = EasyTPA.getInstance();
                me.maybeizen.EasyTPA.util.MessageUtil.sendMessageWithPlaceholders(
                    player,
                    plugin.getConfigManager().getPrefix() + 
                    plugin.getConfigManager().getMessage("teleport.cancelled")
                );
            }
        }

        public boolean isValid() {
            return player.isOnline() && remainingSeconds > 0;
        }

        public void setTask(BukkitTask task) {
            this.task = task;
        }

        public int getRemainingSeconds() {
            return remainingSeconds;
        }
    }

    public void performTeleportDirect(Player player, Location destination) {
        performTeleport(player, destination);
    }

    public void removePendingTeleport(UUID playerId) {
        pendingTeleports.remove(playerId);
    }
}


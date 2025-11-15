package com.maybeizen.EasyTPA.managers;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class CooldownManager {
    private final ConcurrentHashMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final long cooldownTime; // time in ms

    public CooldownManager(long cooldownSeconds) {
        this.cooldownTime = cooldownSeconds * 1000;
    }

    public boolean hasCooldown(UUID playerId) {
        Long expiryTime = cooldowns.get(playerId);
        if (expiryTime == null) {
            return false;
        }
        
        long timeLeft = expiryTime - System.currentTimeMillis();
        if (timeLeft <= 0) {
            cooldowns.remove(playerId);
            return false;
        }
        return true;
    }

    public long getRemainingTime(UUID playerId) {
        Long expiryTime = cooldowns.get(playerId);
        if (expiryTime == null) {
            return 0;
        }
        long remaining = expiryTime - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    public void setCooldown(UUID playerId) {
        cooldowns.put(playerId, System.currentTimeMillis() + cooldownTime);
    }

    public String getRemainingTimeString(UUID playerId) {
        long timeLeft = getRemainingTime(playerId) / 1000;
        return timeLeft + " seconds";
    }
    
    public void cleanupExpired() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Long>> iterator = cooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (entry.getValue() <= currentTime) {
                iterator.remove();
            }
        }
    }
    
    public int getActiveCooldownCount() {
        return cooldowns.size();
    }
} 
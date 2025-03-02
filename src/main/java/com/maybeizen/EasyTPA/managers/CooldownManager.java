package com.maybeizen.EasyTPA.managers;

import java.util.HashMap;
import java.util.UUID;

public class CooldownManager {
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final long cooldownTime; // cooldown in milliseconds

    public CooldownManager(long cooldownSeconds) {
        this.cooldownTime = cooldownSeconds * 1000;
    }

    public boolean hasCooldown(UUID playerId) {
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        
        long timeLeft = getRemainingTime(playerId);
        if (timeLeft <= 0) {
            cooldowns.remove(playerId);
            return false;
        }
        return true;
    }

    public long getRemainingTime(UUID playerId) {
        return cooldowns.containsKey(playerId) ? 
            cooldowns.get(playerId) - System.currentTimeMillis() : 
            0;
    }

    public void setCooldown(UUID playerId) {
        cooldowns.put(playerId, System.currentTimeMillis() + cooldownTime);
    }

    public String getRemainingTimeString(UUID playerId) {
        long timeLeft = getRemainingTime(playerId) / 1000;
        return timeLeft + " seconds";
    }
} 
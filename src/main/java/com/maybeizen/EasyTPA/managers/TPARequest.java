package com.maybeizen.EasyTPA.managers;

import org.bukkit.Location;
import java.util.UUID;


public class TPARequest {
    private final UUID senderUUID;
    private final UUID targetUUID;
    private final Location targetLocation;
    private final long requestTime;

    public TPARequest(UUID senderUUID, UUID targetUUID, Location targetLocation) {
        this.senderUUID = senderUUID;
        this.targetUUID = targetUUID;
        this.targetLocation = targetLocation.clone();
        this.requestTime = System.currentTimeMillis();
    }

    public UUID getSenderUUID() {
        return senderUUID;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public Location getTargetLocation() {
        return targetLocation.clone();
    }

    public long getRequestTime() {
        return requestTime;
    }
    public boolean isFrom(UUID senderUUID) {
        return this.senderUUID.equals(senderUUID);
    }

    public boolean isFor(UUID targetUUID) {
        return this.targetUUID.equals(targetUUID);
    }
}

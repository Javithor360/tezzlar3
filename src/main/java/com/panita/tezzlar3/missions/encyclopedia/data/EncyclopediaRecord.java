package com.panita.tezzlar3.missions.encyclopedia.data;

import org.bukkit.entity.EntityType;

public class EncyclopediaRecord {
    public enum Status {
        APPROVED,
        REJECTED
    }

    private final EntityType mobType;
    private final String deathMethod;
    private final String killerName;
    private final long timestamp;
    private final String dimension;
    private Status status;

    public EncyclopediaRecord(EntityType mobType, String deathMethod, String killerName, long timestamp, String dimension, Status status) {
        this.mobType = mobType;
        this.deathMethod = deathMethod;
        this.killerName = killerName;
        this.timestamp = timestamp;
        this.dimension = dimension;
        this.status = status;
    }

    public EntityType getMobType() {
        return mobType;
    }

    public String getDeathMethod() {
        return deathMethod;
    }

    public String getKillerName() {
        return killerName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDimension() {
        return dimension;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

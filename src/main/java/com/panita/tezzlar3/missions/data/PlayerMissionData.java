package com.panita.tezzlar3.missions.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerMissionData {
    private final UUID uuid;
    private final String name;
    
    // Mission ID -> Progress amount
    private final Map<String, Integer> activeProgress = new HashMap<>();
    
    // Set of completed mission IDs
    private final Set<String> completedMissions = new HashSet<>();
    
    // Set of active punishment IDs (e.g. "DIAMOND_BAN")
    private final Set<String> activePunishments = new HashSet<>();
    
    private long playtimeTicks = 0;

    public PlayerMissionData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Map<String, Integer> getActiveProgress() {
        return activeProgress;
    }
    
    public int getProgress(String missionId) {
        return activeProgress.getOrDefault(missionId, 0);
    }
    
    public void setProgress(String missionId, int amount) {
        activeProgress.put(missionId, amount);
    }
    
    public void addProgress(String missionId, int amount) {
        setProgress(missionId, getProgress(missionId) + amount);
    }

    public Set<String> getCompletedMissions() {
        return completedMissions;
    }

    public boolean hasCompleted(String missionId) {
        return completedMissions.contains(missionId);
    }
    
    public void addCompletedMission(String missionId) {
        completedMissions.add(missionId);
        activeProgress.remove(missionId); // Clear progress when completed
    }

    public Set<String> getActivePunishments() {
        return activePunishments;
    }
    
    public boolean hasPunishment(String punishmentId) {
        return activePunishments.contains(punishmentId);
    }
    
    public void addPunishment(String punishmentId) {
        activePunishments.add(punishmentId);
    }
    
    public void removePunishment(String punishmentId) {
        activePunishments.remove(punishmentId);
    }

    public long getPlaytimeTicks() {
        return playtimeTicks;
    }

    public void setPlaytimeTicks(long playtimeTicks) {
        this.playtimeTicks = playtimeTicks;
    }
    
    public void addPlaytimeTicks(long ticks) {
        this.playtimeTicks += ticks;
    }
}

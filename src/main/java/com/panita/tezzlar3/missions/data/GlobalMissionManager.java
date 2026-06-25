package com.panita.tezzlar3.missions.data;

import com.panita.tezzlar3.core.config.CustomConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class GlobalMissionManager {
    private final CustomConfig config;
    private final Map<String, Integer> globalProgress = new HashMap<>();

    public GlobalMissionManager(JavaPlugin plugin) {
        this.config = new CustomConfig(plugin, "data", "global_missions.yml");
        load();
    }

    public void load() {
        globalProgress.clear();
        FileConfiguration fileConfig = config.getConfig();
        if (fileConfig.contains("progress")) {
            for (String key : fileConfig.getConfigurationSection("progress").getKeys(false)) {
                globalProgress.put(key, fileConfig.getInt("progress." + key));
            }
        }
    }

    public void save() {
        FileConfiguration fileConfig = config.getConfig();
        fileConfig.set("progress", null); // Clear old to avoid orphans
        for (Map.Entry<String, Integer> entry : globalProgress.entrySet()) {
            fileConfig.set("progress." + entry.getKey(), entry.getValue());
        }
        config.save();
    }

    public int getProgress(String missionId) {
        return globalProgress.getOrDefault(missionId, 0);
    }

    public void setProgress(String missionId, int amount) {
        globalProgress.put(missionId, amount);
        save();
    }

    public void addProgress(String missionId, int amount) {
        setProgress(missionId, getProgress(missionId) + amount);
    }
    
    public void resetProgress(String missionId) {
        if (globalProgress.containsKey(missionId)) {
            globalProgress.remove(missionId);
            save();
        }
    }
}

package com.panita.tezzlar3.missions.data;

import com.panita.tezzlar3.core.config.CustomConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private final JavaPlugin plugin;
    private final Map<UUID, PlayerMissionData> cachedData = new HashMap<>();

    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public PlayerMissionData getPlayerData(Player player) {
        return cachedData.get(player.getUniqueId());
    }

    public void loadPlayerData(Player player) {
        CustomConfig customConfig = new CustomConfig(plugin, "data", player.getName() + ".yml");
        FileConfiguration config = customConfig.getConfig();

        PlayerMissionData data = new PlayerMissionData(player.getUniqueId(), player.getName());

        data.setPlaytimeTicks(config.getLong("playtime_ticks", 0));
        
        if (config.contains("active_progress")) {
            for (String key : config.getConfigurationSection("active_progress").getKeys(false)) {
                data.setProgress(key, config.getInt("active_progress." + key));
            }
        }
        
        if (config.contains("completed_missions")) {
            for (String mission : config.getStringList("completed_missions")) {
                data.addCompletedMission(mission);
            }
        }
        
        if (config.contains("active_punishments")) {
            for (String punishment : config.getStringList("active_punishments")) {
                data.addPunishment(punishment);
            }
        }
        
        if (config.contains("pending_rewards")) {
            for (String reward : config.getStringList("pending_rewards")) {
                data.addPendingReward(reward);
            }
        }

        cachedData.put(player.getUniqueId(), data);
    }

    public void savePlayerData(Player player) {
        PlayerMissionData data = cachedData.get(player.getUniqueId());
        if (data == null) return;

        CustomConfig customConfig = new CustomConfig(plugin, "data", player.getName() + ".yml");
        FileConfiguration config = customConfig.getConfig();

        config.set("uuid", data.getUuid().toString());
        config.set("name", data.getName());
        config.set("playtime_ticks", data.getPlaytimeTicks());

        for (Map.Entry<String, Integer> entry : data.getActiveProgress().entrySet()) {
            config.set("active_progress." + entry.getKey(), entry.getValue());
        }

        config.set("completed_missions", new ArrayList<>(data.getCompletedMissions()));
        config.set("active_punishments", new ArrayList<>(data.getActivePunishments()));
        config.set("pending_rewards", new ArrayList<>(data.getPendingRewards()));

        customConfig.save();
    }
    
    public void unloadPlayerData(Player player) {
        savePlayerData(player);
        cachedData.remove(player.getUniqueId());
    }
    
    public void saveAll() {
        for (UUID uuid : cachedData.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                savePlayerData(player);
            }
        }
    }

    public void giveRewardToEveryone(String missionId) {
        java.io.File dataFolder = new java.io.File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) return;
        
        java.io.File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        
        for (java.io.File file : files) {
            String playerName = file.getName().replace(".yml", "");
            org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayerExact(playerName);
            if (p != null && p.isOnline()) {
                PlayerMissionData data = getPlayerData(p);
                if (data != null && !data.hasCompleted(missionId)) {
                    data.addCompletedMission(missionId);
                    data.addPendingReward(missionId);
                }
            } else {
                CustomConfig customConfig = new CustomConfig(plugin, "data", file.getName());
                FileConfiguration config = customConfig.getConfig();
                
                java.util.List<String> pending = config.getStringList("pending_rewards");
                java.util.List<String> completed = config.getStringList("completed_missions");
                
                if (!completed.contains(missionId)) {
                    completed.add(missionId);
                    config.set("completed_missions", completed);
                    
                    if (!pending.contains(missionId)) {
                        pending.add(missionId);
                        config.set("pending_rewards", pending);
                    }
                    customConfig.save();
                }
            }
        }
    }
}

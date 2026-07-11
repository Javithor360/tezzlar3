package com.panita.tezzlar3.missions.data;

import com.panita.tezzlar3.core.config.CustomConfig;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        data.setPunishmentsAcknowledged(config.getBoolean("punishments_acknowledged", true));
        data.setDeaths(config.getInt("deaths", 0));
        data.setMaxLives(config.getInt("max_lives", 3));
        data.setBanExpiration(config.getLong("ban_expiration", 0L));
        data.setDayChangeAcknowledged(config.getInt("day_change_acknowledged", 1));
        data.setLives(config.getInt("lives", 3));
        
        if (config.contains("first_join_day")) {
            data.setFirstJoinDay(config.getInt("first_join_day"));
        } else {
            if (player.hasPlayedBefore()) {
                data.setFirstJoinDay(1);
            } else {
                data.setFirstJoinDay(TimeManager.getCurrentDay());
            }
        }
        
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

        Map<String, Object> values = config.getValues(false);
        for (String k : values.keySet()) {
            config.set(k, null);
        }

        config.set("uuid", data.getUuid().toString());
        config.set("name", data.getName());
        config.set("max_lives", data.getMaxLives());
        config.set("lives", data.getLives());
        config.set("deaths", data.getDeaths());
        config.set("ban_expiration", data.getBanExpiration());
        config.set("day_change_acknowledged", data.getDayChangeAcknowledged());
        config.set("punishments_acknowledged", data.hasPunishmentsAcknowledged());
        config.set("first_join_day", data.getFirstJoinDay());

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
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) return;
        
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml") && !name.equals("global_missions.yml"));
        if (files == null) return;
        
        for (File file : files) {
            String playerName = file.getName().replace(".yml", "");
            Player p = Bukkit.getPlayerExact(playerName);
            if (p != null && p.isOnline()) {
                PlayerMissionData data = getPlayerData(p);
                if (data != null && !data.hasCompleted(missionId)) {
                    data.addCompletedMission(missionId);
                    data.addPendingReward(missionId);
                }
            } else {
                CustomConfig customConfig = new CustomConfig(plugin, "data", file.getName());
                FileConfiguration config = customConfig.getConfig();
                
                List<String> pending = config.getStringList("pending_rewards");
                List<String> completed = config.getStringList("completed_missions");
                
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

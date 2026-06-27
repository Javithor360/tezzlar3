package com.panita.tezzlar3.hardcore.util;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.config.CustomConfig;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HardcoreDataManager {

    public static void init(JavaPlugin plugin) {
    }

    private static PlayerMissionData getOnlineData(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && MissionsModule.getDataManager() != null) {
            return MissionsModule.getDataManager().getPlayerData(player);
        }
        return null;
    }

    public static int getDeaths(UUID playerUUID, String playerName) {
        PlayerMissionData data = getOnlineData(playerUUID);
        if (data != null) return data.getDeaths();
        
        CustomConfig dataConfig = new CustomConfig(Tezzlar.getInstance(), "data", playerName + ".yml");
        return dataConfig.getConfig().getInt("deaths", 0);
    }
    
    public static int getLives(UUID playerUUID, String playerName) {
        PlayerMissionData data = getOnlineData(playerUUID);
        if (data != null) return data.getLives();
        
        CustomConfig dataConfig = new CustomConfig(Tezzlar.getInstance(), "data", playerName + ".yml");
        return dataConfig.getConfig().getInt("lives", 3);
    }
    
    public static int getMaxLives(UUID playerUUID, String playerName) {
        PlayerMissionData data = getOnlineData(playerUUID);
        if (data != null) return data.getMaxLives();
        
        CustomConfig dataConfig = new CustomConfig(Tezzlar.getInstance(), "data", playerName + ".yml");
        return dataConfig.getConfig().getInt("max_lives", 3);
    }

    public static void setLives(UUID playerUUID, String playerName, int lives) {
        PlayerMissionData data = getOnlineData(playerUUID);
        if (data != null) {
            data.setLives(lives);
        }
        CustomConfig dataConfig = new CustomConfig(Tezzlar.getInstance(), "data", playerName + ".yml");
        dataConfig.getConfig().set("lives", lives);
        dataConfig.save();
    }
    
    public static void setMaxLives(UUID playerUUID, String playerName, int maxLives) {
        PlayerMissionData data = getOnlineData(playerUUID);
        if (data != null) {
            data.setMaxLives(maxLives);
        }
        CustomConfig dataConfig = new CustomConfig(Tezzlar.getInstance(), "data", playerName + ".yml");
        dataConfig.getConfig().set("max_lives", maxLives);
        dataConfig.save();
    }

    public static void setDeaths(UUID playerUUID, String playerName, int deaths) {
        PlayerMissionData data = getOnlineData(playerUUID);
        if (data != null) {
            data.setDeaths(deaths);
        }
        CustomConfig dataConfig = new CustomConfig(Tezzlar.getInstance(), "data", playerName + ".yml");
        dataConfig.getConfig().set("deaths", deaths);
        dataConfig.save();
    }

    public static void incrementDeaths(UUID playerUUID, String playerName) {
        setDeaths(playerUUID, playerName, getDeaths(playerUUID, playerName) + 1);
    }

    public static long getBanExpiration(UUID playerUUID, String playerName) {
        PlayerMissionData data = getOnlineData(playerUUID);
        if (data != null) return data.getBanExpiration();
        
        CustomConfig dataConfig = new CustomConfig(Tezzlar.getInstance(), "data", playerName + ".yml");
        return dataConfig.getConfig().getLong("ban_expiration", 0L);
    }

    public static void setBanExpiration(UUID playerUUID, String playerName, long timestamp) {
        PlayerMissionData data = getOnlineData(playerUUID);
        if (data != null) {
            data.setBanExpiration(timestamp);
        }
        CustomConfig dataConfig = new CustomConfig(Tezzlar.getInstance(), "data", playerName + ".yml");
        dataConfig.getConfig().set("ban_expiration", timestamp);
        dataConfig.save();
    }

    public static List<Map.Entry<String, Integer>> getTopDeaths(int limit) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>();
        File dataFolder = new File(Tezzlar.getInstance().getDataFolder(), "data");
        if (dataFolder.exists()) {
            File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml") && !name.equals("global_missions.yml"));
            if (files != null) {
                for (File file : files) {
                    String name = file.getName().replace(".yml", "");
                    CustomConfig customConfig = new CustomConfig(Tezzlar.getInstance(), "data", file.getName());
                    int deaths = customConfig.getConfig().getInt("deaths", 0);
                    if (deaths > 0) {
                        list.add(new AbstractMap.SimpleEntry<>(name, deaths));
                    }
                }
            }
        }
        list.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        if (list.size() > limit) {
            list = list.subList(0, limit);
        }
        return list;
    }
}

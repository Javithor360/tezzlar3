package com.panita.tezzlar3.hardcore.util;

import com.panita.tezzlar3.core.config.CustomConfig;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.configuration.ConfigurationSection;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HardcoreDataManager {
    private static CustomConfig dataConfig;

    public static void init(JavaPlugin plugin) {
        dataConfig = new CustomConfig(plugin, "hardcore", "deaths.yml");
    }

    public static int getDeaths(UUID playerUUID) {
        return dataConfig.getConfig().getInt("players." + playerUUID.toString() + ".deaths", 0);
    }

    public static void setDeaths(UUID playerUUID, String playerName, int deaths) {
        String path = "players." + playerUUID.toString();
        dataConfig.getConfig().set(path + ".name", playerName);
        dataConfig.getConfig().set(path + ".deaths", deaths);
        dataConfig.save();
    }

    public static void incrementDeaths(UUID playerUUID, String playerName) {
        setDeaths(playerUUID, playerName, getDeaths(playerUUID) + 1);
    }

    public static long getBanExpiration(UUID playerUUID) {
        return dataConfig.getConfig().getLong("players." + playerUUID.toString() + ".banExpiration", 0L);
    }

    public static void setBanExpiration(UUID playerUUID, String playerName, long timestamp) {
        String path = "players." + playerUUID.toString();
        dataConfig.getConfig().set(path + ".name", playerName);
        dataConfig.getConfig().set(path + ".banExpiration", timestamp);
        dataConfig.save();
    }

    public static List<Map.Entry<String, Integer>> getTopDeaths(int limit) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>();
        ConfigurationSection section = dataConfig.getConfig().getConfigurationSection("players");
        
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = section.getString(key + ".name", "Desconocido");
                int deaths = section.getInt(key + ".deaths", 0);
                list.add(new AbstractMap.SimpleEntry<>(name, deaths));
            }
            list.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue())); // Descending order
            if (list.size() > limit) {
                list = list.subList(0, limit);
            }
        }
        return list;
    }
}

package com.panita.tezzlar3.hardcore.util;

import com.panita.tezzlar3.core.config.CustomConfig;
import org.bukkit.plugin.java.JavaPlugin;

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
}

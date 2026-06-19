package com.panita.tezzlar3.inventory.util;

import com.panita.tezzlar3.core.config.CustomConfig;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GravesDataManager {
    private static CustomConfig gravesConfig;
    private static CustomConfig gravesBackupConfig;

    public static void init(JavaPlugin plugin) {
        gravesConfig = new CustomConfig(plugin, "", "graves.yml");
        gravesBackupConfig = new CustomConfig(plugin, "", "death_inventory_backup.yml");
    }

    public static void addGrave(Location location, UUID playerUUID, String playerName, String inventoryBase64) {
        String id = UUID.randomUUID().toString();
        String path = "graves." + id;
        
        gravesConfig.getConfig().set(path + ".world", location.getWorld().getName());
        gravesConfig.getConfig().set(path + ".x", location.getBlockX());
        gravesConfig.getConfig().set(path + ".y", location.getBlockY());
        gravesConfig.getConfig().set(path + ".z", location.getBlockZ());
        gravesConfig.getConfig().set(path + ".playerUUID", playerUUID.toString());
        gravesConfig.getConfig().set(path + ".playerName", playerName);
        gravesConfig.getConfig().set(path + ".itemsBase64", inventoryBase64);
        
        gravesConfig.save();

        // Save permanent history immediately upon death
        String historyPath = "archived_graves." + id;
        gravesBackupConfig.getConfig().set(historyPath + ".world", location.getWorld().getName());
        gravesBackupConfig.getConfig().set(historyPath + ".x", location.getBlockX());
        gravesBackupConfig.getConfig().set(historyPath + ".y", location.getBlockY());
        gravesBackupConfig.getConfig().set(historyPath + ".z", location.getBlockZ());
        gravesBackupConfig.getConfig().set(historyPath + ".playerUUID", playerUUID.toString());
        gravesBackupConfig.getConfig().set(historyPath + ".playerName", playerName);
        gravesBackupConfig.getConfig().set(historyPath + ".itemsBase64", inventoryBase64);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        gravesBackupConfig.getConfig().set(historyPath + ".diedAt", timestamp);
        
        gravesBackupConfig.save();
    }

    public static void removeGrave(String id) {
        gravesConfig.getConfig().set("graves." + id, null);
        gravesConfig.save();
    }

    /**
     * Retrieves all active graves. Key = Grave ID, Value = ConfigurationSection containing data.
     */
    public static Map<String, ConfigurationSection> getActiveGraves() {
        Map<String, ConfigurationSection> graves = new HashMap<>();
        ConfigurationSection gravesSection = gravesConfig.getConfig().getConfigurationSection("graves");
        
        if (gravesSection != null) {
            for (String key : gravesSection.getKeys(false)) {
                graves.put(key, gravesSection.getConfigurationSection(key));
            }
        }
        
        return graves;
    }

    /**
     * Retrieves the first active grave associated with a specific player.
     * @param playerUUID The UUID of the player.
     * @return The ConfigurationSection of the grave, or null if none is found.
     */
    public static ConfigurationSection getGraveForPlayer(UUID playerUUID) {
        Map<String, ConfigurationSection> graves = getActiveGraves();
        for (ConfigurationSection section : graves.values()) {
            String uuidStr = section.getString("playerUUID");
            if (uuidStr != null && uuidStr.equals(playerUUID.toString())) {
                return section;
            }
        }
        return null;
    }
}

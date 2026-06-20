package com.panita.tezzlar3.inventory.util;

import com.panita.tezzlar3.core.config.CustomConfig;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GravesDataManager {
    private static CustomConfig gravesConfig;
    private static CustomConfig gravesBackupConfig;

    public static void init(JavaPlugin plugin) {
        gravesConfig = new CustomConfig(plugin, "", "graves.yml");
        gravesBackupConfig = new CustomConfig(plugin, "", "death_inventory_backup.yml");
    }

    public static void addGrave(Location location, UUID playerUUID, String playerName, String inventoryBase64, String deathCause) {
        String id = UUID.randomUUID().toString();
        String path = "graves." + id;
        
        gravesConfig.getConfig().set(path + ".world", location.getWorld().getName());
        gravesConfig.getConfig().set(path + ".x", location.getBlockX());
        gravesConfig.getConfig().set(path + ".y", location.getBlockY());
        gravesConfig.getConfig().set(path + ".z", location.getBlockZ());
        gravesConfig.getConfig().set(path + ".playerUUID", playerUUID.toString());
        gravesConfig.getConfig().set(path + ".playerName", playerName);
        gravesConfig.getConfig().set(path + ".itemsBase64", inventoryBase64);
        gravesConfig.getConfig().set(path + ".deathCause", deathCause);
        
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
        gravesBackupConfig.getConfig().set(historyPath + ".deathCause", deathCause);
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

    /**
     * Retrieves all archived graves (backups) for a specific player, sorted from newest to oldest.
     */
    public static List<Map.Entry<String, ConfigurationSection>> getBackupsForPlayer(UUID playerUUID) {
        List<Map.Entry<String, ConfigurationSection>> list = new ArrayList<>();
        ConfigurationSection section = gravesBackupConfig.getConfig().getConfigurationSection("archived_graves");
        
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection grave = section.getConfigurationSection(key);
                if (grave != null && playerUUID.toString().equals(grave.getString("playerUUID"))) {
                    list.add(new AbstractMap.SimpleEntry<>(key, grave));
                }
            }
        }
        
        // Sort from newest to oldest based on diedAt
        list.sort((e1, e2) -> {
            String date1 = e1.getValue().getString("diedAt", "");
            String date2 = e2.getValue().getString("diedAt", "");
            return date2.compareTo(date1);
        });
        
        return list;
    }

    /**
     * Deletes a specific backup from the history.
     */
    public static void deleteBackup(String id) {
        gravesBackupConfig.getConfig().set("archived_graves." + id, null);
        gravesBackupConfig.save();
    }
}

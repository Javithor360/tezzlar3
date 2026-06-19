package com.panita.tezzlar3.inventory.util;

import com.panita.tezzlar3.core.config.CustomConfig;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GravesDataManager {
    private static CustomConfig gravesConfig;

    public static void init(JavaPlugin plugin) {
        gravesConfig = new CustomConfig(plugin, "", "graves.yml");
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
}

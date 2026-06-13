package com.panita.tezzlar3.qol.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class CoordinatesManager {
    private static File file;
    private static YamlConfiguration config;

    /**
     * Initializes the CoordinatesManager with the plugin's data folder
     * @param plugin The JavaPlugin instance
     */
    public static void init(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "savedcoordinates.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    /** Saves the current configuration to the file */
    private static boolean saveFile() {
        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves a location with the given name. Returns false if the name already exists.
     * @param name The name to save the location under
     * @param loc The Location to save
     * @return True if saved successfully, false if the name already exists
     */
    public static boolean saveLocation(String name, Location loc) {
        if (config.contains(name)) return false;

        config.set(name + ".world", loc.getWorld().getName());
        config.set(name + ".x", loc.getX());
        config.set(name + ".y", loc.getY());
        config.set(name + ".z", loc.getZ());
        config.set(name + ".yaw", loc.getYaw());
        config.set(name + ".pitch", loc.getPitch());

        return saveFile();
    }

    /**
     * Removes a saved location by name. Returns false if the name does not exist.
     * @param name The name of the location to remove
     * @return True if removed successfully, false if the name does not exist
     */
    public static boolean removeLocation(String name) {
        if (!config.contains(name)) return false;

        config.set(name, null);
        return saveFile();
    }

    /**
     * Retrieves a saved location by name. Returns null if the name does not exist or the world is invalid.
     * @param name The name of the location to retrieve
     * @return The Location object, or null if not found or invalid
     */
    public static Location getLocation(String name) {
        if (!config.contains(name)) return null;

        String world = config.getString(name + ".world");
        double x = config.getDouble(name + ".x");
        double y = config.getDouble(name + ".y");
        double z = config.getDouble(name + ".z");
        float yaw = (float) config.getDouble(name + ".yaw");
        float pitch = (float) config.getDouble(name + ".pitch");

        if (Bukkit.getWorld(world) == null) return null;

        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    /** Returns a set of all saved location names */
    public static Set<String> getAllNames() {
        return config.getKeys(false);
    }
}

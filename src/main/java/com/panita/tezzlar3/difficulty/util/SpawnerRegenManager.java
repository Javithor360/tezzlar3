package com.panita.tezzlar3.difficulty.util;

import com.panita.tezzlar3.Tezzlar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SpawnerRegenManager {
    private final File dataFile;
    private final YamlConfiguration dataConfig;
    private final List<RegeneratingSpawner> activeSpawners = new ArrayList<>();

    public SpawnerRegenManager() {
        this.dataFile = new File(Tezzlar.getInstance().getDataFolder(), "spawners_regen.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        load();
    }

    public void addSpawner(Location location, CreatureSpawner spawner, long timeRemainingMillis) {
        activeSpawners.add(new RegeneratingSpawner(location, spawner, timeRemainingMillis));
    }

    public List<RegeneratingSpawner> getActiveSpawners() {
        return activeSpawners;
    }

    public void removeSpawner(RegeneratingSpawner spawner) {
        activeSpawners.remove(spawner);
    }

    public void save() {
        // Clear old entries
        for (String key : dataConfig.getKeys(false)) {
            dataConfig.set(key, null);
        }

        int index = 0;
        for (RegeneratingSpawner spawner : activeSpawners) {
            String path = "spawner_" + index++;
            Location loc = spawner.getLocation();
            if (loc.getWorld() == null) continue;

            dataConfig.set(path + ".world", loc.getWorld().getName());
            dataConfig.set(path + ".x", loc.getBlockX());
            dataConfig.set(path + ".y", loc.getBlockY());
            dataConfig.set(path + ".z", loc.getBlockZ());
            dataConfig.set(path + ".item", spawner.getSpawnerItem());
            dataConfig.set(path + ".timeRemainingMillis", spawner.getTimeRemainingMillis());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            Tezzlar.getInstance().getLogger().log(Level.SEVERE, "Could not save spawners_regen.yml", e);
        }
    }

    private void load() {
        for (String key : dataConfig.getKeys(false)) {
            ConfigurationSection section = dataConfig.getConfigurationSection(key);
            if (section == null) continue;

            String worldName = section.getString("world");
            if (worldName == null) continue;

            World world = Bukkit.getWorld(worldName);
            if (world == null) continue; // World not loaded

            Location loc = new Location(world, section.getInt("x"), section.getInt("y"), section.getInt("z"));
            ItemStack item = section.getItemStack("item");
            long timeRemainingMillis = section.getLong("timeRemainingMillis");

            if (item != null) {
                activeSpawners.add(new RegeneratingSpawner(loc, item, timeRemainingMillis));
            }
        }
    }
}

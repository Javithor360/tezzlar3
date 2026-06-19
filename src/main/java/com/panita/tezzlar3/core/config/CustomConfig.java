package com.panita.tezzlar3.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CustomConfig {
    private final File file;
    private FileConfiguration config;

    public CustomConfig(JavaPlugin plugin, String folder, String fileName) {
        File dataDir = new File(plugin.getDataFolder(), folder);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        this.file = new File(dataDir, fileName);
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create " + fileName + " in " + folder + "!");
            }
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }
}

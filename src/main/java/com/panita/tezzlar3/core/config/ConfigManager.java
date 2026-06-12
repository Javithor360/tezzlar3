package com.panita.tezzlar3.core.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.function.BiConsumer;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private static final MiniMessage mm = MiniMessage.miniMessage();

    public ConfigManager(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    // -----------------------------------
    // CONFIG READERS
    // -----------------------------------
    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public Component getMini(String path, String defRaw) {
        String raw = config.getString(path, defRaw);
        return mm.deserialize(raw != null ? raw : defRaw);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public ConfigurationSection getSection(String path) {
        return config.getConfigurationSection(path);
    }

    // -----------------------------------
    // CONFIG UPDATERS
    // -----------------------------------

    public void updateBoolean(String path, boolean value, BiConsumer<String, Boolean> liveUpdate) {
        config.set(path, value);
        plugin.saveConfig();
        if (liveUpdate != null) liveUpdate.accept(path, value);
    }

    public void updateString(String path, String value, BiConsumer<String, String> liveUpdate) {
        config.set(path, value);
        plugin.saveConfig();
        if (liveUpdate != null) liveUpdate.accept(path, value);
    }

    public void updateInt(String path, int value, BiConsumer<String, Integer> liveUpdate) {
        config.set(path, value);
        plugin.saveConfig();
        if (liveUpdate != null) liveUpdate.accept(path, value);
    }

    public void updateDouble(String path, double value, BiConsumer<String, Double> liveUpdate) {
        config.set(path, value);
        plugin.saveConfig();
        if (liveUpdate != null) liveUpdate.accept(path, value);
    }

    public void updateStringList(String path, List<String> values, BiConsumer<String, List<String>> liveUpdate) {
        config.set(path, values);
        plugin.saveConfig();
        if (liveUpdate != null) liveUpdate.accept(path, values);
    }
}


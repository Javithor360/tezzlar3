package com.panita.tezzlar3.core.modules;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public interface PluginModule {
    /** The name of the module */
    String id();

    /** The base package where the module's classes are located */
    String basePackage();

    /** The package where command classes are located */
    default String commandPackage() { return basePackage() + ".commands"; }

    /** The package where event listener classes are located */
    default String listenerPackage() { return basePackage() + ".listeners"; }

    /** Access to the configuration manager */
    default ConfigManager configManager() {
        return Tezzlar.getConfigManager();
    }

    /** Whether the module is enabled */
    boolean isEnabled();

    /** Set whether the module is enabled */
    void setEnabled(boolean value);

    /** Called when the plugin is enabled */
    default void onEnable(JavaPlugin plugin) {}

    /** Called when the plugin is disabled */
    default void onDisable(JavaPlugin plugin) {}

    /** Called when the plugin is reloaded */
    default void reload(JavaPlugin plugin) {
        onDisable(plugin);
        onEnable(plugin);
    }
}


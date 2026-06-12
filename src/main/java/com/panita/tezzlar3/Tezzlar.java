package com.panita.tezzlar3;

import com.panita.tezzlar3.core.config.Config;
import com.panita.tezzlar3.core.config.ConfigManager;
import com.panita.tezzlar3.core.modules.ModuleManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Tezzlar extends JavaPlugin {

    private static Tezzlar instance;
    private static ConfigManager configManager;
    private static ModuleManager moduleManager;

    @Override
    public void onEnable() {
        // Plugin initialization
        getLogger().info("Tezzlar is starting up!");

        instance = this;

        // Load Configuration
        Config.load(this);
        configManager = new ConfigManager(this, getConfig());

        // Load Modules
        moduleManager = new ModuleManager(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Tezzlar is shutting down!");

        if (moduleManager != null) {
            moduleManager.disableAll();
        }
    }

    /**
     * Gets the singleton instance of the Tezzlar Plugin
     *
     * @return The Tezzlar instance.
     */
    public static Tezzlar getInstance() {
        return instance;
    }

    /**
     * Gets the ConfigManager instance.
     *
     * @return The ConfigManager instance.
     */
    public static ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Gets the ModuleManager instance.
     *
     * @return The ModuleManager instance.
     */
    public static ModuleManager getModuleManager() {
        return moduleManager;
    }

    /**
     * Sets the ConfigManager instance.
     *
     * @param manager The ConfigManager instance to set.
     */
    public static void setConfigManager(ConfigManager manager) {
        configManager = manager;
    }
}

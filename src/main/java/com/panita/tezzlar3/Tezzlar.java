package com.panita.tezzlar3;

import com.panita.tezzlar3.core.commands.CommandRegistry;
import com.panita.tezzlar3.core.config.Config;
import com.panita.tezzlar3.core.config.ConfigManager;
import com.panita.tezzlar3.core.modules.ModuleManager;
import com.panita.tezzlar3.hardcore.HardcoreModule;
import com.panita.tezzlar3.inventory.InventoryModule;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.qol.QualityOfLifeModule;
import com.panita.tezzlar3.timeline.TimelineModule;
import com.panita.tezzlar3.core.listeners.MenuListener;
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

        // Config setup
        Config.load(this);
        configManager = new ConfigManager(this, getConfig());

        new CommandRegistry(this).registerAll("com.panita.tezzlar3.core.commands.base"); // load base commands first
        
        // Register core listeners
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        
        // Register Modules
        moduleManager = new ModuleManager(this);
        moduleManager.register(new QualityOfLifeModule());
        moduleManager.register(new HardcoreModule());
        moduleManager.register(new InventoryModule());
        moduleManager.register(new TimelineModule());
        moduleManager.register(new MissionsModule());
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

package com.panita.tezzlar3;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.CommandRegistry;
import com.panita.tezzlar3.core.config.Config;
import com.panita.tezzlar3.core.config.ConfigManager;
import com.panita.tezzlar3.core.modules.ModuleManager;
import com.panita.tezzlar3.hardcore.HardcoreModule;
import com.panita.tezzlar3.inventory.InventoryModule;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.qol.QualityOfLifeModule;
import com.panita.tezzlar3.rebalance.RebalanceModule;
import com.panita.tezzlar3.difficulty.DifficultyModule;
import com.panita.tezzlar3.timeline.TimelineModule;
import com.panita.tezzlar3.troll.TrollModule;
import com.panita.tezzlar3.minievents.MiniEventsModule;
import com.panita.tezzlar3.core.listeners.MenuListener;
import com.panita.tezzlar3.core.listeners.RiderDismountListener;
import com.panita.tezzlar3.core.papi.TezzlarPlaceholderExpansion;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarManager;
import org.bukkit.Bukkit;
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
        getServer().getPluginManager().registerEvents(new RiderDismountListener(), this);
        
        // Start ActionBar Manager
        new ActionBarManager().start();
        
        // Register Modules
        moduleManager = new ModuleManager(this);
        moduleManager.register(new QualityOfLifeModule());
        moduleManager.register(new HardcoreModule());
        moduleManager.register(new InventoryModule());
        moduleManager.register(new TimelineModule());
        moduleManager.register(new MissionsModule());
        moduleManager.register(new RebalanceModule());
        moduleManager.register(new DifficultyModule());
        moduleManager.register(new TrollModule());
        moduleManager.register(new MiniEventsModule(this));
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TezzlarPlaceholderExpansion().register();
            getLogger().info("Found PAPI plugin, registering Tezzlar 3 placeholders!");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Tezzlar is shutting down!");

        // Clean up bossbars to prevent duplication on reload
        Messenger.hideAllBossBars();
        
        if (ActionBarManager.getInstance() != null) {
            ActionBarManager.getInstance().stop();
        }

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

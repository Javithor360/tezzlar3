package com.panita.tezzlar3.core.modules;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.commands.CommandRegistry;
import com.panita.tezzlar3.core.config.ConfigManager;
import com.panita.tezzlar3.core.listeners.ListenerRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages the registration and lifecycle of plugin modules.
 */
public class ModuleManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final CommandRegistry cmdRegistry;
    private final ListenerRegistry listenerRegistry;

    private final List<PluginModule> active = new ArrayList<>();
    private final List<PluginModule> allModules = new ArrayList<>();

    /**
     * Constructs a ModuleManager for the given plugin.
     *
     * @param plugin The main JavaPlugin instance.
     */
    public ModuleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.cmdRegistry = new CommandRegistry(plugin);
        this.listenerRegistry = new ListenerRegistry(plugin, plugin.getConfig());
        this.configManager = Tezzlar.getConfigManager();
    }

    /**
     * Retrieves a registered module by its ID.
     *
     * @param id The ID of the module.
     * @return The PluginModule instance, or null if not found.
     */
    public PluginModule getModule(String id) {
        return active.stream()
                .filter(m -> m.id().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if a module with the given ID is currently active.
     *
     * @param id The ID of the module.
     * @return True if the module is active, false otherwise.
     */
    public boolean isModuleActive(String id) {
        return getModule(id) != null;
    }

    /**
     * Returns the list of all registered modules, including inactive ones.
     */
    public List<PluginModule> getAllModules() {
        return List.copyOf(allModules);
    }

    /**
     * Returns a copy of the list of active modules.
     */
    public List<PluginModule> getActiveModules() {
        return new ArrayList<>(active);
    }

    /**
     * Registers multiple plugin modules.
     *
     * @param modules The modules to register.
     */
    public void register(PluginModule... modules) {
        Arrays.stream(modules).forEach(this::registerOne);
    }

    /**
     * Registers a single plugin module.
     *
     * @param m The module to register.
     */
    private void registerOne(PluginModule m) {
        allModules.add(m);

        // Check if the module is enabled in the config
        boolean enabled = configManager.getBoolean(m.id() + ".enabled", true);
        m.setEnabled(enabled);

        if (!enabled) {
            plugin.getLogger().info("[Module] " + m.id() + " -> disabled in config.yml");
            return;
        }

        enableModule(m);
    }

    /**
     * Enables a specific plugin module.
     *
     * @param module The module to enable.
     */
    private void enableModule(PluginModule module) {
        // Enable the module
        plugin.getLogger().info("[Module] Enabling " + module.id());

        // Register commands and listeners
        cmdRegistry.registerAll(module.commandPackage());
        listenerRegistry.registerAll(module.listenerPackage());
        module.onEnable(plugin);
        module.setEnabled(true);

        // Add to active modules list
        active.add(module);
    }

    /**
     * Disables a specific plugin module.
     *
     * @param module The module to disable.
     */
    public void disableModule(PluginModule module) {
        if (!active.contains(module)) return;

        // Unregister listeners
        listenerRegistry.unregisterAll(module.listenerPackage());

        // Unregister commands
        cmdRegistry.unregisterAll(module.commandPackage());

        module.onDisable(plugin);
        module.setEnabled(false);
        active.remove(module);

        plugin.getLogger().info("[Module] Disabled " + module.id());
    }

    /**
     * Reloads a specific module based on its configuration setting.
     * If the module is enabled in the config but not currently active, it will be enabled.
     * If the module is disabled in the config but currently active, it will be disabled.
     * If the module is already active and enabled in the config, it will be reloaded.
     *
     * @param module The module to reload.
     */
    public void reloadModule(PluginModule module) {
        boolean enabledInConfig = Tezzlar.getConfigManager().getBoolean(module.id() + ".enabled", true);

        // Plugin enabled in config file but not in PluginModule instance
        if (enabledInConfig && !module.isEnabled()) {
            enableModule(module);
        // Plugin disabled in config file but enabled in PluginModule instance
        } else if (!enabledInConfig && module.isEnabled()) {
            disableModule(module);
        // Plugin enabled in config and already enabled, just reload
        } else if (enabledInConfig) {
            module.reload(plugin);
        }
    }

    /**
     * Disables all registered plugin modules.
     */
    public void disableAll() {
        for (PluginModule m : new ArrayList<>(active)) {
            disableModule(m);
        }
    }

    public ListenerRegistry getListenerRegistry() {
        return listenerRegistry;
    }
}


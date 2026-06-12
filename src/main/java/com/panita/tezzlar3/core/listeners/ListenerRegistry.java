package com.panita.tezzlar3.core.listeners;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;

public class ListenerRegistry {
    private final Plugin plugin;
    private final Configuration config;

    /** Map of module IDs to their registered listeners */
    private final Map<String, List<Listener>> moduleListeners = new HashMap<>();

    public ListenerRegistry(Plugin plugin, Configuration config) {
        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Registers all Listener classes found in the specified base package.
     * @param basePackage The base package to scan for Listener classes.
     */
    public void registerAll(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<? extends Listener>> listeners = reflections.getSubTypesOf(Listener.class);

        List<Listener> registered = new ArrayList<>();

        for (Class<? extends Listener> listenerClass : listeners) {
            try {
                Listener listener;

                // Try to find a constructor that accepts Configuration
                Constructor<?>[] constructors = listenerClass.getConstructors();
                if (constructors.length > 0 && constructors[0].getParameterCount() == 1 &&
                        constructors[0].getParameterTypes()[0] == Configuration.class) {
                    listener = (Listener) constructors[0].newInstance(config);
                } else {
                    listener = listenerClass.getDeclaredConstructor().newInstance();
                }

                Bukkit.getPluginManager().registerEvents(listener, plugin);
                registered.add(listener);

                plugin.getLogger().info("[INFO] Registered listener: " + listenerClass.getSimpleName());
            } catch (Exception e) {
                plugin.getLogger().warning("[ERROR] Failed to register listener " + listenerClass.getSimpleName() + ": " + e.getMessage());
            }
        }

        // Store the registered listeners for this module
        moduleListeners.put(basePackage, registered);
    }

    /**
     * Unregisters all listeners associated with the specified base package.
     * @param basePackage The base package of the module whose listeners should be unregistered.
     */
    public void unregisterAll(String basePackage) {
        List<Listener> registered = moduleListeners.get(basePackage);
        if (registered == null) return;

        for (Listener listener : registered) {
            HandlerList.unregisterAll(listener);

            moduleListeners.remove(basePackage);
            plugin.getLogger().info("[INFO] Unregistered all listeners for package: " + basePackage);
        }
    }

    /**
     * Unregisters all listeners for all registered modules.
     */
    public void unregisterAllModules() {
        for (String pkg : new ArrayList<>(moduleListeners.keySet())) {
            unregisterAll(pkg);
        }
    }
}


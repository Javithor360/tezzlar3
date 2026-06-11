package com.panita.tezzlar3;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin initialization
        getLogger().info("El plugin tezzlar3 se ha habilitado correctamente.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("El plugin tezzlar3 se ha deshabilitado correctamente.");
    }
}

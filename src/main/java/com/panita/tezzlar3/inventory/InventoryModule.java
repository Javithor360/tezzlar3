package com.panita.tezzlar3.inventory;

import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.inventory.tasks.RevenantTrackerTask;
import com.panita.tezzlar3.inventory.util.GravesDataManager;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryModule implements PluginModule {
    private boolean enabled;
    public static String packageName = "com.panita.tezzlar3.inventory";

    @Override
    public String id() {
        return "inventory";
    }

    @Override
    public String basePackage() {
        return packageName;
    }

    @Override
    public void onEnable(JavaPlugin plugin) {
        GravesDataManager.init(plugin);
        plugin.getServer().getScheduler().runTaskTimer(plugin, new RevenantTrackerTask(), 20L, 20L);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean value) {
        this.enabled = value;
    }
}

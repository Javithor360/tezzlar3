package com.panita.tezzlar3.qol;

import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.qol.tasks.QolTotemPassiveTask;
import com.panita.tezzlar3.qol.util.CoordinatesManager;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.plugin.java.JavaPlugin;

public class QualityOfLifeModule implements PluginModule {
    private boolean enabled;
    public static String packageName = "com.panita.tezzlar3.qol";

    @Override
    public String id() {
        return "qol";
    }

    @Override
    public String basePackage() {
        return packageName;
    }

    @Override
    public void onEnable(JavaPlugin plugin) {
        CoordinatesManager.init(plugin);
        CustomItemManager.init(plugin.getDataFolder());
        
        // Start passive totem effects task
        new QolTotemPassiveTask().runTaskTimer(plugin, 0L, 20L);
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

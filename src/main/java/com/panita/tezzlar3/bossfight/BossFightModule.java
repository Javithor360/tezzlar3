package com.panita.tezzlar3.bossfight;

import com.panita.tezzlar3.core.modules.PluginModule;
import org.bukkit.plugin.java.JavaPlugin;

public class BossFightModule implements PluginModule {
    private boolean enabled;
    public static String packageName = "com.panita.tezzlar3.bossfight";

    @Override
    public String id() {
        return "bossfight";
    }

    @Override
    public String basePackage() {
        return packageName;
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

package com.panita.tezzlar3.troll;

import com.panita.tezzlar3.core.modules.PluginModule;
import org.bukkit.plugin.java.JavaPlugin;

public class TrollModule implements PluginModule {
    private boolean enabled = true;
    public static String packageName = "com.panita.tezzlar3.troll";

    @Override
    public String id() {
        return "troll";
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

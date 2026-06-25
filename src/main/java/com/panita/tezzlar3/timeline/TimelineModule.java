package com.panita.tezzlar3.timeline;

import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TimelineModule implements PluginModule {
    private boolean enabled;
    public static final String PACKAGE_NAME = "com.panita.tezzlar3.timeline";

    @Override
    public String id() {
        return "timeline";
    }

    @Override
    public String basePackage() {
        return PACKAGE_NAME;
    }

    @Override
    public void onEnable(JavaPlugin plugin) {
        TimeManager.init(plugin);
        enabled = true;
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

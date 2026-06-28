package com.panita.tezzlar3.minievents;

import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.minievents.impl.*;
import com.panita.tezzlar3.Tezzlar;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniEventsModule implements PluginModule {
    
    private boolean enabled;
    public static final String PACKAGE_NAME = "com.panita.tezzlar3.minievents";
    private static MiniEventsModule instance;
    private MiniEventManager manager;

    public MiniEventsModule(JavaPlugin plugin) {
        instance = this;
    }
    
    public static MiniEventManager getManager() {
        return instance.manager;
    }

    @Override
    public String id() {
        return "minievents";
    }

    @Override
    public String basePackage() {
        return PACKAGE_NAME;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    @Override
    public void onEnable(JavaPlugin plugin) {
        manager = new MiniEventManager(plugin);
        
        manager.registerEvent(new UHCModeEvent());
        manager.registerEvent(new RandomPotionEvent());
        manager.registerEvent(new PositionSwapEvent());
        manager.registerEvent(new SpecialDropMobEvent());
        manager.registerEvent(new BloodMoonEvent());
        manager.registerEvent(new GlobalAcidRainEvent());
        manager.registerEvent(new WrongToolDamageEvent());

        manager.init();
        enabled = true;
    }

    @Override
    public void onDisable(JavaPlugin plugin) {
        if (manager != null) {
            manager.unregisterAll();
        }
        enabled = false;
    }
}

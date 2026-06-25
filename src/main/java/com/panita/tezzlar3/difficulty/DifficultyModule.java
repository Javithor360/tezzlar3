package com.panita.tezzlar3.difficulty;

import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.difficulty.mechanics.AnimalOneShotMechanic;
import com.panita.tezzlar3.difficulty.mechanics.DifficultyMechanic;
import com.panita.tezzlar3.difficulty.mechanics.DoubleFallDamageMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ZombieBeekeeperMechanic;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class DifficultyModule implements PluginModule {
    private boolean enabled;
    public static final String PACKAGE_NAME = "com.panita.tezzlar3.difficulty";

    @Override
    public String id() {
        return "difficulty";
    }

    @Override
    public String basePackage() {
        return PACKAGE_NAME;
    }

    @Override
    public void onEnable(JavaPlugin plugin) {
        List<DifficultyMechanic> mechanics = new ArrayList<>();
        
        // Day 3
        mechanics.add(new DoubleFallDamageMechanic(plugin));
        mechanics.add(new AnimalOneShotMechanic(plugin));
        mechanics.add(new ZombieBeekeeperMechanic(plugin));
        
        for (DifficultyMechanic mechanic : mechanics) {
            plugin.getServer().getPluginManager().registerEvents(mechanic, plugin);
        }
        
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

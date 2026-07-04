package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class DifficultyMechanic implements Listener {
    private final int activationDay;
    protected final JavaPlugin plugin;

    public DifficultyMechanic(JavaPlugin plugin, int activationDay) {
        this.plugin = plugin;
        this.activationDay = activationDay;
    }

    /**
     * Checks if the mechanic should be active based on the current timeline day.
     * @return true if the current day is greater than or equal to the activation day.
     */
    public boolean isActive() {
        PluginModule module = Tezzlar.getModuleManager().getModule("difficulty");
        if (module == null || !module.isEnabled()) return false;
        
        return TimeManager.getCurrentDay() >= activationDay;
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.timeline.util.TimeManager;

import java.util.Random;

public class NightRainMechanic extends DifficultyMechanic {

    private final Random random = new Random();

    public NightRainMechanic(JavaPlugin plugin) {
        super(plugin, 11);
        
        // Check every 100 ticks (5 seconds)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            // Revert the "always rain at night" mechanic on day 15+ since it's eternal night
            if (TimeManager.getCurrentDay() >= 15) return;
            
            for (World world : Bukkit.getWorlds()) {
                if (world.getEnvironment() == World.Environment.NORMAL) {
                    long time = world.getTime();
                    // Night is roughly between 13000 and 23000 ticks
                    if (time > 13000 && time < 23000) {
                        if (!world.hasStorm()) {
                            world.setStorm(true);
                            
                            // Probabilidad de que sea tormenta eléctrica en lugar de lluvia normal
                            if (random.nextDouble() < 0.30) {
                                world.setThundering(true);
                            }
                            
                            // Set a long duration so it doesn't clear immediately
                            world.setWeatherDuration(12000); 
                        }
                    }
                }
            }
        }, 100L, 100L);
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class EternalNightMechanic extends DifficultyMechanic {

    public EternalNightMechanic(JavaPlugin plugin) {
        super(plugin, 15);
        
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            for (World world : Bukkit.getWorlds()) {
                if (world.getEnvironment() == World.Environment.NORMAL) {
                    long time = world.getTime();
                    // If it's daytime (less than 13000) or sunrise (more than 23000)
                    if (time < 13000 || time > 23000) {
                        // Calculate ticks left to skip directly to 13000 (dusk)
                        long ticksToSkip = (13000 - time + 24000) % 24000;
                        world.setFullTime(world.getFullTime() + ticksToSkip);
                    }
                }
            }
        }, 0L, 100L); // Check every 5 seconds
    }
}

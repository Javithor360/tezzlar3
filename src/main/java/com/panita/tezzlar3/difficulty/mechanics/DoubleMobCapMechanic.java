package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DoubleMobCapMechanic extends DifficultyMechanic {
    public DoubleMobCapMechanic(JavaPlugin plugin) {
        super(plugin, 9); // Day 9
        
        // Run a background task to enforce or restore limits based on the current day
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (isActive()) {
                // Day 9 is active, ensure all worlds have double mob cap
                int doubleLimit = Bukkit.getMonsterSpawnLimit() * 2;
                if (doubleLimit <= 0) doubleLimit = 140; // Fallback just in case
                
                for (World world : Bukkit.getWorlds()) {
                    world.setSpawnLimit(SpawnCategory.MONSTER, doubleLimit);
                }
            } else {
                // Day 9 is not active, restore any altered worlds to the server default
                for (World world : Bukkit.getWorlds()) {
                    world.setSpawnLimit(SpawnCategory.MONSTER, -1);
                }
            }
        }, 100L, 100L); // Check every 5 seconds (100 ticks)
    }
}

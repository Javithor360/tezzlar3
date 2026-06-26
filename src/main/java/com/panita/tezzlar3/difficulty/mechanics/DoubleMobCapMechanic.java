package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DoubleMobCapMechanic extends DifficultyMechanic {
    private final Map<UUID, Integer> originalLimits = new HashMap<>();

    public DoubleMobCapMechanic(JavaPlugin plugin) {
        super(plugin, 9); // Day 9
        
        // Run a background task to enforce or restore limits based on the current day
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (isActive()) {
                // Day 9 is active, ensure all worlds have double mob cap
                for (World world : Bukkit.getWorlds()) {
                    if (!originalLimits.containsKey(world.getUID())) {
                        int currentLimit = world.getSpawnLimit(SpawnCategory.MONSTER);
                        
                        // Bukkit might return -1 if the world relies on server default configs
                        if (currentLimit > 0) {
                            originalLimits.put(world.getUID(), currentLimit);
                            world.setSpawnLimit(SpawnCategory.MONSTER, currentLimit * 2);
                        } else if (currentLimit < 0) {
                            originalLimits.put(world.getUID(), -1);
                            world.setSpawnLimit(SpawnCategory.MONSTER, 140); // 70 is vanilla default, so 140 is double
                        }
                    }
                }
            } else {
                // Day 9 is not active, restore any altered worlds
                for (World world : Bukkit.getWorlds()) {
                    if (originalLimits.containsKey(world.getUID())) {
                        int original = originalLimits.get(world.getUID());
                        world.setSpawnLimit(SpawnCategory.MONSTER, original);
                        originalLimits.remove(world.getUID());
                    }
                }
            }
        }, 100L, 100L); // Check every 5 seconds (100 ticks)
    }
}

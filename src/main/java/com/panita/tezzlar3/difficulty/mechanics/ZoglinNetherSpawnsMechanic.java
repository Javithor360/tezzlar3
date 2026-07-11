package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class ZoglinNetherSpawnsMechanic extends DifficultyMechanic {

    private final Random random = new Random();

    public ZoglinNetherSpawnsMechanic(JavaPlugin plugin) {
        super(plugin, 24);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNetherSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getSpawnReason() != SpawnReason.NATURAL && event.getSpawnReason() != SpawnReason.DEFAULT) return;
        if (event.getEntity().getWorld().getEnvironment() != World.Environment.NETHER) return;
        
        if (event.getEntityType() == EntityType.ZOMBIFIED_PIGLIN) {
            // 6% chance
            if (random.nextDouble() <= 0.06) {
                event.setCancelled(true);
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (event.getLocation().getChunk().isLoaded()) {
                        event.getLocation().getWorld().spawnEntity(event.getLocation(), EntityType.ZOGLIN);
                    }
                });
            }
        }
    }
}

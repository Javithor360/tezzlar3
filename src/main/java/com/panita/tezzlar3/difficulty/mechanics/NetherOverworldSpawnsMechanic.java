package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Random;

public class NetherOverworldSpawnsMechanic extends DifficultyMechanic {

    private final Random random = new Random();
    
    private final List<EntityType> OVERWORLD_MOBS = List.of(
            EntityType.CREEPER,
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.SPIDER,
            EntityType.PHANTOM
    );
    
    private final List<EntityType> NETHER_MOBS = List.of(
            EntityType.PIGLIN,
            EntityType.ZOMBIFIED_PIGLIN,
            EntityType.PIGLIN_BRUTE,
            EntityType.GHAST,
            EntityType.MAGMA_CUBE,
            EntityType.BLAZE,
            EntityType.WITHER_SKELETON,
            EntityType.ENDERMAN,
            EntityType.SKELETON
    );

    public NetherOverworldSpawnsMechanic(JavaPlugin plugin) {
        super(plugin, 23);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNetherSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        // Only natural spawns in the nether
        if (event.getEntity().getWorld().getEnvironment() != World.Environment.NETHER) return;
        if (event.getSpawnReason() != SpawnReason.NATURAL && event.getSpawnReason() != SpawnReason.DEFAULT) return;
        
        EntityType type = event.getEntityType();
        
        if (NETHER_MOBS.contains(type)) {
            // 35% chance to replace with overworld mob
            if (random.nextDouble() <= 0.35) {
                event.setCancelled(true);
                
                EntityType replacement = OVERWORLD_MOBS.get(random.nextInt(OVERWORLD_MOBS.size()));
                
                // Spawn it naturally so other mechanics (like ZombieBeekeeper) can intercept and modify it
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (event.getLocation().getChunk().isLoaded()) {
                        event.getLocation().getWorld().spawnEntity(event.getLocation(), replacement, SpawnReason.NATURAL);
                    }
                });
            }
        }
    }
}

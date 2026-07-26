package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Set;

public class NoNaturalSpawnsMechanic extends DifficultyMechanic {

    private final Set<EntityType> BLOCKED_TYPES = EnumSet.of(
            EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED,
            EntityType.SKELETON, EntityType.BOGGED, EntityType.PARCHED, EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.WITHER,
            EntityType.SPIDER, EntityType.CAVE_SPIDER,
            EntityType.CREEPER,
            EntityType.PHANTOM
    );

    public NoNaturalSpawnsMechanic(JavaPlugin plugin) {
        super(plugin, 30);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;

        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        
        // Only block natural-like spawns
        if (reason == CreatureSpawnEvent.SpawnReason.NATURAL ||
            reason == CreatureSpawnEvent.SpawnReason.DEFAULT) {
            
            if (BLOCKED_TYPES.contains(event.getEntityType())) {
                event.setCancelled(true);
            }
        }
    }
}

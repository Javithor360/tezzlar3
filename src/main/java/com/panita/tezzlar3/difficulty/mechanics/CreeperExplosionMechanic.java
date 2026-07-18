package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.CreatureSpawnEvent;
import com.panita.tezzlar3.timeline.util.TimeManager;

public class CreeperExplosionMechanic extends DifficultyMechanic {

    private final NamespacedKey teleportKey;

    public CreeperExplosionMechanic(JavaPlugin plugin) {
        super(plugin, 1); // Start from day 1 to handle early game rules independently
        this.teleportKey = new NamespacedKey(plugin, "teleporting_creeper");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Creeper && TimeManager.getCurrentDay() >= 7) {
            // We multiply the radius by 1.6 as requested.
            event.setRadius(event.getRadius() * 1.6f);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Creeper creeper) {
            boolean shouldCancelBlocks = false;
            
            // 1. If it is a teleporting creeper, it never breaks blocks
            if (creeper.getPersistentDataContainer().has(teleportKey, PersistentDataType.BYTE)) {
                shouldCancelBlocks = true;
            }
            
            // 2. If it is day 16+ and it spawned from a spawner, it doesn't break blocks
            if (TimeManager.getCurrentDay() >= 16 && creeper.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                shouldCancelBlocks = true;
            }
            
            if (shouldCancelBlocks) {
                event.blockList().clear(); // Clears blocks so it does damage to entities but breaks nothing
            }
        }
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ElderGuardianMechanic extends DifficultyMechanic {

    public ElderGuardianMechanic(JavaPlugin plugin) {
        super(plugin, 19);
    }

    @EventHandler
    public void onGuardianSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getEntityType() == EntityType.GUARDIAN) {
            if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
            
            // 1% chance
            if (Math.random() < 0.01) {
                event.setCancelled(true);
                EntityUtils.spawnNatural(event.getLocation(), EntityType.ELDER_GUARDIAN);
            }
        }
    }
}

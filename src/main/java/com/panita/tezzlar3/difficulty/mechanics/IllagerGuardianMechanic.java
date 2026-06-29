package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class IllagerGuardianMechanic extends DifficultyMechanic {

    public IllagerGuardianMechanic(JavaPlugin plugin) {
        super(plugin, 16);
    }

    @EventHandler
    public void onIllagerSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        EntityType type = event.getEntityType();
        if (type == EntityType.WITCH || type == EntityType.PILLAGER || 
            type == EntityType.EVOKER || type == EntityType.VINDICATOR) {
            
            if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
            
            LivingEntity illager = event.getEntity();
            Entity guardian = EntityUtils.spawnNatural(event.getLocation(), EntityType.GUARDIAN);
            guardian.setSilent(true);
            illager.addPassenger(guardian);
        }
    }
}

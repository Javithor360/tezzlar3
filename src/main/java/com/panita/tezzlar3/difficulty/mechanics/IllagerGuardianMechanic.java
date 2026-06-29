package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import com.panita.tezzlar3.timeline.util.TimeManager;
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
            
            LivingEntity illager = event.getEntity();
            
            boolean isPeruvian = illager.getPersistentDataContainer().has(new NamespacedKey(plugin, "is_peruvian"), PersistentDataType.BYTE);
            if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason()) && !isPeruvian) return;
            
            EntityType guardianType = EntityType.GUARDIAN;
            if (TimeManager.getCurrentDay() >= 19 && Math.random() < 0.1) {
                guardianType = EntityType.ELDER_GUARDIAN;
            }
            
            Entity guardian = EntityUtils.spawnNatural(event.getLocation(), guardianType);
            guardian.setSilent(true);
            illager.addPassenger(guardian);
        }
    }
}

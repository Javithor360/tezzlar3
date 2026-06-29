package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.util.MobGearUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.timeline.util.TimeManager;

public class RandomMobGearMechanic extends DifficultyMechanic {

    public RandomMobGearMechanic(JavaPlugin plugin) {
        super(plugin, 5);
    }

    // We run at HIGH but we delay execution by 1 tick so custom mechanics (like Beekeeper) can apply their 'is_' tags first
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        LivingEntity entity = event.getEntity();
        
        if (event.getSpawnReason() == SpawnReason.SPAWNER) {
            int day = TimeManager.getCurrentDay();
            if (day >= 16 && day <= 18) return;
        }
        
        if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
        
        // Use the explicit list of valid mob types
        if (!MobGearUtils.isValidMobTarget(entity)) return;
        
        // Delay 1 tick to allow custom mob registries to finish modifying the entity
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!entity.isValid() || entity.isDead()) return;
            
            // Skip custom mobs (e.g., Zombie Beekeeper, Infrared Skeleton) to protect their custom gear
            if (EntityUtils.isCustomMob(entity)) return;
            
            MobGearUtils.equipRandomGear(entity);
        });
    }
}

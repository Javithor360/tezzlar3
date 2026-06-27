package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class GlobalVariantsMechanic extends DifficultyMechanic {

    private final Random random = new Random();

    public GlobalVariantsMechanic(JavaPlugin plugin) {
        super(plugin, 14);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (EntityUtils.isCustomMob(event.getEntity())) return;
        if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
        
        EntityType type = event.getEntityType();
        
        if (type == EntityType.ZOMBIE) {
            // Husk conversion 25% chance in any biome
            if (random.nextDouble() < 0.25) {
                event.getLocation().getWorld().spawnEntity(event.getLocation(), EntityType.HUSK);
                event.setCancelled(true);
            }
        } else if (type == EntityType.SKELETON) {
            // Stray, Bogged, or Parched 30% chance in any biome
            if (random.nextDouble() < 0.30) {
                int r = random.nextInt(3);
                EntityType variant = null;
                if (r == 0) variant = EntityType.STRAY;
                else if (r == 1) variant = EntityType.BOGGED;
                else variant = EntityType.PARCHED;
                
                event.getLocation().getWorld().spawnEntity(event.getLocation(), variant);
                event.setCancelled(true);
            }
        }
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class RandomMobSizeMechanic extends DifficultyMechanic {

    private final Random random = new Random();

    public RandomMobSizeMechanic(JavaPlugin plugin) {
        super(plugin, 4);
    }

    // Runs on NORMAL priority so custom mobs running on HIGH can overwrite this size
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        LivingEntity entity = event.getEntity();
        
        // Skip custom mobs (like Realistic Spider or Parasite) spawned via world.spawn consumer
        if (EntityUtils.isCustomMob(entity)) return;
        
        AttributeInstance scale = entity.getAttribute(Attribute.SCALE);
        if (scale != null) {
            // Generate a random double between 0.75 and 1.25
            double min = 0.75;
            double max = 1.25;
            double randomScale = min + (max - min) * random.nextDouble();
            
            scale.setBaseValue(randomScale);
        }
    }
}

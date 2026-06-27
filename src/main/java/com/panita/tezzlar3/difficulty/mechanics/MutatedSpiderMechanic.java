package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MutatedSpiderMechanic extends DifficultyMechanic {

    private final Random random = new Random();

    public MutatedSpiderMechanic(JavaPlugin plugin) {
        super(plugin, 15);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpiderSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        LivingEntity entity = event.getEntity();
        EntityType type = entity.getType();
        
        if (type == EntityType.SPIDER || type == EntityType.CAVE_SPIDER) {
            if (EntityUtils.isCustomMob(entity)) return;
            if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
            
            int amountOfEffects = random.nextInt(3) + 1; // 1 to 3
            
            List<PotionEffect> pool = new ArrayList<>();
            pool.add(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 2)); // Strength 3
            pool.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1)); // Speed 2
            pool.add(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0)); // Invisibility
            pool.add(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 2)); // Regeneration 3
            pool.add(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0)); // Fire Resistance
            pool.add(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2)); // Resistance 3
            
            Collections.shuffle(pool);
            
            for (int i = 0; i < amountOfEffects; i++) {
                entity.addPotionEffect(pool.get(i));
            }
        }
    }
}

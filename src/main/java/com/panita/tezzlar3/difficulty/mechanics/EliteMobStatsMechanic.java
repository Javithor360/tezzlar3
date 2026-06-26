package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class EliteMobStatsMechanic extends DifficultyMechanic {

    public EliteMobStatsMechanic(JavaPlugin plugin) {
        super(plugin, 11);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return; // Never apply to players
        
        // Delay 1 tick to allow custom mobs (like Beekeeper or Realistic Spiders) 
        // to modify their base health and damage first.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!entity.isValid() || entity.isDead()) return;
            
            // Skip the Giga Magma Cube boss
            if (entity.getPersistentDataContainer().has(GigaMagmaCubeMechanic.BOSS_KEY, PersistentDataType.BYTE)) {
                return;
            }
            
            // Double Health
            AttributeInstance healthAttr = entity.getAttribute(Attribute.MAX_HEALTH);
            if (healthAttr != null) {
                double newHealth = healthAttr.getBaseValue() * 2.0;
                healthAttr.setBaseValue(newHealth);
                entity.setHealth(newHealth);
            }
            
            // Double Damage
            AttributeInstance damageAttr = entity.getAttribute(Attribute.ATTACK_DAMAGE);
            if (damageAttr != null) {
                damageAttr.setBaseValue(damageAttr.getBaseValue() * 2.0);
            }
        });
    }
}

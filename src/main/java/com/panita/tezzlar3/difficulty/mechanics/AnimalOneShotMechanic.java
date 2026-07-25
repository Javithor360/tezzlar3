package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Animals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AnimalOneShotMechanic extends DifficultyMechanic {

    public AnimalOneShotMechanic(JavaPlugin plugin) {
        super(plugin, 3);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAnimalDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Animals animal) {
            // Only apply the one-shot rule to direct entity attacks and projectiles
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK &&
                cause != EntityDamageEvent.DamageCause.PROJECTILE) {
                return;
            }
            
            AttributeInstance maxHealthAttr = animal.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealthAttr != null) {
                double maxHealth = maxHealthAttr.getValue();
                if (event.getFinalDamage() < maxHealth) {
                    event.setCancelled(true);
                }
            }
        }
    }
}

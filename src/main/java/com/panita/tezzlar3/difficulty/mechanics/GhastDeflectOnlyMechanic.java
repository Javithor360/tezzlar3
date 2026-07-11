package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LargeFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GhastDeflectOnlyMechanic extends DifficultyMechanic {

    public GhastDeflectOnlyMechanic(JavaPlugin plugin) {
        super(plugin, 25);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGhastDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Ghast) {
            
            // Only allow damage if it's from a Fireball
            if (event instanceof EntityDamageByEntityEvent byEntityEvent) {
                Entity damager = byEntityEvent.getDamager();
                if (damager instanceof LargeFireball || damager instanceof Fireball) {
                    return; // Allow the damage
                }
            }
            
            // Cancel all other sources of damage
            event.setCancelled(true);
        }
    }
}

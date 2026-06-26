package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Ghast;
import org.bukkit.entity.LargeFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GhastExplosionMechanic extends DifficultyMechanic {

    public GhastExplosionMechanic(JavaPlugin plugin) {
        super(plugin, 8); // Day 8
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof LargeFireball fireball) {
            if (fireball.getShooter() instanceof Ghast) {
                // We multiply the radius by 1.8.
                event.setRadius(event.getRadius() * 1.8f);
            }
        }
    }
}

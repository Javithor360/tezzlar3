package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CreeperExplosionMechanic extends DifficultyMechanic {

    public CreeperExplosionMechanic(JavaPlugin plugin) {
        super(plugin, 7); // Day 7
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Creeper) {
            // We multiply the radius by 1.6 as requested.
            event.setRadius(event.getRadius() * 1.6f);
        }
    }
}

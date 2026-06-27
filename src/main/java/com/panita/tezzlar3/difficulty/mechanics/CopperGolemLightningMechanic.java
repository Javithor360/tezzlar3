package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.chat.Messenger;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class CopperGolemLightningMechanic extends DifficultyMechanic {

    private final NamespacedKey REDIRECTED_KEY;

    public CopperGolemLightningMechanic(JavaPlugin plugin) {
        super(plugin, 12);
        REDIRECTED_KEY = new NamespacedKey(plugin, "lightning_redirected");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLightning(LightningStrikeEvent event) {
        if (!isActive()) return;
        
        // Prevent redirecting a lightning strike that was already redirected
        if (event.getLightning().getPersistentDataContainer().has(REDIRECTED_KEY, PersistentDataType.BYTE)) return;
        
        Location loc = event.getLightning().getLocation();
        Entity nearestGolem = null;
        double nearestDistSq = 64 * 64; // Max radius of 64 blocks
        
        for (Entity e : loc.getNearbyEntities(64, 64, 64)) {
            if (e.getType() == EntityType.COPPER_GOLEM) {
                double dist = e.getLocation().distanceSquared(loc);
                if (dist < nearestDistSq) {
                    nearestDistSq = dist;
                    nearestGolem = e;
                }
            }
        }
        
        if (nearestGolem != null) {
            event.setCancelled(true);
            nearestGolem.getWorld().spawn(nearestGolem.getLocation(), LightningStrike.class, newLightning -> {
                newLightning.getPersistentDataContainer().set(REDIRECTED_KEY, PersistentDataType.BYTE, (byte) 1);
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGolemDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity().getType() == EntityType.COPPER_GOLEM) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == EntityDamageEvent.DamageCause.LIGHTNING || 
                cause == EntityDamageEvent.DamageCause.FIRE || 
                cause == EntityDamageEvent.DamageCause.FIRE_TICK) {
                
                event.setCancelled(true);
                // Extinguish the golem immediately if it's on fire
                if (event.getEntity().getFireTicks() > 0) {
                    event.getEntity().setFireTicks(0);
                }
            }
        }
    }
}

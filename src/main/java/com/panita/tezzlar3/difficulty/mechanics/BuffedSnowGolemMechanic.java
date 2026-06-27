package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BuffedSnowGolemMechanic extends DifficultyMechanic {

    public BuffedSnowGolemMechanic(JavaPlugin plugin) {
        super(plugin, 13);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSnowGolemSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Snowman golem) {
            EntityUtils.trySetAttribute(golem, Attribute.MAX_HEALTH, 25.0);
            EntityUtils.trySetAttribute(golem, Attribute.ARMOR, 50.0);
            
            // Heal to max after increasing health capacity
            golem.setHealth(25.0);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSnowGolemDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Snowman) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == EntityDamageEvent.DamageCause.MELTING || cause == EntityDamageEvent.DamageCause.DROWNING) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSnowballHit(ProjectileHitEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Snowball snowball) {
            if (snowball.getShooter() instanceof Snowman) {
                // Generate harmless explosion for blocks (deals damage, does not break blocks or start fire)
                snowball.getWorld().createExplosion(snowball.getLocation(), 4.4f, false, false);
                
                // Extra particle for visual impact
                try {
                    snowball.getWorld().spawnParticle(Particle.valueOf("EXPLOSION_EMITTER"), snowball.getLocation(), 1);
                } catch (IllegalArgumentException ignored) {
                    try {
                        snowball.getWorld().spawnParticle(Particle.valueOf("EXPLOSION_LARGE"), snowball.getLocation(), 1);
                    } catch (IllegalArgumentException ignored2) {}
                }
            }
        }
    }
}

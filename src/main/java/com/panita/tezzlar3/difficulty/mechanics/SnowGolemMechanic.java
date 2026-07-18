package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class SnowGolemMechanic extends DifficultyMechanic {

    public SnowGolemMechanic(JavaPlugin plugin) {
        super(plugin, 13);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSnowGolemExplosionDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Snowman) {
            if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || 
                event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSnowGolemTargetBoss(EntityTargetEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Snowman) {
            Entity target = event.getTarget();
            if (target != null) {
                boolean isBoss = target.getPersistentDataContainer().has(GigaMagmaCubeMechanic.BOSS_KEY, PersistentDataType.BYTE) ||
                                 target.getPersistentDataContainer().has(GlacialBonebreakerMechanic.BOSS_KEY, PersistentDataType.BYTE);
                
                if (isBoss) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSnowballHitBoss(EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        
        if (event.getDamager() instanceof Snowball snowball) {
            if (snowball.getShooter() instanceof Snowman) {
                Entity target = event.getEntity();
                boolean isBoss = target.getPersistentDataContainer().has(GigaMagmaCubeMechanic.BOSS_KEY, PersistentDataType.BYTE) ||
                                 target.getPersistentDataContainer().has(GlacialBonebreakerMechanic.BOSS_KEY, PersistentDataType.BYTE);
                
                if (isBoss) {
                    event.setCancelled(true);
                }
            }
        }
    }
}

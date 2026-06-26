package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Strider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.Bukkit;

public class FastStriderMechanic extends DifficultyMechanic {

    public FastStriderMechanic(JavaPlugin plugin) {
        super(plugin, 9); // Day 9
        
        // Update existing striders in loaded chunks
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!isActive()) return;
            for (World world : Bukkit.getWorlds()) {
                for (Entity e : world.getEntitiesByClass(Strider.class)) {
                    doubleSpeed((Strider) e);
                }
            }
        }, 60L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getEntityType() == EntityType.STRIDER) {
            doubleSpeed((Strider) event.getEntity());
        }
    }
    
    private void doubleSpeed(Strider strider) {
        AttributeInstance attr = strider.getAttribute(Attribute.MOVEMENT_SPEED);
        // Default movement speed for strider is 0.174
        if (attr != null && attr.getBaseValue() < 0.3) {
            attr.setBaseValue(attr.getBaseValue() * 2.0);
        }
    }
}

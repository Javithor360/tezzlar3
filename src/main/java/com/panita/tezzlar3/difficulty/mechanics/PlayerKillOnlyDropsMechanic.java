package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerKillOnlyDropsMechanic extends DifficultyMechanic {

    public PlayerKillOnlyDropsMechanic(JavaPlugin plugin) {
        super(plugin, 10); // Day 10
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isActive()) return;

        // If the entity is a mob and it wasn't killed by a player directly
        if (event.getEntity() instanceof Mob) {
            if (event.getEntity().getKiller() == null) {
                // Clear all item drops
                event.getDrops().clear();
            }
        }
    }
}

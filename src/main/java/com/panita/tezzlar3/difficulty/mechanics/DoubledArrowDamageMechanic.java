package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DoubledArrowDamageMechanic extends DifficultyMechanic {

    public DoubledArrowDamageMechanic(JavaPlugin plugin) {
        super(plugin, 10); // Day 10
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArrowDamage(EntityDamageByEntityEvent event) {
        if (!isActive()) return;

        if (event.getEntity() instanceof Player) {
            if (event.getDamager() instanceof Arrow arrow) {
                if (!(arrow.getShooter() instanceof Player)) {
                    event.setDamage(event.getDamage() * 2.0);
                }
            }
        }
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Stray;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class StrayFreezeMechanic extends DifficultyMechanic {

    public StrayFreezeMechanic(JavaPlugin plugin) {
        super(plugin, 14);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Player player) {
            if (event.getDamager() instanceof Arrow arrow) {
                if (arrow.getShooter() instanceof Stray) {
                    // Apply native freezing effect for 3 seconds past the freeze threshold
                    player.setFreezeTicks(player.getMaxFreezeTicks() + 60);
                }
            }
        }
    }
}

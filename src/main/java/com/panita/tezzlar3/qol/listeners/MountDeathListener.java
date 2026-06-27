package com.panita.tezzlar3.qol.listeners;

import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;

public class MountDeathListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onRiderDeath(EntityDismountEvent event) {
        if (event.getEntity() instanceof LivingEntity rider) {
            // Check if the rider is dying or already dead when dismounting
            if (rider.isDead() || rider.getHealth() <= 0) {
                if (event.getDismounted() instanceof Animals mount) {
                    // Kill the mount indirectly to prevent animal accumulation
                    mount.damage(10000.0);
                }
            }
        }
    }
}

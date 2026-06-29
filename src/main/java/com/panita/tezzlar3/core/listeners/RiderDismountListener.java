package com.panita.tezzlar3.core.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;

public class RiderDismountListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDismount(EntityDismountEvent event) {
        org.bukkit.entity.Entity rider = event.getEntity();
        org.bukkit.entity.Entity vehicle = event.getDismounted();
        
        // If the rider is not a player, and they dismount a living vehicle that isn't a player, the vehicle dies
        if (!(rider instanceof Player) && vehicle instanceof LivingEntity && !(vehicle instanceof Player)) {
            ((LivingEntity) vehicle).setHealth(0.0);
        }
    }
}

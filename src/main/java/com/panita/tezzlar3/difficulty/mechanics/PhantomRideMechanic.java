package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PhantomRideMechanic extends DifficultyMechanic {

    public PhantomRideMechanic(JavaPlugin plugin) {
        super(plugin, 5); // Day 5
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPhantomAttack(EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        
        if (event.getDamager() instanceof Phantom phantom && event.getEntity() instanceof Player player) {
            // When Phantom hits a Player, the Player is forced to ride the Phantom
            phantom.addPassenger(player);
        }
    }
}

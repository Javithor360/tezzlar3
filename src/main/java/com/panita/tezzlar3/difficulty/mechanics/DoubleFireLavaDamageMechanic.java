package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;

public class DoubleFireLavaDamageMechanic extends DifficultyMechanic {

    public DoubleFireLavaDamageMechanic(JavaPlugin plugin) {
        super(plugin, 23);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireLavaDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Player) {
            DamageCause cause = event.getCause();
            if (cause == DamageCause.LAVA || cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK) {
                event.setDamage(event.getDamage() * 2.0);
            }
        }
    }
}

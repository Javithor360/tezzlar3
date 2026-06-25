package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.chat.Messenger;

public class DoubleFallDamageMechanic extends DifficultyMechanic {

    public DoubleFallDamageMechanic(JavaPlugin plugin) {
        super(plugin, 3);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Player player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setDamage(event.getDamage() * 2.0);
        }
    }
}

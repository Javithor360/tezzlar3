package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DoubleDamageMechanic extends DifficultyMechanic {

    public DoubleDamageMechanic(JavaPlugin plugin) {
        super(plugin, 19);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!isActive()) return;

        if (event.getEntity() instanceof Player) {
            event.setDamage(event.getDamage() * 2.0);
        }
    }
}

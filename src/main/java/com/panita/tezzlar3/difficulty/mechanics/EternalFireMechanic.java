package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;

public class EternalFireMechanic extends DifficultyMechanic {

    public EternalFireMechanic(JavaPlugin plugin) {
        super(plugin, 14);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFireTick(EntityDamageEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Player player) {
            DamageCause cause = event.getCause();
            if (cause == DamageCause.FIRE_TICK || cause == DamageCause.FIRE) {
                // Renew fire for 10 seconds (200 ticks) so it never naturally extinguishes on land
                if (player.getFireTicks() < 200) {
                    player.setFireTicks(200);
                }
            }
        }
    }
}

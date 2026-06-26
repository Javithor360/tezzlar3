package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class FastDrowningMechanic extends DifficultyMechanic {

    public FastDrowningMechanic(JavaPlugin plugin) {
        super(plugin, 8); // Day 8
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAirChange(EntityAirChangeEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Player player) {
            int currentAir = player.getRemainingAir();
            int newAir = event.getAmount();
            
            // If air is decreasing...
            if (newAir < currentAir) {
                int difference = currentAir - newAir;
                // Double the loss.
                // This applies to both positive air (bubbles) and negative air (damage countdown).
                // Vanilla counts down from 0 to -20. Since we subtract by 2 (difference * 2),
                // it will perfectly land on -20 and trigger vanilla's drowning damage.
                event.setAmount(newAir - difference);
            }
        }
    }
}

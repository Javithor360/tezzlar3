package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class FastHungerMechanic extends DifficultyMechanic {

    public FastHungerMechanic(JavaPlugin plugin) {
        super(plugin, 12);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Player player) {
            int currentFood = player.getFoodLevel();
            int newFood = event.getFoodLevel();
            
            // If food level drops (running, jumping, regenerating health)
            if (newFood < currentFood) {
                int difference = currentFood - newFood;
                // Double the loss amount
                event.setFoodLevel(Math.max(0, newFood - difference));
            }
        }
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CropGrowthMechanic extends DifficultyMechanic {

    public CropGrowthMechanic(JavaPlugin plugin) {
        super(plugin, 12);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCropGrow(BlockGrowEvent event) {
        if (!isActive()) return;

        if (event.getNewState().getBlockData() instanceof Ageable ageable) {
            int currentNewAge = ageable.getAge();
            int maxAge = ageable.getMaximumAge();
            
            // Advance by one extra step to simulate 2x growth speed
            if (currentNewAge < maxAge) {
                ageable.setAge(Math.min(maxAge, currentNewAge + 1));
                event.getNewState().setBlockData(ageable);
            }
        }
    }
}

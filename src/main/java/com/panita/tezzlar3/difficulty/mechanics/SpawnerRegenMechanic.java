package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.difficulty.util.SpawnerRegenManager;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnerRegenMechanic extends DifficultyMechanic {
    private final SpawnerRegenManager manager;

    public SpawnerRegenMechanic(JavaPlugin plugin, SpawnerRegenManager manager) {
        super(plugin, 11); // Active from day 11
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerBreak(BlockBreakEvent event) {
        if (!isActive()) return;

        if (event.getBlock().getType() == Material.SPAWNER) {
            BlockState state = event.getBlock().getState();
            if (state instanceof CreatureSpawner spawner) {
                // Triple the experience dropped
                event.setExpToDrop(event.getExpToDrop() * 3);
                
                // Ensure the spawner block itself doesn't drop
                event.setDropItems(false);

                // Add to the regeneration manager with 10 minutes (600,000 ms) delay
                manager.addSpawner(event.getBlock().getLocation(), spawner, 10L * 60L * 1000L);
            }
        }
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.difficulty.util.SpawnerRegenManager;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.Block;
import java.util.Iterator;

public class SpawnerRegenMechanic extends DifficultyMechanic {
    private final SpawnerRegenManager manager;

    public SpawnerRegenMechanic(JavaPlugin plugin, SpawnerRegenManager manager) {
        super(plugin, 15); // Active from day 15
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerBreak(BlockBreakEvent event) {
        if (!isActive()) return;

        if (event.getBlock().getType() == Material.SPAWNER) {
            // Allow admins to break spawners permanently while sneaking
            if (event.getPlayer().isSneaking() && (event.getPlayer().isOp() || event.getPlayer().hasPermission("tezzlar.admin"))) {
                return;
            }

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!isActive()) return;
        handleExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!isActive()) return;
        handleExplosion(event.blockList());
    }

    private void handleExplosion(java.util.List<Block> blocks) {
        Iterator<Block> iterator = blocks.iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (block.getType() == Material.SPAWNER) {
                BlockState state = block.getState();
                if (state instanceof CreatureSpawner spawner) {
                    manager.addSpawner(block.getLocation(), spawner, 10L * 60L * 1000L);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!isActive()) return;
        if (event.getBlock().getType() == Material.SPAWNER) {
            BlockState state = event.getBlock().getState();
            if (state instanceof CreatureSpawner spawner) {
                manager.addSpawner(event.getBlock().getLocation(), spawner, 10L * 60L * 1000L);
            }
        }
    }
}

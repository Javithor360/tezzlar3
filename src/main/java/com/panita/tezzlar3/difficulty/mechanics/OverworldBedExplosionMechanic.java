package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class OverworldBedExplosionMechanic extends DifficultyMechanic {

    public OverworldBedExplosionMechanic(JavaPlugin plugin) {
        super(plugin, 5);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBedInteract(PlayerInteractEvent event) {
        if (!isActive()) return;
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getBlockData() instanceof Bed) {
                World world = block.getWorld();
                
                // If the world is the Overworld (NORMAL environment), explode the bed
                if (world.getEnvironment() == World.Environment.NORMAL) {
                    
                    // Cancel the interaction completely before the server sets the spawn point or sleep cycle
                    event.setCancelled(true);
                    
                    // Remove the bed block first so it doesn't drop as an item
                    block.setType(Material.AIR);
                    
                    // Create explosion: location, power (5.0 is standard nether bed), setFire (true), breakBlocks (true)
                    world.createExplosion(block.getLocation(), 7.0f, true, true);
                }
            }
        }
    }
}

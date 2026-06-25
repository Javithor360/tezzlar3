package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class OverworldBedExplosionMechanic extends DifficultyMechanic {

    public OverworldBedExplosionMechanic(JavaPlugin plugin) {
        super(plugin, 5);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!isActive()) return;
        
        Block bed = event.getBed();
        World world = bed.getWorld();
        
        // If the world is the Overworld (NORMAL environment), explode the bed
        if (world.getEnvironment() == World.Environment.NORMAL) {
            event.setCancelled(true);
            
            // Remove the bed block first so it doesn't drop as an item
            bed.setType(Material.AIR);
            
            // Create explosion: location, power (5.0 is standard nether bed), setFire (true), breakBlocks (true)
            world.createExplosion(bed.getLocation(), 5.0f, true, true);
        }
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.World;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.util.EntityUtils;

import java.util.Random;

public class NetherBlazeSpawnsMechanic extends DifficultyMechanic {

    private final Random random = new Random();

    public NetherBlazeSpawnsMechanic(JavaPlugin plugin) {
        super(plugin, 11);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNetherSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
        
        if (event.getEntity().getWorld().getEnvironment() == World.Environment.NETHER) {
            // Check if it's a monster and not already a Blaze
            if (event.getEntity() instanceof Monster && !(event.getEntity() instanceof Blaze)) {
                if (random.nextDouble() < 0.10) {
                    event.setCancelled(true);
                    int amount = random.nextInt(4) + 1; // 1 to 4 blazes
                    for (int i = 0; i < amount; i++) {
                        EntityUtils.spawnNatural(event.getLocation(), EntityType.BLAZE);
                    }
                }
            }
        }
    }
}

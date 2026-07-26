package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EnderDragonBuffMechanic extends DifficultyMechanic {

    public EnderDragonBuffMechanic(JavaPlugin plugin) {
        super(plugin, 28);
        
        // Start a recurring task to check existing dragons since they might already be spawned
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            for (World world : Bukkit.getWorlds()) {
                if (world.getEnvironment() == World.Environment.THE_END) {
                    for (EnderDragon dragon : world.getEntitiesByClass(EnderDragon.class)) {
                        buffDragonIfNeeded(dragon);
                    }
                }
            }
        }, 100L, 100L); // every 5 seconds
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDragonSpawn(EntitySpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof EnderDragon dragon) {
            buffDragonIfNeeded(dragon);
        }
    }
    
    private void buffDragonIfNeeded(EnderDragon dragon) {
        // Check if there are players nearby
        boolean playerNearby = false;
        for (Entity entity : dragon.getNearbyEntities(200, 200, 200)) {
            if (entity instanceof Player player) {
                if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                    playerNearby = true;
                    break;
                }
            }
        }
        
        if (playerNearby) {
            AttributeInstance maxHealthAttr = dragon.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealthAttr != null && maxHealthAttr.getBaseValue() < 1500.0) {
                maxHealthAttr.setBaseValue(1500.0);
                dragon.setHealth(1500.0);
            }
        }
    }
}

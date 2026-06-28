package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NetherRoofWitherMechanic extends DifficultyMechanic {

    public NetherRoofWitherMechanic(JavaPlugin plugin) {
        super(plugin, 16);
        
        // Check every second (20 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld().getEnvironment() == World.Environment.NETHER) {
                    if (player.getLocation().getY() >= 127) {
                        // Check if standing on bedrock
                        if (player.getLocation().getBlock().getRelative(0, -1, 0).getType() == Material.BEDROCK) {
                            if (Math.random() < 0.10) {
                                Entity wither = player.getWorld().spawnEntity(player.getLocation().add(0, 1, 0), EntityType.WITHER);
                                // Mark the Wither so it doesn't drop a nether star
                                wither.addScoreboardTag("roof_wither");
                            }
                        }
                    }
                }
            }
        }, 20L, 20L);
    }

    @EventHandler
    public void onWitherDeath(EntityDeathEvent event) {
        if (!isActive()) return;
        
        if (event.getEntityType() == EntityType.WITHER) {
            if (event.getEntity().getScoreboardTags().contains("roof_wither")) {
                // Remove any Nether Star from drops
                event.getDrops().removeIf(item -> item.getType() == Material.NETHER_STAR);
            }
        }
    }
}

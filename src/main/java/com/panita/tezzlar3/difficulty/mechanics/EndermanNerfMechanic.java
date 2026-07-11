package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class EndermanNerfMechanic extends DifficultyMechanic {

    public EndermanNerfMechanic(JavaPlugin plugin) {
        super(plugin, 25);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) return;
                
                for (World world : Bukkit.getWorlds()) {
                    for (Enderman enderman : world.getEntitiesByClass(Enderman.class)) {
                        if (enderman.getTarget() == null) {
                            enrageEnderman(enderman);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 200L); // Every 10 seconds
    }

    private void enrageEnderman(Enderman enderman) {
        double radius = enderman.getWorld().getEnvironment() == World.Environment.THE_END ? 16.0 : 64.0;
        
        Player target = null;
        double closestDist = Double.MAX_VALUE;
        
        for (Entity e : enderman.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Player p && p.isValid() && !p.isDead()) {
                double dist = p.getLocation().distanceSquared(enderman.getLocation());
                if (dist < closestDist) {
                    closestDist = dist;
                    target = p;
                }
            }
        }
        
        if (target != null) {
            enderman.setTarget(target);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEndermanTeleport(EntityTeleportEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Enderman) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEndermanSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Enderman enderman) {
            // Give the spawn a tiny delay so the entity is fully in the world
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (enderman.isValid() && !enderman.isDead()) {
                    enrageEnderman(enderman);
                }
            });
        }
    }
}

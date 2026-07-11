package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class PhantomRiderMechanic extends DifficultyMechanic {

    private final Random random = new Random();
    private final NamespacedKey RIDER_KEY;
    private final NamespacedKey MOUNT_KEY;

    public PhantomRiderMechanic(JavaPlugin plugin) {
        super(plugin, 23);
        RIDER_KEY = new NamespacedKey(plugin, "phantom_rider_shulker");
        MOUNT_KEY = new NamespacedKey(plugin, "phantom_rider_mount");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        if (event.getSpawnReason() != SpawnReason.NATURAL && event.getSpawnReason() != SpawnReason.DEFAULT) return;

        World.Environment env = event.getLocation().getWorld().getEnvironment();
        EntityType type = event.getEntityType();
        
        boolean shouldTransform = false;

        if (env == World.Environment.NORMAL && type == EntityType.PHANTOM) {
            // 5% in Overworld
            if (random.nextDouble() <= 0.05) {
                shouldTransform = true;
            }
        } else if ((env == World.Environment.NETHER || env == World.Environment.THE_END) && type == EntityType.ENDERMAN) {
            // 1% in Nether/End
            if (random.nextDouble() <= 0.01) {
                shouldTransform = true;
            }
        }

        if (shouldTransform) {
            event.setCancelled(true);
            
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!event.getLocation().getChunk().isLoaded()) return;
                
                Phantom phantom = (Phantom) event.getLocation().getWorld().spawnEntity(event.getLocation(), EntityType.PHANTOM);
                Shulker shulker = (Shulker) event.getLocation().getWorld().spawnEntity(event.getLocation(), EntityType.SHULKER);
                
                phantom.getPersistentDataContainer().set(MOUNT_KEY, PersistentDataType.BYTE, (byte) 1);
                shulker.getPersistentDataContainer().set(RIDER_KEY, PersistentDataType.BYTE, (byte) 1);
                
                phantom.addPassenger(shulker);
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Phantom phantom) {
            if (phantom.getPersistentDataContainer().has(MOUNT_KEY, PersistentDataType.BYTE)) {
                // Check if it has a Shulker passenger
                for (Entity passenger : phantom.getPassengers()) {
                    if (passenger instanceof Shulker shulker && shulker.getPersistentDataContainer().has(RIDER_KEY, PersistentDataType.BYTE)) {
                        if (!shulker.isDead()) {
                            // Cancel damage to phantom and redirect to shulker
                            event.setCancelled(true);
                            shulker.damage(event.getDamage());
                            return; // Only redirect once
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(EntityTeleportEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Shulker shulker) {
            if (shulker.getPersistentDataContainer().has(RIDER_KEY, PersistentDataType.BYTE)) {
                // Prevent the Shulker from teleporting so it doesn't dismount
                event.setCancelled(true);
            }
        }
    }
}

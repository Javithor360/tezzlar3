package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import org.bukkit.Location;

import java.util.Random;

public class PhantomRiderMechanic extends DifficultyMechanic {

    private final Random random = new Random();
    private final NamespacedKey RIDER_KEY;
    private final NamespacedKey MOUNT_KEY;

    public PhantomRiderMechanic(JavaPlugin plugin) {
        super(plugin, 23);
        RIDER_KEY = new NamespacedKey(plugin, "phantom_rider_shulker");
        MOUNT_KEY = new NamespacedKey(plugin, "phantom_rider_mount");
        
        CustomMobManager.register(CustomMobType.PHANTOM_RIDER, this::spawnManual);
    }

    public void spawnManual(Location loc) {
        Phantom phantom = (Phantom) EntityUtils.spawnNatural(loc, EntityType.PHANTOM);
        Skeleton skeleton = (Skeleton) EntityUtils.spawnNatural(loc, EntityType.SKELETON);
        
        phantom.getPersistentDataContainer().set(MOUNT_KEY, PersistentDataType.BYTE, (byte) 1);
        skeleton.getPersistentDataContainer().set(RIDER_KEY, PersistentDataType.BYTE, (byte) 1);
        
        EntityUtils.setCustomName(skeleton, "<#8A2BE2>Phantom Rider</#8A2BE2>", false);
        
        phantom.addPassenger(skeleton);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;

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
                
                spawnManual(event.getLocation());
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Phantom phantom) {
            if (phantom.getPersistentDataContainer().has(MOUNT_KEY, PersistentDataType.BYTE)) {
                // Check if it has a Skeleton passenger
                for (Entity passenger : phantom.getPassengers()) {
                    if (passenger instanceof Skeleton skeleton && skeleton.getPersistentDataContainer().has(RIDER_KEY, PersistentDataType.BYTE)) {
                        if (!skeleton.isDead()) {
                            // Cancel damage to phantom and redirect to skeleton
                            event.setCancelled(true);
                            skeleton.damage(event.getDamage());
                            return; // Only redirect once
                        }
                    }
                }
            }
        }
    }
}

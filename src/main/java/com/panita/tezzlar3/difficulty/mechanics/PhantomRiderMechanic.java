package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
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
        Shulker rider = (Shulker) EntityUtils.spawnNatural(loc, EntityType.SHULKER);
        
        phantom.getPersistentDataContainer().set(MOUNT_KEY, PersistentDataType.BYTE, (byte) 1);
        rider.getPersistentDataContainer().set(RIDER_KEY, PersistentDataType.BYTE, (byte) 1);
        
        EntityUtils.setCustomName(rider, "<#8A2BE2>Phantom Rider</#8A2BE2>", false);
        
        phantom.addPassenger(rider);
        
        // Explicitly apply Elite stats to guarantee they get buffed
        NamespacedKey ELITE_KEY = new NamespacedKey(plugin, "elite_buffed");
        int currentDay = TimeManager.getCurrentDay();
        double healthMultiplier = currentDay >= 22 ? 3.0 : 2.0;
        double damageMultiplier = currentDay >= 22 ? 4.0 : 2.0;
        
        for (LivingEntity ent : new LivingEntity[]{phantom, rider}) {
            if (ent.getPersistentDataContainer().has(ELITE_KEY, PersistentDataType.BYTE)) continue;
            
            ent.getPersistentDataContainer().set(ELITE_KEY, PersistentDataType.BYTE, (byte) 1);
            
            AttributeInstance healthAttr = ent.getAttribute(Attribute.MAX_HEALTH);
            if (healthAttr != null) {
                double newHealth = healthAttr.getBaseValue() * healthMultiplier;
                EntityUtils.trySetAttribute(ent, Attribute.MAX_HEALTH, newHealth);
                try { ent.setHealth(newHealth); } catch (Exception ignored) {}
            }
            
            org.bukkit.attribute.AttributeInstance damageAttr = ent.getAttribute(org.bukkit.attribute.Attribute.ATTACK_DAMAGE);
            if (damageAttr != null) {
                EntityUtils.trySetAttribute(ent, org.bukkit.attribute.Attribute.ATTACK_DAMAGE, damageAttr.getBaseValue() * damageMultiplier);
            }
            
            if (currentDay >= 22) {
                org.bukkit.attribute.AttributeInstance followRangeAttr = ent.getAttribute(org.bukkit.attribute.Attribute.FOLLOW_RANGE);
                if (followRangeAttr != null) {
                    EntityUtils.trySetAttribute(ent, org.bukkit.attribute.Attribute.FOLLOW_RANGE, followRangeAttr.getBaseValue() * 2.0);
                }
            }
        }
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
                // Check if it has a Shulker passenger
                for (Entity passenger : phantom.getPassengers()) {
                    if (passenger instanceof Shulker rider && rider.getPersistentDataContainer().has(RIDER_KEY, PersistentDataType.BYTE)) {
                        if (!rider.isDead()) {
                            // Cancel damage to phantom and redirect to shulker
                            event.setCancelled(true);
                            rider.damage(event.getDamage());
                            return; // Only redirect once
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShulkerTeleport(EntityTeleportEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Shulker shulker) {
            if (shulker.getPersistentDataContainer().has(RIDER_KEY, PersistentDataType.BYTE)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRiderBulletHit(EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        
        if (event.getDamager() instanceof org.bukkit.entity.ShulkerBullet bullet) {
            if (bullet.getShooter() instanceof Shulker shulker && shulker.getPersistentDataContainer().has(RIDER_KEY, PersistentDataType.BYTE)) {
                // Apply the EliteMobStats multiplier explicitly because Shulkers don't have melee ATTACK_DAMAGE
                double multiplier = com.panita.tezzlar3.timeline.util.TimeManager.getCurrentDay() >= 22 ? 4.0 : 2.0;
                event.setDamage(event.getDamage() * multiplier);
            }
        }
    }
}

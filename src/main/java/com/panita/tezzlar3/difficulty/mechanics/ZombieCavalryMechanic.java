package com.panita.tezzlar3.difficulty.mechanics;
import com.panita.tezzlar3.core.util.EntityUtils;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.World;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class ZombieCavalryMechanic extends DifficultyMechanic {

    private final Random random = new Random();
    private final EntityType[] MOUNTS = {
            EntityType.HORSE, EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE, 
            EntityType.LLAMA, EntityType.MULE, EntityType.DONKEY, EntityType.CAMEL,
            EntityType.CAMEL_HUSK
    };

    public ZombieCavalryMechanic(JavaPlugin plugin) {
        super(plugin, 6);
    }

    // Runs on HIGHEST priority to execute after the RandomMobGearMechanic (HIGH)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onZombieSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        LivingEntity entity = event.getEntity();
        
        if (event.getSpawnReason() == SpawnReason.SPAWNER) {
            int day = TimeManager.getCurrentDay();
            if (day >= 16 && day <= 18) return;
        }
        
        EntityType type = entity.getType();
        
        // Verify if it belongs to the Zombie family
        boolean isZombie = type == EntityType.ZOMBIE || type == EntityType.ZOMBIE_VILLAGER || type == EntityType.DROWNED || type == EntityType.HUSK;
        
        if (isZombie) {
            // If in the Overworld, only allow cavalry on the surface (Y >= 63)
            // Other dimensions do not have this height restriction
            World world = entity.getWorld();
            if (world.getEnvironment() == World.Environment.NORMAL && entity.getLocation().getY() < 63) {
                return;
            }
            
            // Delay 2 ticks to ensure RandomMobGearMechanic (which runs on 1 tick delay) has already equipped the zombie
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!entity.isValid() || entity.isDead()) return;
                
                EntityEquipment eq = entity.getEquipment();
                if (eq != null) {
                    ItemStack mainHand = eq.getItemInMainHand();
                    // If carrying a spear, grant a guaranteed mount
                    if (mainHand != null && mainHand.getType().name().contains("SPEAR")) {
                        
                        EntityType mountType = MOUNTS[random.nextInt(MOUNTS.length)];
                        LivingEntity mount = (LivingEntity) EntityUtils.spawnNatural(entity.getLocation(), mountType);
                        
                        if (mount != null) {
                            // Double the speed of the mount
                            AttributeInstance speed = mount.getAttribute(Attribute.MOVEMENT_SPEED);
                            if (speed != null) speed.setBaseValue(speed.getBaseValue() * 2.0);
                            
                            mount.addPassenger(entity);

                            // Sync mount with the zombie (bypasses teleport issues with SpawnAnimations)
                            // Also kills the mount if the zombie dies (to prevent lag/empty mounts)
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (entity.isDead() || !entity.isValid()) {
                                        if (mount.isValid()) mount.remove();
                                        this.cancel();
                                        return;
                                    }
                                    if (mount.isDead() || !mount.isValid()) {
                                        if (entity.isValid()) entity.remove();
                                        this.cancel();
                                        return;
                                    }
                                    
                                    // If datapack teleported the zombie, it dismounts. We force it back and teleport the mount.
                                    if (!mount.getPassengers().contains(entity)) {
                                        mount.teleport(entity.getLocation());
                                        mount.addPassenger(entity);
                                    }
                                }
                            }.runTaskTimer(plugin, 1L, 1L);
                        }
                    }
                }
            }, 2L);
        }
    }

    // Prevent zombies from suffocating (especially useful when riding fast horses near walls/trees)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onZombieSuffocate(EntityDamageEvent event) {
        if (!isActive()) return;
        
        if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION || event.getCause() == EntityDamageEvent.DamageCause.CRAMMING) {
            if (event.getEntity() instanceof Zombie zombie) {
                // If it's a zombie, we make it immune to suffocation to prevent cavalry from dying in walls
                event.setCancelled(true);
            } else if (event.getEntity().getPersistentDataContainer().has(new org.bukkit.NamespacedKey(plugin, "is_cavalry_mount"), org.bukkit.persistence.PersistentDataType.BYTE)) {
                // If the entity is a mount, make it immune to suffocation and cramming
                event.setCancelled(true);
            }
        }
    }
}

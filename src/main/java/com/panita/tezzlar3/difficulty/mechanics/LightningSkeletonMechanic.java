package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class LightningSkeletonMechanic extends DifficultyMechanic {

    private final Random random = new Random();
    private final NamespacedKey LIGHTNING_KEY;

    public LightningSkeletonMechanic(JavaPlugin plugin) {
        super(plugin, 8); // Day 8
        LIGHTNING_KEY = new NamespacedKey(plugin, "is_lightning");
        
        // Passive lightning strike every 120 seconds (2400 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntitiesByClasses(Skeleton.class)) {
                    if (entity.getPersistentDataContainer().has(LIGHTNING_KEY, PersistentDataType.BYTE)) {
                        // Strike a visual lightning bolt on the skeleton
                        world.strikeLightningEffect(entity.getLocation());
                    }
                }
            }
        }, 60L, 2400L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSkeletonSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        if (event.getEntityType() != EntityType.SKELETON) return;
        if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
        if (EntityUtils.isCustomMob(event.getEntity())) return;
        
        // 10% spawn chance
        if (random.nextInt(100) >= 10) return;
        
        Skeleton skeleton = (Skeleton) event.getEntity();
        
        skeleton.getPersistentDataContainer().set(LIGHTNING_KEY, PersistentDataType.BYTE, (byte) 1);
        EntityUtils.setCustomName(skeleton, "<color:#894B0A>Esqueleto Relámpago</color>");
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!skeleton.isValid() || skeleton.isDead()) return;
            
            ItemStack helmet = new ItemStack(Material.COPPER_HELMET);
            helmet.addUnsafeEnchantment(Enchantment.PROJECTILE_PROTECTION, 5);
            
            ItemStack chestplate = new ItemStack(Material.COPPER_CHESTPLATE);
            chestplate.addUnsafeEnchantment(Enchantment.PROTECTION, 2);
            chestplate.addUnsafeEnchantment(Enchantment.THORNS, 4);
            
            ItemStack leggings = new ItemStack(Material.COPPER_LEGGINGS);
            leggings.addUnsafeEnchantment(Enchantment.PROTECTION, 2);
            leggings.addUnsafeEnchantment(Enchantment.THORNS, 4);
            
            ItemStack boots = new ItemStack(Material.COPPER_BOOTS);
            boots.addUnsafeEnchantment(Enchantment.PROJECTILE_PROTECTION, 5);
            
            EntityUtils.equipArmor(skeleton, helmet, chestplate, leggings, boots, 0.0f);
            
            ItemStack bow = new ItemStack(Material.BOW);
            bow.addUnsafeEnchantment(Enchantment.POWER, 5);
            skeleton.getEquipment().setItemInMainHand(bow);
            skeleton.getEquipment().setItemInMainHandDropChance(0.0f);
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onArrowHit(ProjectileHitEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Arrow arrow) {
            if (arrow.getShooter() instanceof Skeleton skeleton) {
                if (skeleton.getPersistentDataContainer().has(LIGHTNING_KEY, PersistentDataType.BYTE)) {
                    // Spawn a real lightning bolt at the hit location
                    Location hitLoc = event.getHitEntity() != null 
                        ? event.getHitEntity().getLocation() 
                        : (event.getHitBlock() != null ? event.getHitBlock().getLocation() : null);
                        
                    if (hitLoc != null) {
                        hitLoc.getWorld().strikeLightning(hitLoc);
                    }
                }
            }
        }
    }
}

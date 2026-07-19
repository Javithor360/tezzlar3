package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.timeline.util.TimeManager;
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
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;

import java.util.Random;

public class LightningSkeletonMechanic extends DifficultyMechanic {

    private final Random random = new Random();
    private final NamespacedKey LIGHTNING_KEY;
    private final Set<Skeleton> activeLightningSkeletons = new HashSet<>();

    public LightningSkeletonMechanic(JavaPlugin plugin) {
        super(plugin, 8); // Day 8
        LIGHTNING_KEY = new NamespacedKey(plugin, "is_lightning");
        CustomMobManager.register(CustomMobType.LIGHTNING_SKELETON, this::spawnManual);

        
        // Pre-populate loaded lightning skeletons
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClasses(Skeleton.class)) {
                if (entity.getPersistentDataContainer().has(LIGHTNING_KEY, PersistentDataType.BYTE)) {
                    activeLightningSkeletons.add((Skeleton) entity);
                }
            }
        }

        // Passive lightning strike every 120 seconds (2400 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            Iterator<Skeleton> iterator = activeLightningSkeletons.iterator();
            while (iterator.hasNext()) {
                Skeleton skeleton = iterator.next();
                if (!skeleton.isValid() || skeleton.isDead()) {
                    iterator.remove();
                    continue;
                }
                
                // Strike a visual lightning bolt on the skeleton
                skeleton.getWorld().strikeLightningEffect(skeleton.getLocation());
            }
        }, 60L, 2400L);
    }

    public void spawnManual(Location loc) {
        Skeleton skeleton = (Skeleton) EntityUtils.spawnNatural(loc, EntityType.SKELETON);
        if (skeleton != null) {
            CustomMobManager.tagCustomMob(skeleton, CustomMobType.LIGHTNING_SKELETON);
            transform(skeleton);
        }
    }
    
    private void transform(Skeleton skeleton) {
        skeleton.getPersistentDataContainer().set(LIGHTNING_KEY, PersistentDataType.BYTE, (byte) 1);
        EntityUtils.setCustomName(skeleton, "<#894B0A>Esqueleto Relámpago</#894B0A>");
        activeLightningSkeletons.add(skeleton);
        
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
            int powerLevel = TimeManager.getCurrentDay() >= 12 ? 35 : 5;
            bow.addUnsafeEnchantment(Enchantment.POWER, powerLevel);
            skeleton.getEquipment().setItemInMainHand(bow);
            skeleton.getEquipment().setItemInMainHandDropChance(0.0f);
        });
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
        transform(skeleton);
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
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity e : event.getChunk().getEntities()) {
            if (e instanceof Skeleton skeleton && skeleton.getPersistentDataContainer().has(LIGHTNING_KEY, PersistentDataType.BYTE)) {
                activeLightningSkeletons.add(skeleton);
            }
        }
    }
}

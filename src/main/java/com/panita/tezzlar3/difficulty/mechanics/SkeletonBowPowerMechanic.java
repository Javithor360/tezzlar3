package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.Tezzlar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.concurrent.ThreadLocalRandom;

public class SkeletonBowPowerMechanic extends DifficultyMechanic implements Listener {

    public SkeletonBowPowerMechanic(JavaPlugin plugin) {
        super(plugin, 11);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSkeletonSpawn(EntitySpawnEvent event) {
        if (!isActive()) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) return;

        // Delay by 2 ticks to ensure it runs AFTER MobGearUtils and other mechanics which delay by 1 tick
        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
            if (!entity.isValid() || entity.isDead()) return;

            // Check if entity is any skeleton or PARCHED
            boolean isTargetMob = entity instanceof AbstractSkeleton;
            
            if (isTargetMob) {
                LivingEntity livingEntity = (LivingEntity) entity;
                EntityEquipment equipment = livingEntity.getEquipment();
                
                if (equipment != null) {
                    ItemStack mainHand = equipment.getItemInMainHand();
                    if (mainHand != null && mainHand.getType() == Material.BOW) {
                        // Level between 5 and 20
                        int powerLevel = ThreadLocalRandom.current().nextInt(5, 21);
                        
                        ItemMeta meta = mainHand.getItemMeta();
                        if (meta != null) {
                            meta.addEnchant(Enchantment.POWER, powerLevel, true);
                            mainHand.setItemMeta(meta);
                        }
                    }
                }
            }
        }, 3L);
    }
}

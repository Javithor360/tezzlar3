package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.util.MobGearUtils;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class WitherSkeletonEliteGearMechanic extends DifficultyMechanic {

    private final Random random = new Random();

    public WitherSkeletonEliteGearMechanic(JavaPlugin plugin) {
        super(plugin, 14);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWitherSkeletonSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        LivingEntity entity = event.getEntity();
        if (entity.getType() != EntityType.WITHER_SKELETON) return;
        if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
        
        // Delay by 2 ticks to ensure it overrides RandomMobGearMechanic (which delays by 1 tick)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!entity.isValid() || entity.isDead()) return;
            if (EntityUtils.isCustomMob(entity)) return;
            
            EntityEquipment eq = entity.getEquipment();
            if (eq == null) return;
            
            Material newMat;
            int r = random.nextInt(5);
            if (r == 0) newMat = Material.DIAMOND_SWORD;
            else if (r == 1) newMat = Material.NETHERITE_SWORD;
            else if (r == 2) newMat = Material.DIAMOND_AXE;
            else if (r == 3) newMat = Material.NETHERITE_AXE;
            else newMat = Material.BOW;
            
            ItemStack newWeapon = new ItemStack(newMat);
            
            MobGearUtils.GearTier tier = MobGearUtils.getTier(TimeManager.getCurrentDay());
            newWeapon = MobGearUtils.applyRandomEnchantments(newWeapon, tier);
            
            eq.setItemInMainHand(newWeapon);
            eq.setItemInMainHandDropChance(0.0f);
        }, 2L);
    }
}

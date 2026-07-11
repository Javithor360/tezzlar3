package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.util.ItemUtils;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import org.bukkit.Location;

import java.util.Random;

public class InfraredSkeletonMechanic extends DifficultyMechanic {

    private final NamespacedKey INFRARED_KEY;
    private final Random random = new Random();

    public InfraredSkeletonMechanic(JavaPlugin plugin) {
        super(plugin, 4);
        INFRARED_KEY = new NamespacedKey(plugin, "is_infrared");
        CustomMobManager.register(CustomMobType.INFRARED_SKELETON, this::spawnManual);
    }

    public void spawnManual(Location loc) {
        Skeleton skeleton = (Skeleton) EntityUtils.spawnNatural(loc, EntityType.SKELETON);
        transform(skeleton);
    }

    private void transform(Skeleton skeleton) {
        skeleton.getPersistentDataContainer().set(INFRARED_KEY, PersistentDataType.BYTE, (byte) 1);
        EntityUtils.setCustomName(skeleton, "&cEsqueleto Infrarrojo");
        
        // Armor
        ItemStack head = new ItemStack(Material.RED_STAINED_GLASS);
        ItemStack chestplate = ItemUtils.createColoredLeather(Material.LEATHER_CHESTPLATE, Color.NAVY);
        ItemStack leggings = ItemUtils.createColoredLeather(Material.LEATHER_LEGGINGS, Color.NAVY);
        ItemStack boots = ItemUtils.createColoredLeather(Material.LEATHER_BOOTS, Color.NAVY);
        
        EntityUtils.equipArmor(skeleton, head, chestplate, leggings, boots, 0.0f);
        
        // Weapon
        ItemStack bow = new ItemStack(Material.BOW);
        ItemUtils.enchantItem(bow, Enchantment.FLAME, 1);
        
        int powerLevel = TimeManager.getCurrentDay() >= 12 ? 30 : 3;
        ItemUtils.enchantItem(bow, Enchantment.POWER, powerLevel);
        
        if (skeleton.getEquipment() != null) {
            skeleton.getEquipment().setItemInMainHand(bow);
            skeleton.getEquipment().setItemInMainHandDropChance(0.0f);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSkeletonSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Skeleton skeleton) {
            if (EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) {
                // 10% chance to become an infrared skeleton
                if (random.nextDouble() < 0.10) {
                    transform(skeleton);
                }
            }
        }
    }
}

package com.panita.tezzlar3.core.util;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobGearUtils {

    private static final Random random = new Random();

    // Valid Entities List
    private static final List<EntityType> VALID_MOBS = List.of(
            EntityType.PIGLIN, EntityType.ZOMBIFIED_PIGLIN, EntityType.DROWNED, 
            EntityType.BOGGED, EntityType.HUSK, EntityType.PARCHED,
            EntityType.PIGLIN_BRUTE, EntityType.SKELETON, EntityType.STRAY, 
            EntityType.WITHER_SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER
    );

    // Armor Materials
    private static final Material[] HELMETS = {Material.LEATHER_HELMET, Material.COPPER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET};
    private static final Material[] CHESTPLATES = {Material.LEATHER_CHESTPLATE, Material.COPPER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE};
    private static final Material[] LEGGINGS = {Material.LEATHER_LEGGINGS, Material.COPPER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS};
    private static final Material[] BOOTS = {Material.LEATHER_BOOTS, Material.COPPER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS};

    // Weapon Materials
    private static final Material[] MELEE_WEAPONS = {
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.COPPER_SWORD,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.GOLDEN_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.COPPER_AXE,
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.GOLDEN_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.COPPER_PICKAXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.GOLDEN_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.COPPER_SHOVEL,
            Material.WOODEN_SPEAR, Material.STONE_SPEAR, Material.GOLDEN_SPEAR, Material.IRON_SPEAR, Material.DIAMOND_SPEAR, Material.COPPER_SPEAR,
            Material.FISHING_ROD, Material.TRIDENT, Material.MACE
    };

    private static final Material[] RANGED_WEAPONS = {Material.BOW};

    // Offhand Materials
    private static final Material[] OFFHAND_ITEMS = {
            Material.SHIELD, Material.TOTEM_OF_UNDYING, Material.GOAT_HORN, Material.ENDER_PEARL, Material.SPYGLASS, Material.COMPASS, 
            Material.WIND_CHARGE, Material.EGG, Material.BROWN_EGG, Material.BLUE_EGG, Material.MAP, Material.FILLED_MAP, Material.BRUSH,
            Material.FLINT_AND_STEEL, Material.SNOWBALL, Material.BUNDLE
    };

    public static boolean isValidMobTarget(LivingEntity entity) {
        return VALID_MOBS.contains(entity.getType());
    }

    public static void equipRandomGear(LivingEntity entity) {
        EntityEquipment eq = entity.getEquipment();
        if (eq == null) return;

        // Armor Assignment
        if (random.nextDouble() < 0.60) eq.setHelmet(applyRandomEnchantments(getRandomArmor(HELMETS)));
        if (random.nextDouble() < 0.50) eq.setChestplate(applyRandomEnchantments(getRandomArmor(CHESTPLATES)));
        if (random.nextDouble() < 0.50) eq.setLeggings(applyRandomEnchantments(getRandomArmor(LEGGINGS)));
        if (random.nextDouble() < 0.60) eq.setBoots(applyRandomEnchantments(getRandomArmor(BOOTS)));

        // Weapons Assignment
        EntityType type = entity.getType();
        
        boolean isSkeleton = type == EntityType.SKELETON || type == EntityType.WITHER_SKELETON || type == EntityType.STRAY || type == EntityType.BOGGED || type == EntityType.PARCHED;
        
        if (random.nextDouble() < 0.70) {
            ItemStack weapon = null;
            
            // Attempt to assign ranged weapon
            if (random.nextBoolean()) {
                if (isSkeleton) {
                    weapon = new ItemStack(Material.BOW);
                } else if (type == EntityType.PIGLIN) {
                    weapon = new ItemStack(Material.CROSSBOW);
                }
            }
            
            // If ranged wasn't chosen or allowed, assign melee weapon
            if (weapon == null) {
                Material meleeMat = getWeightedMaterial(MELEE_WEAPONS);
                
                // Skeletons cannot use SPEARS
                while (isSkeleton && meleeMat.name().contains("SPEAR")) {
                    meleeMat = getWeightedMaterial(MELEE_WEAPONS);
                }
                
                weapon = new ItemStack(meleeMat);
            }
            
            eq.setItemInMainHand(applyRandomEnchantments(weapon));
        }

        // Offhand Assignment
        if (random.nextDouble() < 0.15) {
            eq.setItemInOffHand(getRandomItem(OFFHAND_ITEMS));
        }

        // Drop Chances
        eq.setHelmetDropChance(0.01f);
        eq.setChestplateDropChance(0.01f);
        eq.setLeggingsDropChance(0.01f);
        eq.setBootsDropChance(0.01f);
        eq.setItemInMainHandDropChance(0.0f);
        eq.setItemInOffHandDropChance(0.15f);
    }

    private static ItemStack getRandomArmor(Material[] options) {
        Material mat = getWeightedMaterial(options);
        if (mat.name().contains("LEATHER_")) {
            return ItemUtils.createColoredLeather(mat, Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
        }
        return new ItemStack(mat);
    }

    private static ItemStack getRandomItem(Material[] options) {
        return new ItemStack(getWeightedMaterial(options));
    }

    public static ItemStack applyRandomEnchantments(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return item;
        
        if (random.nextDouble() > 0.30) {
            return item; // 70% chance to NOT have enchantments
        }
        
        int enchantsToApply = random.nextBoolean() ? 1 : 2; // 1 or 2 enchants
        List<Enchantment> pool = new ArrayList<>();
        String name = item.getType().name();
        
        if (name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS") || name.contains("BOOTS")) {
            pool.add(Enchantment.PROTECTION);
            pool.add(Enchantment.FIRE_PROTECTION);
            pool.add(Enchantment.PROJECTILE_PROTECTION);
            pool.add(Enchantment.BLAST_PROTECTION);
            pool.add(Enchantment.THORNS);
            
            if (name.contains("HELMET")) {
                pool.add(Enchantment.RESPIRATION);
                pool.add(Enchantment.AQUA_AFFINITY);
            } else if (name.contains("LEGGINGS")) {
                pool.add(Enchantment.SWIFT_SNEAK);
            } else if (name.contains("BOOTS")) {
                pool.add(Enchantment.FEATHER_FALLING);
                pool.add(Enchantment.DEPTH_STRIDER);
                pool.add(Enchantment.SOUL_SPEED);
            }
        } else if (name.contains("SWORD") || name.contains("AXE") || name.contains("PICKAXE") || name.contains("SHOVEL") || name.contains("SPEAR")) {
            pool.add(Enchantment.SHARPNESS);
            pool.add(Enchantment.SMITE);
            pool.add(Enchantment.KNOCKBACK);
            pool.add(Enchantment.FIRE_ASPECT);
        } else if (name.contains("BOW") || name.contains("CROSSBOW")) {
            pool.add(Enchantment.POWER);
            if (name.contains("CROSSBOW")) {
                pool.add(Enchantment.PIERCING);
                pool.add(Enchantment.QUICK_CHARGE);
            } else {
                pool.add(Enchantment.PUNCH);
                pool.add(Enchantment.FLAME);
            }
        }
        
        if (pool.isEmpty()) return item;
        
        for (int i = 0; i < enchantsToApply; i++) {
            Enchantment selected = pool.get(random.nextInt(pool.size()));
            int maxLevel = selected.getMaxLevel();
            int randomLevel = random.nextInt(maxLevel + 1) + 1; // [1, maxLevel + 1] (Allows exceeding max level by 1)
            item.addUnsafeEnchantment(selected, randomLevel);
        }
        
        return item;
    }

    /**
     * Re-rolls if the picked material is overpowered to make them statistically rare.
     */
    private static Material getWeightedMaterial(Material[] options) {
        for (int i = 0; i < 4; i++) {
            Material mat = options[random.nextInt(options.length)];
            boolean isOP = mat.name().contains("DIAMOND_") || mat.name().equals("TOTEM_OF_UNDYING") || 
                           mat.name().equals("TRIDENT") || mat.name().equals("MACE");
            
            if (isOP) {
                if (random.nextDouble() < 0.10) return mat; // 10% chance to keep the OP item if rolled
            } else {
                return mat; // Accept normal items immediately
            }
        }
        // Fallback to the weakest material if everything failed
        return options[0];
    }
}

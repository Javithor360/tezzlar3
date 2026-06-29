package com.panita.tezzlar3.core.util;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import com.panita.tezzlar3.timeline.util.TimeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobGearUtils {

    private static final Random random = new Random();

    public enum GearTier {
        VANILLA, // Day 1-4
        BASIC,   // Day 5-10
        ELITE,   // Day 11-21
        MASTER   // Day 22+
    }

    public static GearTier getTier(int day) {
        if (day >= 22) return GearTier.MASTER;
        if (day >= 11) return GearTier.ELITE;
        if (day >= 5) return GearTier.BASIC;
        return GearTier.VANILLA;
    }

    // Valid Entities List
    private static final List<EntityType> VALID_MOBS = List.of(
            EntityType.PIGLIN, EntityType.ZOMBIFIED_PIGLIN, EntityType.DROWNED, 
            EntityType.BOGGED, EntityType.HUSK, EntityType.PARCHED,
            EntityType.PIGLIN_BRUTE, EntityType.SKELETON, EntityType.STRAY, 
            EntityType.WITHER_SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER
    );

    // Normal Armor Materials (Basic)
    private static final Material[] HELMETS = {Material.LEATHER_HELMET, Material.COPPER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET};
    private static final Material[] CHESTPLATES = {Material.LEATHER_CHESTPLATE, Material.COPPER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE};
    private static final Material[] LEGGINGS = {Material.LEATHER_LEGGINGS, Material.COPPER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS};
    private static final Material[] BOOTS = {Material.LEATHER_BOOTS, Material.COPPER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS};

    // Elite & Master Armor Materials (Day 11+)
    private static final Material[] ELITE_HELMETS = {Material.COPPER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET};
    private static final Material[] ELITE_CHESTPLATES = {Material.COPPER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE};
    private static final Material[] ELITE_LEGGINGS = {Material.COPPER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS};
    private static final Material[] ELITE_BOOTS = {Material.COPPER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS};

    // Weapon Materials
    private static final Material[] MELEE_WEAPONS = {
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.COPPER_SWORD,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.GOLDEN_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.COPPER_AXE,
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.GOLDEN_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.COPPER_PICKAXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.GOLDEN_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.COPPER_SHOVEL,
            Material.WOODEN_SPEAR, Material.STONE_SPEAR, Material.GOLDEN_SPEAR, Material.IRON_SPEAR, Material.DIAMOND_SPEAR, Material.COPPER_SPEAR,
            Material.FISHING_ROD, Material.TRIDENT, Material.MACE
    };

    // Elite & Master Weapon Materials (Day 11+)
    private static final Material[] ELITE_MELEE_WEAPONS = {
            Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.COPPER_SWORD, Material.NETHERITE_SWORD,
            Material.IRON_AXE, Material.DIAMOND_AXE, Material.COPPER_AXE, Material.NETHERITE_AXE,
            Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.COPPER_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.COPPER_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.IRON_SPEAR, Material.DIAMOND_SPEAR, Material.COPPER_SPEAR, Material.NETHERITE_SPEAR,
            Material.TRIDENT, Material.MACE
    };

    // Offhand Materials
    private static final Material[] OFFHAND_ITEMS = {
            Material.SHIELD, Material.TOTEM_OF_UNDYING, Material.GOAT_HORN, Material.ENDER_PEARL, Material.SPYGLASS, Material.COMPASS, 
            Material.WIND_CHARGE, Material.EGG, Material.BROWN_EGG, Material.BLUE_EGG, Material.MAP, Material.FILLED_MAP, Material.BRUSH,
            Material.FLINT_AND_STEEL, Material.SNOWBALL, Material.BUNDLE, Material.TORCH, Material.LANTERN, Material.SOUL_TORCH, Material.SOUL_LANTERN,
            Material.COPPER_TORCH, Material.OXIDIZED_COPPER_LANTERN, Material.LEAD
    };

    public static boolean isValidMobTarget(LivingEntity entity) {
        return VALID_MOBS.contains(entity.getType());
    }

    public static void equipRandomGear(LivingEntity entity) {
        EntityEquipment eq = entity.getEquipment();
        if (eq == null) return;

        GearTier tier = getTier(TimeManager.getCurrentDay());
        if (tier == GearTier.VANILLA) return;
        
        // Armor Assignment (higher chances depending on tier)
        double helmetChance = tier == GearTier.MASTER ? 0.95 : (tier == GearTier.ELITE ? 0.70 : 0.40);
        double chestChance = tier == GearTier.MASTER ? 0.95 : (tier == GearTier.ELITE ? 0.65 : 0.30);
        double legsChance = tier == GearTier.MASTER ? 0.95 : (tier == GearTier.ELITE ? 0.65 : 0.30);
        double bootsChance = tier == GearTier.MASTER ? 0.95 : (tier == GearTier.ELITE ? 0.70 : 0.40);

        Material[] hPool = tier == GearTier.BASIC ? HELMETS : ELITE_HELMETS;
        Material[] cPool = tier == GearTier.BASIC ? CHESTPLATES : ELITE_CHESTPLATES;
        Material[] lPool = tier == GearTier.BASIC ? LEGGINGS : ELITE_LEGGINGS;
        Material[] bPool = tier == GearTier.BASIC ? BOOTS : ELITE_BOOTS;

        if (random.nextDouble() < helmetChance) eq.setHelmet(applyRandomEnchantments(getRandomArmor(hPool, tier), tier));
        if (random.nextDouble() < chestChance) eq.setChestplate(applyRandomEnchantments(getRandomArmor(cPool, tier), tier));
        if (random.nextDouble() < legsChance) eq.setLeggings(applyRandomEnchantments(getRandomArmor(lPool, tier), tier));
        if (random.nextDouble() < bootsChance) eq.setBoots(applyRandomEnchantments(getRandomArmor(bPool, tier), tier));

        // Weapons Assignment
        EntityType type = entity.getType();
        boolean isSkeleton = type == EntityType.SKELETON || type == EntityType.WITHER_SKELETON || type == EntityType.STRAY || type == EntityType.BOGGED || type == EntityType.PARCHED;
        
        double weaponChance = tier == GearTier.MASTER ? 1.0 : (tier == GearTier.ELITE ? 0.75 : 0.50);
        if (random.nextDouble() < weaponChance) {
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
                Material[] pool = tier == GearTier.BASIC ? MELEE_WEAPONS : ELITE_MELEE_WEAPONS;
                Material meleeMat = getWeightedMaterial(pool, tier);
                
                // Skeletons cannot use SPEARS
                while (isSkeleton && meleeMat.name().contains("SPEAR")) {
                    meleeMat = getWeightedMaterial(pool, tier);
                }
                
                weapon = new ItemStack(meleeMat);
            }
            
            eq.setItemInMainHand(applyRandomEnchantments(weapon, tier));
        }

        // Offhand Assignment
        double offhandChance = tier == GearTier.MASTER ? 0.50 : (tier == GearTier.ELITE ? 0.30 : 0.15);
        if (random.nextDouble() < offhandChance) {
            ItemStack offhand = getRandomItem(OFFHAND_ITEMS, tier);
            if (offhand.getType() == Material.SHIELD) {
                offhand = applyRandomEnchantments(offhand, tier);
            }
            eq.setItemInOffHand(offhand);
        }

        // Drop Chances (hardcoded low so players don't farm easily)
        eq.setHelmetDropChance(0.01f);
        eq.setChestplateDropChance(0.01f);
        eq.setLeggingsDropChance(0.01f);
        eq.setBootsDropChance(0.01f);
        eq.setItemInMainHandDropChance(0.0f);
        eq.setItemInOffHandDropChance(0.15f);
    }

    private static ItemStack getRandomArmor(Material[] options, GearTier tier) {
        Material mat = getWeightedMaterial(options, tier);
        if (mat.name().contains("LEATHER_")) {
            return ItemUtils.createColoredLeather(mat, Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
        }
        return new ItemStack(mat);
    }

    private static ItemStack getRandomItem(Material[] options, GearTier tier) {
        return new ItemStack(getWeightedMaterial(options, tier));
    }

    public static ItemStack applyRandomEnchantments(ItemStack item, GearTier tier) {
        if (item == null || item.getType() == Material.AIR) return item;
        
        // Base probability of having NO enchantments
        double noneChance = switch (tier) {
            case MASTER -> 0.10; // 10% chance of no enchants
            case ELITE -> 0.50;  // 50% chance of no enchants
            default -> 0.70;     // 70% chance of no enchants
        };

        if (random.nextDouble() < noneChance) {
            return item; 
        }
        
        int enchantsToApply = switch (tier) {
            case MASTER -> random.nextInt(2) + 3; // 3 to 4 enchants
            case ELITE -> random.nextInt(3) + 1;  // 1 to 3 enchants
            default -> random.nextBoolean() ? 1 : 2; // 1 to 2 enchants
        };

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
        } else if (name.contains("SHIELD")) {
            pool.add(Enchantment.UNBREAKING);
        }
        
        if (pool.isEmpty()) return item;
        
        for (int i = 0; i < enchantsToApply; i++) {
            Enchantment selected = pool.get(random.nextInt(pool.size()));
            int maxLevel = selected.getMaxLevel();
            int randomLevel;
            
            switch (tier) {
                case MASTER:
                    randomLevel = maxLevel; // Always max level
                    break;
                case ELITE:
                    randomLevel = maxLevel == 1 ? 1 : Math.max(2, random.nextInt(maxLevel + 1) + 1); // Min level 2
                    break;
                default:
                    randomLevel = random.nextInt(maxLevel + 1) + 1; // [1, maxLevel + 1]
                    break;
            }
            
            item.addUnsafeEnchantment(selected, randomLevel);
        }
        
        return item;
    }

    /**
     * Re-rolls if the picked material is overpowered to make them statistically rare.
     */
    private static Material getWeightedMaterial(Material[] options, GearTier tier) {
        for (int i = 0; i < 4; i++) {
            Material mat = options[random.nextInt(options.length)];
            
            if (mat.name().contains("NETHERITE_")) {
                double chance = tier == GearTier.MASTER ? 0.35 : 0.10;
                if (random.nextDouble() < chance) return mat;
                continue;
            }
            
            boolean isOP = mat.name().contains("DIAMOND_") || mat.name().equals("TOTEM_OF_UNDYING") || 
                           mat.name().equals("TRIDENT") || mat.name().equals("MACE");
            
            if (isOP) {
                double chance = tier == GearTier.MASTER ? 0.80 : (tier == GearTier.ELITE ? 0.25 : 0.10);
                if (random.nextDouble() < chance) return mat;
            } else {
                return mat; // Normal items accepted immediately
            }
        }
        // Fallback to the weakest material if everything failed
        return options[0];
    }
}

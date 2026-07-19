package com.panita.tezzlar3.difficulty.util;

import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BossRewardUtils {

    private static final Random RANDOM = new Random();

    public static void giveOrDropItem(Player p, ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        
        Map<Integer, ItemStack> leftover = p.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                p.getWorld().dropItem(p.getLocation(), drop);
            }
        }
    }

    public static void giveOrDropItems(Player p, Material material, int amount) {
        int maxStack = Math.min(64, Math.max(1, material.getMaxStackSize()));
        while (amount > 0) {
            int currentAmount = Math.min(amount, maxStack);
            giveOrDropItem(p, new ItemStack(material, currentAmount));
            amount -= currentAmount;
        }
    }

    public static void giveOrDropCustomStack(Player p, ItemStack baseItem, int amount, int maxStack) {
        while (amount > 0) {
            int currentAmount = Math.min(amount, maxStack);
            ItemStack item = baseItem.clone();
            item.setAmount(currentAmount);
            giveOrDropItem(p, item);
            amount -= currentAmount;
        }
    }

    public static void executeSharedBossRewards(Player p) {
        List<Runnable> options = new ArrayList<>();
        
        options.add(() -> giveOrDropItems(p, Material.GOLDEN_APPLE, 1 + RANDOM.nextInt(31)));
        options.add(() -> giveOrDropItems(p, Material.ENCHANTED_GOLDEN_APPLE, 4));
        options.add(() -> p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0)));
        options.add(() -> p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 12 * 60 * 60 * 20, 0)));
        options.add(() -> p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1)));
        options.add(() -> p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 4 * 60 * 60 * 20, 2)));
        options.add(() -> p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 8 * 60 * 60 * 20, 2)));
        options.add(() -> giveOrDropItems(p, Material.TOTEM_OF_UNDYING, 1 + RANDOM.nextInt(6)));
        options.add(() -> giveOrDropItems(p, Material.DIAMOND, 25 + RANDOM.nextInt(50)));
        options.add(() -> giveOrDropItems(p, Material.BAKED_POTATO, 128 + RANDOM.nextInt(385)));
        options.add(() -> giveOrDropCustomStack(p, new ItemStack(Material.MUSHROOM_STEW), 128 + RANDOM.nextInt(385), 67));
        options.add(() -> giveOrDropCustomStack(p, new ItemStack(Material.BEETROOT_SOUP), 128 + RANDOM.nextInt(385), 67));
        options.add(() -> giveOrDropCustomStack(p, new ItemStack(Material.RABBIT_STEW), 128 + RANDOM.nextInt(385), 67));
        options.add(() -> giveOrDropCustomStack(p, new ItemStack(Material.PUMPKIN_PIE), 128 + RANDOM.nextInt(385), 67));
        options.add(() -> {
            ItemStack item = CustomItemManager.getItem("manzanium_apple");
            if (item != null) giveOrDropCustomStack(p, item, 128 + RANDOM.nextInt(385), 67);
        });
        options.add(() -> {
            ItemStack item = CustomItemManager.getItem("copper_carrot");
            if (item != null) giveOrDropCustomStack(p, item, 128 + RANDOM.nextInt(385), 64);
        });
        options.add(() -> giveOrDropItems(p, Material.RAW_GOLD, 48 + RANDOM.nextInt(151)));
        options.add(() -> {
            Material[] shulkers = {Material.SHULKER_BOX, Material.RED_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BLACK_SHULKER_BOX};
            giveOrDropItems(p, shulkers[RANDOM.nextInt(shulkers.length)], 1);
        });
        options.add(() -> {
            String[] customItems = {"tactic_bow", "tezzlar_heart", "bee_totem", "chicken_totem", "ghast_totem", "golden_totem", "life_save", "memory_evoker", "sniffer_totem", "sulfur_totem", "turtle_totem", "mega_spear", "memory_evoker", "amethyst_horn", "fancy_umbrella", "soulbound_relic"};
            String chosen = customItems[RANDOM.nextInt(customItems.length)];
            ItemStack item = CustomItemManager.getItem(chosen);
            if (item != null) giveOrDropItem(p, item);
        });
        options.add(() -> giveOrDropItems(p, Material.EMERALD, 128 + RANDOM.nextInt(385)));
        
        Collections.shuffle(options);
        
        // Execute 3 choices
        options.get(0).run();
        options.get(1).run();
        options.get(2).run();
    }
}

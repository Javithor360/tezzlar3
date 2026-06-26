package com.panita.tezzlar3.core.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Color;

import java.util.Arrays;
import java.util.Random;

public class ItemUtils {
    public static boolean checkItemModel(ItemStack item, NamespacedKey modelKey) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = meta.getItemModel();
        return key != null && key.equals(modelKey);
    }

    public static NamespacedKey getItemModel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        return meta.getItemModel();
    }

    /**
     * Gets a custom String ID stored in the item's PersistentDataContainer.
     * Useful for identifying custom items from other plugins or configurations.
     */
    public static String getCustomItemId(ItemStack item, NamespacedKey key) {
        if (item == null || !item.hasItemMeta()) return null;
        
        return item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    public static int countMaterial(Player player, Material mat) {
        return Arrays.stream(player.getInventory().getContents())
                .filter(i -> i != null && i.getType() == mat)
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    public static void removeMaterial(Player player, Material mat, int amount) {
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getType() != mat) continue;
            int take = Math.min(amount, stack.getAmount());
            stack.setAmount(stack.getAmount() - take);
            amount -= take;
            if (amount <= 0) break;
        }
    }

    /**
     * Calculates extra drops based on the looting level of the player's main hand item.
     * Mimics Vanilla logic: random number from 0 to LootingLevel.
     */
    public static int getLootingBonus(Player player) {
        if (player == null) return 0;
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!hand.hasItemMeta()) return 0;
        
        int lootingLevel = hand.getEnchantmentLevel(Enchantment.LOOTING);
        if (lootingLevel > 0) {
            return new Random().nextInt(lootingLevel + 1);
        }
        return 0;
    }

    /**
     * Quickly applies an unsafe enchantment to an item and returns it for chaining.
     */
    public static ItemStack enchantItem(ItemStack item, Enchantment enchantment, int level) {
        if (item != null && item.getType() != Material.AIR) {
            item.addUnsafeEnchantment(enchantment, level);
        }
        return item;
    }

    /**
     * Creates a colored leather armor piece.
     */
    public static ItemStack createColoredLeather(Material material, Color color) {
        ItemStack item = new ItemStack(material);
        if (item.getItemMeta() instanceof LeatherArmorMeta meta) {
            meta.setColor(color);
            item.setItemMeta(meta);
        }
        return item;
    }
}


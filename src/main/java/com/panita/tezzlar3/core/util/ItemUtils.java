package com.panita.tezzlar3.core.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

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
}


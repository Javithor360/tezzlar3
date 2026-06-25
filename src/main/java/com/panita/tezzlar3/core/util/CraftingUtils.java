package com.panita.tezzlar3.core.util;

import org.bukkit.Material;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftingUtils {
    /**
     * Get the total amount of items crafted in a CraftItemEvent, accounting for shift-click crafting.
     * @param event The CraftItemEvent to analyze.
     * @return The total number of items crafted (assuming enough inventory space).
     */
    public static int getCraftedAmount(CraftItemEvent event) {
        if (event.getRecipe() == null || event.getRecipe().getResult() == null) return 0;
        int resultAmount = event.getRecipe().getResult().getAmount();

        // Normal Click
        if (!event.isShiftClick()) {
            return resultAmount;
        }

        // Shift Click
        int minStack = Integer.MAX_VALUE;
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item != null && item.getType() != Material.AIR) {
                minStack = Math.min(minStack, item.getAmount());
            }
        }
        
        if (minStack == Integer.MAX_VALUE) {
            return 0;
        }

        return minStack * resultAmount;
    }
}

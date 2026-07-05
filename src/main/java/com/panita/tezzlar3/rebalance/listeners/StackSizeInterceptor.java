package com.panita.tezzlar3.rebalance.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.timeline.util.TimeManager;

public class StackSizeInterceptor implements Listener {

    private final JavaPlugin plugin;

    public StackSizeInterceptor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private void schedulePlayerSweep(Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            boolean modified = sweepInventory(player.getInventory());
            ItemStack cursor = player.getItemOnCursor();
            if (applyCustomStackSize(cursor)) {
                player.setItemOnCursor(cursor);
                modified = true;
            }
            if (modified) {
                player.updateInventory();
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            schedulePlayerSweep(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        schedulePlayerSweep(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            schedulePlayerSweep(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            schedulePlayerSweep(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreativeClick(InventoryCreativeEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            schedulePlayerSweep(player);
        }
    }

    /**
     * Sweeps the inventory and updates items
     * @return true if at least one item was modified
     */
    public boolean sweepInventory(Inventory inv) {
        boolean modified = false;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                if (applyCustomStackSize(item)) {
                    inv.setItem(i, item);
                    modified = true;
                }
            }
        }
        return modified;
    }

    /**
     * Applies the custom stack size to the item's meta if applicable.
     * @return true if the item was modified
     */
    private boolean applyCustomStackSize(ItemStack item) {
        if (TimeManager.getCurrentDay() < 2) return false;
        
        if (item == null || item.getType() == Material.AIR) return false;

        int newStackSize = getCustomMaxStackSize(item.getType());
        if (newStackSize != -1) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // Check if the component is missing or different
                if (!meta.hasMaxStackSize() || meta.getMaxStackSize() != newStackSize) {
                    meta.setMaxStackSize(newStackSize);
                    item.setItemMeta(meta);
                    return true;
                }
            }
        }
        return false;
    }

    private int getCustomMaxStackSize(Material material) {
        if (material == Material.ENDER_PEARL) {
            return 4;
        }
        if (material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION) {
            return 6;
        }
        if (material == Material.MUSHROOM_STEW || material == Material.RABBIT_STEW || material == Material.SUSPICIOUS_STEW || 
            material == Material.BEETROOT_SOUP || material == Material.CAKE || material == Material.HONEY_BOTTLE || 
            material == Material.BREAD || material == Material.APPLE || material == Material.GOLDEN_APPLE || 
            material == Material.ENCHANTED_GOLDEN_APPLE || material == Material.PUMPKIN_PIE) {
            return 67;
        }
        if (material == Material.EGG || material == Material.SNOWBALL) {
            return 99;
        }
        // Blocks that originally stack to 64 are changed to 99
        if (material.isBlock() && material.getMaxStackSize() == 64) {
            return 99;
        }
        return -1;
    }
}

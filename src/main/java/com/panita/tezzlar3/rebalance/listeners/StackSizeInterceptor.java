package com.panita.tezzlar3.rebalance.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.*;
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

    private void requestUpdate(Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin, player::updateInventory, 1L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        if (applyCustomStackSize(item)) {
            event.getEntity().setItemStack(item);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickup(EntityPickupItemEvent event) {
        boolean modified = false;
        if (event.getEntity() instanceof Player player) {
            modified = sweepInventory(player.getInventory());
        }
        
        ItemStack item = event.getItem().getItemStack();
        if (applyCustomStackSize(item)) {
            event.getItem().setItemStack(item);
            modified = true;
        }

        if (modified && event.getEntity() instanceof Player player) {
            requestUpdate(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        boolean topMod = false;
        // Do not sweep processing block inventories to prevent breaking vanilla mechanics (Furnaces, Brewers, etc.)
        org.bukkit.event.inventory.InventoryType topType = event.getInventory().getType();
        if (topType != org.bukkit.event.inventory.InventoryType.FURNACE && 
            topType != org.bukkit.event.inventory.InventoryType.BLAST_FURNACE && 
            topType != org.bukkit.event.inventory.InventoryType.SMOKER &&
            topType != org.bukkit.event.inventory.InventoryType.BREWING) {
            topMod = sweepInventory(event.getInventory());
        }
        
        boolean botMod = sweepInventory(event.getPlayer().getInventory());
        if ((topMod || botMod) && event.getPlayer() instanceof Player player) {
            requestUpdate(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (sweepInventory(event.getPlayer().getInventory())) {
            requestUpdate(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraft(CraftItemEvent event) {
        boolean modified = false;
        ItemStack item = event.getCurrentItem();
        if (item != null && applyCustomStackSize(item)) {
            event.setCurrentItem(item);
            modified = true;
        }
        if (modified && event.getWhoClicked() instanceof Player player) {
            requestUpdate(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClick() == org.bukkit.event.inventory.ClickType.MIDDLE) {
            if (event.getWhoClicked() instanceof Player player) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    boolean mod = sweepInventory(player.getInventory());
                    ItemStack cur = player.getItemOnCursor();
                    if (applyCustomStackSize(cur)) {
                        player.setItemOnCursor(cur);
                        mod = true;
                    }
                    if (mod) {
                        player.updateInventory();
                    }
                }, 1L);
            }
            return;
        }

        boolean modified = false;
        
        // Skip modifying items inside processing blocks to prevent breaking them
        boolean isProcessingBlock = false;
        if (event.getClickedInventory() != null) {
            org.bukkit.event.inventory.InventoryType type = event.getClickedInventory().getType();
            isProcessingBlock = (type == org.bukkit.event.inventory.InventoryType.FURNACE || 
                                 type == org.bukkit.event.inventory.InventoryType.BLAST_FURNACE || 
                                 type == org.bukkit.event.inventory.InventoryType.SMOKER ||
                                 type == org.bukkit.event.inventory.InventoryType.BREWING);
        }
        
        if (!isProcessingBlock) {
            ItemStack current = event.getCurrentItem();
            if (current != null && applyCustomStackSize(current)) {
                event.setCurrentItem(current);
                modified = true;
            }
        }
        
        ItemStack cursor = event.getCursor();
        if (cursor != null && applyCustomStackSize(cursor)) {
            event.getView().setCursor(cursor);
            modified = true;
        }
        
        if (modified && event.getWhoClicked() instanceof Player player) {
            requestUpdate(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (result != null && applyCustomStackSize(result)) {
            event.getInventory().setResult(result);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreativeClick(InventoryCreativeEvent event) {
        // Fix for middle click copying blocks: delay the modification by 1 tick
        if (event.getWhoClicked() instanceof Player player) {
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

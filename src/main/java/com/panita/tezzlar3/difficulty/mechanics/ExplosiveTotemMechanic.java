package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ExplosiveTotemMechanic extends DifficultyMechanic {

    public ExplosiveTotemMechanic(JavaPlugin plugin) {
        super(plugin, 16);
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (!isActive()) return;

        ItemStack offhand = event.getOffHandItem();
        if (offhand.getType() == Material.TOTEM_OF_UNDYING) {
            if (handleTotemEquip(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!isActive()) return;
        
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        // Slot 40 is the offhand slot
        if (event.getSlot() == 40) {
            ItemStack cursor = event.getCursor();
            if (cursor.getType() == Material.TOTEM_OF_UNDYING) {
                if (handleTotemEquip(player)) {
                    event.setCancelled(true);
                }
            }
        }
        
        // If they shift-click a totem
        if (event.isShiftClick()) {
            ItemStack current = event.getCurrentItem();
            if (current.getType() == Material.TOTEM_OF_UNDYING) {
                ItemStack offhandItem = player.getInventory().getItemInOffHand();
                if (offhandItem.getType() == Material.AIR) {
                    if (handleTotemEquip(player)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private boolean handleTotemEquip(Player player) {
        if (Math.random() < 0.07) {
            player.getWorld().createExplosion(player.getLocation(), 4.0F, true, false);
            return true;
        }
        return false;
    }
}

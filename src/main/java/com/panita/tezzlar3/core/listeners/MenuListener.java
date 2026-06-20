package com.panita.tezzlar3.core.listeners;

import com.panita.tezzlar3.core.gui.Menu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        
        InventoryHolder holder = event.getInventory().getHolder();
        
        if (holder instanceof Menu) {
            // Cancel the event so items cannot be moved or stolen
            event.setCancelled(true);
            
            // Do not process clicks outside the actual menu (e.g. clicking the player's own inventory while a menu is open)
            if (event.getClickedInventory().equals(event.getInventory())) {
                Menu menu = (Menu) holder;
                menu.handleMenu(event);
            }
        }
    }
}

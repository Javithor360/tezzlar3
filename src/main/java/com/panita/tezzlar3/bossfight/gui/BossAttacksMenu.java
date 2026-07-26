package com.panita.tezzlar3.bossfight.gui;

import com.panita.tezzlar3.core.gui.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BossAttacksMenu extends Menu {

    public BossAttacksMenu(Player player) {
        super(player);
    }

    @Override
    public String getMenuName() {
        return "<red><bold>Ataques del Jefe";
    }

    @Override
    public int getSlots() {
        return 54; // 6 rows for all attacks
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getSlot();
        
        // TODO: Handle attack clicks
    }
}

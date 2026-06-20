package com.panita.tezzlar3.core.gui;

import com.panita.tezzlar3.core.chat.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class Menu implements InventoryHolder {

    protected Inventory inventory;
    protected Player player;
    
    // FILLER_GLASS is standard for borders
    protected final ItemStack FILLER_GLASS = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();

    public Menu(Player player) {
        this.player = player;
    }

    public abstract String getMenuName();

    public abstract int getSlots();

    public abstract void handleMenu(InventoryClickEvent e);

    public abstract void setMenuItems();

    public void open() {
        inventory = Bukkit.createInventory(this, getSlots(), Messenger.mini(getMenuName()));
        this.setMenuItems();
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setFillerGlass() {
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, FILLER_GLASS);
            }
        }
    }
}

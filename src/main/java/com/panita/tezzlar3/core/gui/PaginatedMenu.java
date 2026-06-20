package com.panita.tezzlar3.core.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public abstract class PaginatedMenu extends Menu {

    protected int page = 0;
    protected int maxItemsPerPage = 28;
    protected int index = 0;

    public PaginatedMenu(Player player) {
        super(player);
    }

    // Set maximum slots to 54 for all paginated menus
    @Override
    public int getSlots() {
        return 54;
    }

    public int getMaxItemsPerPage() {
        return maxItemsPerPage;
    }

    public void addMenuBorder() {
        // Bottom row buttons
        inventory.setItem(48, new ItemBuilder(Material.ARROW).name("<green>◀ Página Anterior").build());
        inventory.setItem(49, new ItemBuilder(Material.BARRIER).name("<red>Cerrar Menú").build());
        inventory.setItem(50, new ItemBuilder(Material.ARROW).name("<green>Página Siguiente ▶").build());

        // Top and Bottom borders
        for (int i = 0; i < 10; i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, FILLER_GLASS);
        }
        for (int i = 45; i < 54; i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, FILLER_GLASS);
        }
        
        // Left and Right borders
        for (int i = 9; i < 45; i += 9) {
            if (inventory.getItem(i) == null) inventory.setItem(i, FILLER_GLASS);
        }
        for (int i = 17; i < 45; i += 9) {
            if (inventory.getItem(i) == null) inventory.setItem(i, FILLER_GLASS);
        }
    }
}

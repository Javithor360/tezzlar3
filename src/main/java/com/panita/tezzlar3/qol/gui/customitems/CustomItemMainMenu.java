package com.panita.tezzlar3.qol.gui.customitems;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.core.gui.PaginatedMenu;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomItemMainMenu extends PaginatedMenu {

    private final List<String> itemIds;

    public CustomItemMainMenu(Player player) {
        super(player);
        this.itemIds = new ArrayList<>(CustomItemManager.getAllItemNames());
        Collections.sort(itemIds); // Alphabetical sort
    }

    @Override
    public String getMenuName() {
        return "<dark_purple><b>Ítems Personalizados</b></dark_purple>";
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        int[] innerSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        if (itemIds.isEmpty()) {
            ItemStack noItems = new ItemBuilder(Material.BARRIER)
                    .name("<red><b>No hay ítems guardados</b></red>")
                    .lore("<gray>Usa <yellow>/tz item save <nombre></yellow> con un", "<gray>ítem en tu mano principal para guardarlo.</gray>")
                    .build();
            inventory.setItem(22, noItems); // Center of the menu
            return;
        }

        for (int i = 0; i < getMaxItemsPerPage(); i++) {
            index = getMaxItemsPerPage() * page + i;
            if (index >= itemIds.size()) break;

            String id = itemIds.get(index);
            ItemStack item = CustomItemManager.getItem(id);

            if (item != null) {
                // We create a visual clone for the menu
                ItemStack displayItem = item.clone();
                ItemMeta meta = displayItem.getItemMeta();
                if (meta != null) {
                    List<Component> currentLore = meta.hasLore() ? meta.lore() : new ArrayList<>();
                    currentLore.add(Component.empty());
                    currentLore.add(Messenger.mini("<yellow>► Clic para obtener</yellow>"));
                    currentLore.add(Messenger.mini("<dark_gray>ID: " + id + "</dark_gray>"));
                    meta.lore(currentLore);
                    displayItem.setItemMeta(meta);
                }
                inventory.setItem(innerSlots[i], displayItem);
            }
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getSlot();

        if (slot == 48) { // Prev
            if (page > 0) {
                page--;
                super.open();
            } else {
                Messenger.prefixedSend(player, "&cYa estás en la primera página.");
            }
            return;
        }

        if (slot == 50) { // Next
            if (!((index + 1) >= itemIds.size())) {
                page++;
                super.open();
            } else {
                Messenger.prefixedSend(player, "&cEstás en la última página.");
            }
            return;
        }

        if (slot == 49) { // Close
            player.closeInventory();
            return;
        }

        int[] innerSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        if (!itemIds.isEmpty()) {
            for (int i = 0; i < innerSlots.length; i++) {
                if (slot == innerSlots[i]) {
                    int clickedIndex = getMaxItemsPerPage() * page + i;
                    if (clickedIndex < itemIds.size()) {
                        String clickedId = itemIds.get(clickedIndex);
                        ItemStack realItem = CustomItemManager.getItem(clickedId);
                        if (realItem != null) {
                            ItemStack giveItem = realItem.clone();
                            if (player.getInventory().firstEmpty() == -1) {
                                player.getWorld().dropItemNaturally(player.getLocation(), giveItem);
                                Messenger.prefixedSend(player, "<yellow>Inventario lleno. El ítem fue arrojado al suelo.</yellow>");
                            } else {
                                player.getInventory().addItem(giveItem);
                                Messenger.prefixedSend(player, "<green>Has recibido el ítem.</green>");
                            }
                        }
                    }
                    break;
                }
            }
        }
    }
}

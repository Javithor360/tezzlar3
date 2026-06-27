package com.panita.tezzlar3.difficulty.gui;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.core.gui.PaginatedMenu;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.List;

public class MobSpawnMenu extends PaginatedMenu {

    private final List<CustomMobType> mobs;

    public MobSpawnMenu(Player player) {
        super(player);
        this.mobs = Arrays.asList(CustomMobType.values());
    }

    @Override
    public String getMenuName() {
        return "<dark_red>Generador de Mobs</dark_red>";
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

        for (int i = 0; i < getMaxItemsPerPage(); i++) {
            index = getMaxItemsPerPage() * page + i;
            if (index >= mobs.size()) break;

            CustomMobType mob = mobs.get(index);
            
            double displayHealth = mob.getHealth();
            if (TimeManager.getCurrentDay() >= 11 && mob != CustomMobType.GIGA_MAGMA_CUBE) {
                displayHealth *= 2;
            }

            List<String> loreLines = new java.util.ArrayList<>();
            loreLines.add("<gray>Salud Base: <red>❤ " + displayHealth + "</red>");
            loreLines.add("");
            for (String line : mob.getDescription().split("<newline>")) {
                loreLines.add("<white>" + line);
            }
            loreLines.add("");
            loreLines.add("<yellow>► Clic para spawnear aquí");

            ItemBuilder icon = new ItemBuilder(mob.getIcon())
                    .name("<gold><bold>" + mob.getCustomName() + "</bold></gold>")
                    .lore(loreLines);

            inventory.setItem(innerSlots[i], icon.build());
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
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
            if (!((index + 1) >= mobs.size())) {
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

        for (int i = 0; i < innerSlots.length; i++) {
            if (slot == innerSlots[i]) {
                int clickedIndex = getMaxItemsPerPage() * page + i;
                if (clickedIndex < mobs.size()) {
                    CustomMobType clickedMob = mobs.get(clickedIndex);
                    player.closeInventory();
                    CustomMobManager.spawn(clickedMob, player.getLocation());
                    Messenger.prefixedSend(player, "&aHas invocado a: &e" + clickedMob.getCustomName() + "&a.");
                }
                break;
            }
        }
    }
}

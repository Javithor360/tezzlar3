package com.panita.tezzlar3.missions.encyclopedia.ui;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.missions.encyclopedia.data.EncyclopediaManager;
import com.panita.tezzlar3.missions.encyclopedia.data.EncyclopediaRecord;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class EncyclopediaAdminGUI extends EncyclopediaGUI {

    public EncyclopediaAdminGUI(Player player, EncyclopediaManager manager) {
        super(player, manager);
    }

    @Override
    public String getMenuName() {
        return "<dark_red>Admin: <dark_green>Enciclopedia <gray>(" + manager.getCompletedCount() + "/" + manager.getTotalCount() + ")";
    }

    @Override
    protected void modifyIcon(ItemBuilder icon, EntityType type, boolean completed) {
        icon.appendLore("");
        if (completed) {
            icon.appendLore("<red>► Click Izquierdo para INVALIDAR");
        } else {
            icon.appendLore("<green>► Click Derecho para FORZAR VALIDACIÓN");
        }
    }

    @Override
    protected void handleCustomClick(InventoryClickEvent e) {
        int slot = e.getSlot();
        
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
                    EntityType type = mobs.get(clickedIndex);
                    boolean completed = manager.isMobCompleted(type);
                    
                    if (completed && e.getClick() == ClickType.LEFT) {
                        EncyclopediaRecord record = manager.getRecord(type);
                        record.setStatus(EncyclopediaRecord.Status.REJECTED);
                        manager.saveData();
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                        Messenger.prefixedSend(player, "&aHas &cinvalidado&a el registro de &e" + type.name());
                        super.open();
                    } else if (!completed && e.getClick() == ClickType.RIGHT) {
                        // Force validation
                        EncyclopediaRecord forced = new EncyclopediaRecord(
                                type,
                                "FORCED_BY_ADMIN",
                                player.getName(),
                                System.currentTimeMillis(),
                                player.getWorld().getEnvironment().name(),
                                EncyclopediaRecord.Status.APPROVED
                        );
                        manager.addRecord(forced);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                        Messenger.prefixedSend(player, "&aHas &aforzado la validación&a de &e" + type.name());
                        super.open();
                    }
                }
                break;
            }
        }
    }
}

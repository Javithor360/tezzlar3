package com.panita.tezzlar3.missions.encyclopedia.ui;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.core.gui.PaginatedMenu;
import com.panita.tezzlar3.missions.encyclopedia.data.EncyclopediaManager;
import com.panita.tezzlar3.missions.encyclopedia.data.EncyclopediaRecord;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EncyclopediaGUI extends PaginatedMenu {
    protected final EncyclopediaManager manager;
    protected final List<EntityType> mobs;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public EncyclopediaGUI(Player player, EncyclopediaManager manager) {
        super(player);
        this.manager = manager;
        this.mobs = manager.getTargetMobs();
    }

    @Override
    public String getMenuName() {
        return "<dark_green>Enciclopedia de Mobs <gray>(" + manager.getCompletedCount() + "/" + manager.getTotalCount() + ")";
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();
        
        ItemBuilder infoBook = new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .name("<gold><bold>Progreso de la Enciclopedia")
                .lore("<gray>Completados: <yellow>" + manager.getCompletedCount() + "<gray>/</gray><gold>" + manager.getTotalCount());
        inventory.setItem(4, infoBook.build());
        
        int[] innerSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < getMaxItemsPerPage(); i++) {
            index = getMaxItemsPerPage() * page + i;
            if (index >= mobs.size()) break;

            EntityType type = mobs.get(index);
            boolean completed = manager.isMobCompleted(type);
            
            ItemBuilder icon;
            if (completed) {
                EncyclopediaRecord record = manager.getRecord(type);
                String date = dateFormat.format(new Date(record.getTimestamp()));
                icon = new ItemBuilder(getIcon(type))
                        .name("<green><bold>" + type.name())
                        .lore(
                                "<gray>Asesino: <yellow>" + record.getKillerName(),
                                "<gray>Método: <gold>" + record.getDeathMethod(),
                                "<gray>Dimensión: <white>" + record.getDimension(),
                                "<gray>Fecha: <white>" + date
                        );
            } else {
                icon = new ItemBuilder(Material.LIGHT_GRAY_DYE)
                        .name("<red>" + type.name())
                        .lore("<gray>Aún no registrado.");
            }
            
            modifyIcon(icon, type, completed);
            
            inventory.setItem(innerSlots[i], icon.build());
        }
    }
    
    protected void modifyIcon(ItemBuilder icon, EntityType type, boolean completed) {
        // For override in admin GUI
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
        
        handleCustomClick(e);
    }
    
    protected void handleCustomClick(InventoryClickEvent e) {
        // For override in admin GUI
    }

    protected Material getIcon(EntityType type) {
        if (type == EntityType.ILLUSIONER) return Material.BOW;
        if (type == EntityType.GIANT) return Material.ZOMBIE_HEAD;
        
        try {
            return Material.valueOf(type.name() + "_SPAWN_EGG");
        } catch (IllegalArgumentException ex) {
            return Material.EGG;
        }
    }
}

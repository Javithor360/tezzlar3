package com.panita.tezzlar3.inventory.gui;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.core.gui.PaginatedMenu;
import com.panita.tezzlar3.inventory.util.GravesDataManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import org.bukkit.Bukkit;
import org.bukkit.World;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerDeathsMenu extends PaginatedMenu {

    private final UUID targetUUID;
    private final String targetName;
    private List<Map.Entry<String, ConfigurationSection>> backups;

    public PlayerDeathsMenu(Player player, UUID targetUUID, String targetName) {
        super(player);
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.backups = GravesDataManager.getBackupsForPlayer(targetUUID);
    }

    @Override
    public String getMenuName() {
        return "<dark_red>Restauración <gray>></gray> " + targetName;
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
            if (index >= backups.size()) break;

            Map.Entry<String, ConfigurationSection> entry = backups.get(index);
            ConfigurationSection data = entry.getValue();

            String date = data.getString("diedAt", "Desconocida");
            String worldName = data.getString("world", "Desconocido");
            String dimension = worldName;
            World bukkitWorld = Bukkit.getWorld(worldName);
            if (bukkitWorld != null) {
                dimension = bukkitWorld.getEnvironment().name();
                if (dimension.equals("NORMAL")) {
                    dimension = "OVERWORLD";
                }
            }
            int x = data.getInt("x", 0);
            int y = data.getInt("y", 0);
            int z = data.getInt("z", 0);
            String deathCause = data.getString("deathCause", "Causa desconocida");
            String graveId = entry.getKey();

            boolean isActivated = !GravesDataManager.getActiveGraves().containsKey(graveId);
            String status = isActivated ? "<green>Tumba activada</green>" : "<yellow>A la espera</yellow>";

            ItemBuilder barrel = new ItemBuilder(Material.BARREL)
                    .name("<gold><bold>Muerte:</bold> " + date)
                    .lore(
                            "<gray>Mundo: <white>" + dimension,
                            "<gray>Ubicación: <red>X: " + x + " Y: " + y + " Z: " + z + "</red>",
                            "<gray>Causa: <white>" + deathCause,
                            "<gray>Estado: " + status,
                            "",
                            "<yellow>► Clic para ver inventario"
                    );

            inventory.setItem(innerSlots[i], barrel.build());
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
            if (!((index + 1) >= backups.size())) {
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
                if (clickedIndex < backups.size()) {
                    Map.Entry<String, ConfigurationSection> clickedEntry = backups.get(clickedIndex);
                    // Open the detailed inventory view
                    new DeathInventoryMenu(player, targetUUID, targetName, clickedEntry.getKey(), clickedEntry.getValue(), this).open();
                }
                break;
            }
        }
    }
}

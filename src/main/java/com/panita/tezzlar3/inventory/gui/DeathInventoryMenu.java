package com.panita.tezzlar3.inventory.gui;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.core.gui.Menu;
import com.panita.tezzlar3.inventory.util.GravesDataManager;
import com.panita.tezzlar3.inventory.util.InventorySerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DeathInventoryMenu extends Menu {

    private final UUID targetUUID;
    private final String targetName;
    private final String backupId;
    private final ConfigurationSection backupData;
    private final PlayerDeathsMenu previousMenu;
    private ItemStack[] deathItems;

    public DeathInventoryMenu(Player player, UUID targetUUID, String targetName, String backupId, ConfigurationSection backupData, PlayerDeathsMenu previousMenu) {
        super(player);
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.backupId = backupId;
        this.backupData = backupData;
        this.previousMenu = previousMenu;
        
        try {
            this.deathItems = InventorySerializer.fromBase64(backupData.getString("itemsBase64"));
        } catch (Exception e) {
            this.deathItems = new ItemStack[0];
            player.sendMessage("Error cargando el inventario Base64.");
        }
    }

    @Override
    public String getMenuName() {
        return "<dark_red>Inventario de " + targetName;
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void setMenuItems() {
        // Render the 41 items
        for (int i = 0; i < deathItems.length && i <= 40; i++) {
            if (deathItems[i] != null && !deathItems[i].isEmpty()) {
                inventory.setItem(i, deathItems[i]);
            }
        }

        // Borders
        for (int i = 41; i <= 45; i++) {
            inventory.setItem(i, FILLER_GLASS);
        }
        inventory.setItem(47, FILLER_GLASS);
        inventory.setItem(49, FILLER_GLASS);
        inventory.setItem(51, FILLER_GLASS);
        inventory.setItem(53, FILLER_GLASS);

        // Buttons
        inventory.setItem(46, new ItemBuilder(Material.CHEST).name("<gold>Obtener en Cofres").lore("<gray>Empaqueta el inventario en", "<gray>cofres y te los entrega.").build());
        inventory.setItem(48, new ItemBuilder(Material.EMERALD_BLOCK).name("<green>Restaurar al Jugador").lore("<gray>Envía los ítems directamente", "<gray>al inventario del jugador.", "<red>¡Debe estar conectado!").build());
        inventory.setItem(50, new ItemBuilder(Material.LAVA_BUCKET).name("<dark_red>Eliminar Registro").lore("<gray>Borra este historial de muerte", "<gray>permanentemente de la base de datos.").build());
        inventory.setItem(52, new ItemBuilder(Material.ARROW).name("<yellow>Volver Atrás").lore("<gray>Regresa a la lista de muertes.").build());
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        int slot = e.getSlot();

        if (slot == 52) { // Back
            previousMenu.open();
            return;
        }

        if (slot == 50) { // Delete Record
            GravesDataManager.deleteBackup(backupId);
            Messenger.prefixedSend(player, "&aRegistro eliminado correctamente.");
            player.closeInventory();
            return;
        }

        if (slot == 48) { // Restore directly to player
            Player target = Bukkit.getPlayer(targetUUID);
            if (target != null && target.isOnline()) {
                List<ItemStack> validItems = getValidItems();
                HashMap<Integer, ItemStack> overflow = target.getInventory().addItem(validItems.toArray(new ItemStack[0]));
                
                for (ItemStack overflowItem : overflow.values()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), overflowItem);
                }
                Messenger.prefixedSend(player, "&aSe han restaurado los ítems a &e" + target.getName() + "&a.");
                Messenger.prefixedSend(target, "&aUn administrador te ha devuelto tus ítems perdidos.");
                player.closeInventory();
            } else {
                Messenger.prefixedSend(player, "&cEl jugador debe estar conectado para recibir los ítems directamente.");
            }
            return;
        }

        if (slot == 46) { // Get as Chests
            List<ItemStack> chests = createChestsFromItems(getValidItems());
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(chests.toArray(new ItemStack[0]));
            
            for (ItemStack overflowItem : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), overflowItem);
            }
            Messenger.prefixedSend(player, "&aHas recibido &e" + chests.size() + " cofres &acon el inventario del jugador.");
            player.closeInventory();
            return;
        }
    }

    private List<ItemStack> getValidItems() {
        List<ItemStack> list = new ArrayList<>();
        for (ItemStack item : deathItems) {
            if (item != null && !item.isEmpty() && item.getType() != Material.AIR) {
                list.add(item);
            }
        }
        return list;
    }

    private List<ItemStack> createChestsFromItems(List<ItemStack> items) {
        List<ItemStack> chests = new ArrayList<>();
        Inventory tempInv = Bukkit.createInventory(null, 27);
        
        for (ItemStack item : items) {
            if (tempInv.firstEmpty() == -1) {
                chests.add(packChest(tempInv));
                tempInv.clear();
            }
            tempInv.addItem(item);
        }
        
        if (!tempInv.isEmpty()) {
            chests.add(packChest(tempInv));
        }
        
        return chests;
    }

    private ItemStack packChest(Inventory inv) {
        ItemStack chestItem = new ItemBuilder(Material.CHEST).name("<gold>Inventario Restaurado de " + targetName).build();
        BlockStateMeta meta = (BlockStateMeta) chestItem.getItemMeta();
        
        if (meta != null && meta.getBlockState() instanceof Container container) {
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && !item.isEmpty()) {
                    container.getInventory().setItem(i, item);
                }
            }
            meta.setBlockState(container);
            chestItem.setItemMeta(meta);
        }
        
        return chestItem;
    }
}

package com.panita.tezzlar3.minievents.gui;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.core.gui.Menu;
import com.panita.tezzlar3.minievents.MiniEvent;
import com.panita.tezzlar3.minievents.MiniEventManager;
import com.panita.tezzlar3.minievents.MiniEventsModule;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MiniEventSummonMenu extends Menu {

    public MiniEventSummonMenu(Player player) {
        super(player);
    }

    @Override
    public String getMenuName() {
        return "<dark_purple><b>Forzar Mini Evento</b></dark_purple>";
    }

    @Override
    public int getSlots() {
        return 27; // 3 rows
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();

        // Random option at slot 10 (center-left)
        ItemStack randomItem = new ItemBuilder(Material.ENDER_EYE)
                .name("<light_purple>Aleatorio</light_purple>")
                .lore("<gray>Elige un evento sorpresa</gray>", "<gray>como dicta la ruleta normal.</gray>")
                .build();
        inventory.setItem(10, randomItem);

        MiniEventManager manager = MiniEventsModule.getManager();
        if (manager != null) {
            List<MiniEvent> events = manager.getRegisteredEvents();
            int slot = 11;
            
            for (MiniEvent event : events) {
                if (slot > 16) break; // prevent overflowing the row
                
                Material icon = Material.PAPER;
                switch (event.getId()) {
                    case "uhc_mode" -> icon = Material.GOLDEN_APPLE;
                    case "random_potion" -> icon = Material.POTION;
                    case "position_swap" -> icon = Material.ENDER_PEARL;
                    case "special_drop_mob" -> icon = Material.GOLD_BLOCK;
                    case "blood_moon" -> icon = Material.JACK_O_LANTERN;
                }
                
                ItemStack eventItem = new ItemBuilder(icon)
                        .name(event.getGenericName())
                        .lore("<gray>Haz click para forzar</gray>", "<gray>este evento específico.</gray>", "", "<dark_gray>ID: " + event.getId() + "</dark_gray>")
                        .build();
                        
                inventory.setItem(slot, eventItem);
                slot++;
            }
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        
        MiniEventManager manager = MiniEventsModule.getManager();
        if (manager == null) return;
        
        if (manager.getActiveEvent() != null) {
            Messenger.prefixedSend(player, "<red>Ya hay un evento activo. Espera a que termine.</red>");
            player.closeInventory();
            return;
        }

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            return;
        }

        player.closeInventory();

        int slot = e.getSlot();
        if (slot == 10) {
            // Random
            manager.forceRoulette(null);
            Messenger.prefixedSend(player, "<green>Forzando Ruleta (Aleatorio)...</green>");
        } else if (slot >= 11 && slot <= 16) {
            // Specific event
            List<MiniEvent> events = manager.getRegisteredEvents();
            int index = slot - 11;
            if (index < events.size()) {
                MiniEvent target = events.get(index);
                manager.forceRoulette(target);
                Messenger.prefixedSend(player, "<green>Forzando Ruleta (Destino: " + target.getDisplayName() + "<green>)...</green>");
            }
        }
    }
}

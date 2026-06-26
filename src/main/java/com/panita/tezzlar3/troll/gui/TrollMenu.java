package com.panita.tezzlar3.troll.gui;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.core.gui.Menu;
import com.panita.tezzlar3.troll.util.TrollManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class TrollMenu extends Menu {

    private final Player target;

    public TrollMenu(Player executor, Player target) {
        super(executor);
        this.target = target;
    }

    @Override
    public String getMenuName() {
        return "<dark_purple>Troll <gray>></gray> " + target.getName();
    }

    @Override
    public int getSlots() {
        return 27; // 3 rows
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();

        // 1. Thor
        ItemStack thor = new ItemBuilder(Material.LIGHTNING_ROD)
                .name("<yellow><bold>Thor")
                .lore("<gray>Invoca un rayo celestial", "<gray>sobre la cabeza del jugador.")
                .build();
        inventory.setItem(10, thor);

        // 2. Shuffle Inventory
        ItemStack shuffle = new ItemBuilder(Material.CHEST)
                .name("<gold><bold>Shuffle Inventory")
                .lore("<gray>Mezcla aleatoriamente todos", "<gray>los ítems en su inventario.")
                .build();
        inventory.setItem(11, shuffle);

        // 3. Water Drop
        ItemStack waterDrop = new ItemBuilder(Material.WATER_BUCKET)
                .name("<aqua><bold>Water Drop")
                .lore("<gray>Lanza al jugador por los cielos", "<gray>y le da un cubo de agua.", "", "<red>No funciona en el Nether.")
                .build();
        inventory.setItem(12, waterDrop);

        // 4. Fake Creeper
        ItemStack creeper = new ItemBuilder(Material.CREEPER_HEAD)
                .name("<green><bold>Susto de Creeper")
                .lore("<gray>Spawnea un Creeper eléctrico", "<gray>falso detrás del jugador.")
                .build();
        inventory.setItem(13, creeper);

        // 5. Arachnophobia
        ItemStack arachno = new ItemBuilder(Material.COBWEB)
                .name("<dark_gray><bold>Arachnophobia")
                .lore("<gray>Atrapa instantáneamente al", "<gray>jugador en una telaraña.")
                .build();
        inventory.setItem(14, arachno);

        // 6. Silverfish Swarm
        ItemStack silverfish = new ItemBuilder(Material.INFESTED_STONE)
                .name("<gray><bold>Plaga de Lepismas")
                .lore("<gray>Invoca 5 lepismas agresivos,", "<gray>incluyendo 1 parásito.")
                .build();
        inventory.setItem(15, silverfish);

        // 7. Copper Trail
        ItemStack copper = new ItemBuilder(Material.RAW_COPPER_BLOCK)
                .name("<color:#d97654><bold>Copper Trail")
                .lore("<gray>Durante 5s, el bloque bajo", "<gray>sus pies se vuelve cobre.")
                .build();
        inventory.setItem(16, copper);

        // 8. Half Heart
        ItemStack halfHeart = new ItemBuilder(Material.FERMENTED_SPIDER_EYE)
                .name("<dark_red><bold>Medio Corazón")
                .lore("<gray>Reduce instantáneamente la", "<gray>vida del jugador a medio corazón.")
                .build();
        inventory.setItem(19, halfHeart);

        // 9. Starve
        ItemStack starve = new ItemBuilder(Material.ROTTEN_FLESH)
                .name("<color:#8B4513><bold>Hambruna")
                .lore("<gray>Vacia completamente la barra", "<gray>de comida y saturación.")
                .build();
        inventory.setItem(20, starve);
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (!target.isOnline()) {
            Messenger.prefixedSend(player, "&cEl jugador " + target.getName() + " se desconectó.");
            player.closeInventory();
            return;
        }

        int slot = e.getSlot();
        
        switch (slot) {
            case 10:
                TrollManager.executeThor(target);
                Messenger.prefixedSend(player, "&aTrolleo &eThor &aaplicado a " + target.getName());
                break;
            case 11:
                TrollManager.executeShuffle(target);
                Messenger.prefixedSend(player, "&aTrolleo &eShuffle &aaplicado a " + target.getName());
                break;
            case 12:
                boolean success = TrollManager.executeWaterDrop(player, target);
                if (success) {
                    Messenger.prefixedSend(player, "&aTrolleo &eWater Drop &aaplicado a " + target.getName());
                } else {
                    Messenger.prefixedSend(player, "&cNo puedes usar Water Drop si el jugador está en el Nether.");
                }
                break;
            case 13:
                TrollManager.executeFakeCreeper(target);
                Messenger.prefixedSend(player, "&aTrolleo &eFake Creeper &aaplicado a " + target.getName());
                break;
            case 14:
                TrollManager.executeArachnophobia(target);
                Messenger.prefixedSend(player, "&aTrolleo &eArachnophobia &aaplicado a " + target.getName());
                break;
            case 15:
                TrollManager.executeSilverfishSwarm(target);
                Messenger.prefixedSend(player, "&aTrolleo &eSilverfish Swarm &aaplicado a " + target.getName());
                break;
            case 16:
                TrollManager.enableCopperTrail(target);
                Messenger.prefixedSend(player, "&aTrolleo &eCopper Trail &aaplicado a " + target.getName());
                break;
            case 19:
                TrollManager.executeHalfHeart(target);
                Messenger.prefixedSend(player, "&aTrolleo &eMedio Corazón &aaplicado a " + target.getName());
                break;
            case 20:
                TrollManager.executeStarve(target);
                Messenger.prefixedSend(player, "&aTrolleo &eHambruna &aaplicado a " + target.getName());
                break;
        }
        
        if ((slot >= 10 && slot <= 16) || slot == 19 || slot == 20) {
            player.closeInventory();
        }
    }
}

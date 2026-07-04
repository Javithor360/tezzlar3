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
        return 36; // 4 rows
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

        // 10. Obsidian Box
        ItemStack obsidianBox = new ItemBuilder(Material.OBSIDIAN)
                .name("<color:#4a325e><bold>Caja de Obsidiana")
                .lore("<gray>Encierra al jugador en", "<gray>una caja de obsidiana.")
                .build();
        inventory.setItem(21, obsidianBox);

        // 11. Fake Death
        ItemStack fakeDeath = new ItemBuilder(Material.SKELETON_SKULL)
                .name("<red><bold>Muerte Falsa")
                .lore("<gray>Simula una muerte hardcore", "<gray>solo visible cerca de él.")
                .build();
        inventory.setItem(22, fakeDeath);

        // 12. Fake Totem
        ItemStack fakeTotem = new ItemBuilder(Material.TOTEM_OF_UNDYING)
                .name("<yellow><bold>Tótem Falso")
                .lore("<gray>Finge la activación de", "<gray>un tótem de inmortalidad.")
                .build();
        inventory.setItem(23, fakeTotem);

        // 13. Drop Item
        ItemStack dropItem = new ItemBuilder(Material.DROPPER)
                .name("<white><bold>Drop Item")
                .lore("<gray>Fuerza al jugador a dropear", "<gray>el ítem que tiene en su mano.")
                .build();
        inventory.setItem(24, dropItem);

        // 14. Swap Hands
        ItemStack swapHands = new ItemBuilder(Material.STICK)
                .name("<light_purple><bold>Cambio de Manos")
                .lore("<gray>Intercambia los ítems entre", "<gray>su mano principal y secundaria.")
                .build();
        inventory.setItem(25, swapHands);

        // 15. Fake Warden
        ItemStack fakeWarden = new ItemBuilder(Material.SCULK_SHRIEKER)
                .name("<dark_aqua><bold>Warden Falso")
                .lore("<gray>Aplica oscuridad y reproduce", "<gray>el sonido de spawn de un Warden.")
                .build();
        inventory.setItem(28, fakeWarden);
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
            case 21:
                TrollManager.executeObsidianBox(target);
                Messenger.prefixedSend(player, "&aTrolleo &eCaja de Obsidiana &aaplicado a " + target.getName());
                break;
            case 22:
                TrollManager.executeFakeDeath(target);
                Messenger.prefixedSend(player, "&aTrolleo &eMuerte Falsa &aaplicado a " + target.getName());
                break;
            case 23:
                TrollManager.executeFakeTotem(target);
                Messenger.prefixedSend(player, "&aTrolleo &eTótem Falso &aaplicado a " + target.getName());
                break;
            case 24:
                TrollManager.executeDropItem(target);
                Messenger.prefixedSend(player, "&aTrolleo &eDrop Item &aaplicado a " + target.getName());
                break;
            case 25:
                TrollManager.executeSwapHands(target);
                Messenger.prefixedSend(player, "&aTrolleo &eCambio de Manos &aaplicado a " + target.getName());
                break;
            case 28:
                TrollManager.executeFakeWarden(target);
                Messenger.prefixedSend(player, "&aTrolleo &eWarden Falso &aaplicado a " + target.getName());
                break;
        }
        
        if ((slot >= 10 && slot <= 16) || (slot >= 19 && slot <= 25) || slot == 28) {
            player.closeInventory();
        }
    }
}

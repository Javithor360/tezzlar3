package com.panita.tezzlar3.troll.gui;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.core.gui.Menu;
import com.panita.tezzlar3.troll.util.TrollManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class TrollLiteMenu extends Menu {

    private final Player target;

    public TrollLiteMenu(Player executor, Player target) {
        super(executor);
        this.target = target;
    }

    @Override
    public String getMenuName() {
        return "<dark_purple>Troll Lite <gray>></gray> " + target.getName();
    }

    @Override
    public int getSlots() {
        return 27; // 3 rows
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();

        // 10. Fake Death
        ItemStack fakeDeath = new ItemBuilder(Material.SKELETON_SKULL)
                .name("<red><bold>Muerte Falsa")
                .lore("<gray>Simula una muerte hardcore", "<gray>solo visible cerca de él.")
                .build();
        inventory.setItem(10, fakeDeath);

        // 11. Fake Totem
        ItemStack fakeTotem = new ItemBuilder(Material.TOTEM_OF_UNDYING)
                .name("<yellow><bold>Tótem Falso")
                .lore("<gray>Finge la activación de", "<gray>un tótem de inmortalidad.")
                .build();
        inventory.setItem(11, fakeTotem);

        // 12. Debris Trail
        ItemStack debris = new ItemBuilder(Material.ANCIENT_DEBRIS)
                .name("<color:#6D4C41><bold>Debris Trail")
                .lore("<gray>Durante 5s, el bloque bajo", "<gray>sus pies se vuelve escombros antiguos.")
                .build();
        inventory.setItem(12, debris);

        // 13. Swap Hands
        ItemStack swapHands = new ItemBuilder(Material.STICK)
                .name("<light_purple><bold>Cambio de Manos")
                .lore("<gray>Intercambia los ítems entre", "<gray>su mano principal y secundaria.")
                .build();
        inventory.setItem(13, swapHands);

        // 14. Arachnophobia
        ItemStack arachno = new ItemBuilder(Material.COBWEB)
                .name("<dark_gray><bold>Aracnofobia")
                .lore("<gray>Atrapa instantáneamente al", "<gray>jugador en una telaraña.")
                .build();
        inventory.setItem(14, arachno);

        Material fakeThorMat = Material.matchMaterial("WEATHERED_LIGHTNING_ROD");
        if (fakeThorMat == null) fakeThorMat = Material.matchMaterial("OXIDIZED_LIGHTNING_ROD");
        if (fakeThorMat == null) fakeThorMat = Material.LIGHTNING_ROD;

        // 15. Fake Thor
        ItemStack fakeThor = new ItemBuilder(fakeThorMat)
                .name("<yellow><bold>Thor Falso")
                .lore("<gray>Invoca un rayo inofensivo", "<gray>sobre el jugador.")
                .build();
        inventory.setItem(15, fakeThor);

        // 16. Fake Explosion
        ItemStack fakeExplosion = new ItemBuilder(Material.TNT)
                .name("<red><bold>Explosión Falsa")
                .lore("<gray>Genera una explosión visual", "<gray>que no hace daño al jugador.")
                .build();
        inventory.setItem(16, fakeExplosion);
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (!target.isOnline()) {
            Messenger.prefixedSend(player, "&cEl jugador " + target.getName() + " se desconectó.");
            player.closeInventory();
            return;
        }

        if (!player.getWorld().equals(target.getWorld()) || player.getLocation().distance(target.getLocation()) > 20.0) {
            Messenger.prefixedSend(player, "&cEl jugador se alejó demasiado (más de 20 bloques).");
            player.closeInventory();
            return;
        }

        int slot = e.getSlot();
        
        switch (slot) {
            case 10:
                TrollManager.executeFakeDeath(target);
                Messenger.prefixedSend(player, "&aTrolleo &eMuerte Falsa &aaplicado a " + target.getName());
                break;
            case 11:
                TrollManager.executeFakeTotem(target);
                Messenger.prefixedSend(player, "&aTrolleo &eTótem Falso &aaplicado a " + target.getName());
                break;
            case 12:
                TrollManager.enableDebrisTrail(target);
                Messenger.prefixedSend(player, "&aTrolleo &eDebris Trail &aaplicado a " + target.getName());
                break;
            case 13:
                TrollManager.executeSwapHands(target);
                Messenger.prefixedSend(player, "&aTrolleo &eCambio de Manos &aaplicado a " + target.getName());
                break;
            case 14:
                TrollManager.executeArachnophobia(target);
                Messenger.prefixedSend(player, "&aTrolleo &eAracnofobia &aaplicado a " + target.getName());
                break;
            case 15:
                TrollManager.executeFakeThor(target);
                Messenger.prefixedSend(player, "&aTrolleo &eThor Falso &aaplicado a " + target.getName());
                break;
            case 16:
                TrollManager.executeFakeExplosion(target);
                Messenger.prefixedSend(player, "&aTrolleo &eExplosión Falsa &aaplicado a " + target.getName());
                break;
        }
        
        if (slot >= 10 && slot <= 16) {
            player.closeInventory();
        }
    }
}

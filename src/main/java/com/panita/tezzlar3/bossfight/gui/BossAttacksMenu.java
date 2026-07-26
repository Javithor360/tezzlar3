package com.panita.tezzlar3.bossfight.gui;

import com.panita.tezzlar3.bossfight.util.BossAttacks;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.core.gui.Menu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffectType;

public class BossAttacksMenu extends Menu {

    public BossAttacksMenu(Player player) {
        super(player);
    }

    @Override
    public String getMenuName() {
        return "<red><bold>Ataques del Jefe";
    }

    @Override
    public int getSlots() {
        return 54; // 6 rows for all attacks
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        
        // 12 Attacks
        inventory.setItem(10, new ItemBuilder(Material.LIGHTNING_ROD)
                .name("<yellow><bold>Rayos (Thor Pro)")
                .lore("<gray>Golpea con rayos a todos los jugadores cercanos.")
                .build());
                
        inventory.setItem(11, new ItemBuilder(Material.COBWEB)
                .name("<dark_purple><bold>Aracnofobia")
                .lore("<gray>Coloca telarañas en 3x3 y genera una araña por jugador.")
                .build());
                
        inventory.setItem(12, new ItemBuilder(Material.MAGMA_BLOCK)
                .name("<gold><bold>Suelo de Magmablocks")
                .lore("<gray>Pone rastro de magmablock bajo sus pies por 10s.")
                .build());
                
        inventory.setItem(13, new ItemBuilder(Material.BLACK_DYE)
                .name("<dark_gray><bold>Ceguera")
                .lore("<gray>Aplica Ceguera a todos (10s).")
                .build());
                
        inventory.setItem(14, new ItemBuilder(Material.POISONOUS_POTATO)
                .name("<green><bold>Náusea")
                .lore("<gray>Aplica Náusea a todos (20s).")
                .build());
                
        inventory.setItem(15, new ItemBuilder(Material.WOODEN_PICKAXE)
                .name("<dark_red><bold>Fatiga de Minería IV")
                .lore("<gray>Aplica Fatiga IV a todos (30s).")
                .build());
                
        inventory.setItem(16, new ItemBuilder(Material.SOUL_SAND)
                .name("<gray><bold>Lentitud III")
                .lore("<gray>Aplica Lentitud III a todos (15s).")
                .build());
                
        inventory.setItem(19, new ItemBuilder(Material.WITHER_ROSE)
                .name("<dark_purple><bold>Debilidad III")
                .lore("<gray>Aplica Debilidad III a todos (30s).")
                .build());
                
        inventory.setItem(20, new ItemBuilder(Material.WITHER_SKELETON_SKULL)
                .name("<black><bold>Wither II")
                .lore("<gray>Aplica Wither II a todos (30s).")
                .build());
                
        inventory.setItem(21, new ItemBuilder(Material.SPAWNER)
                .name("<red><bold>Invocación: Vestigios Errantes")
                .lore("<gray>Invoca 3-5 mobs aleatorios sobre cada jugador.")
                .build());
                
        inventory.setItem(22, new ItemBuilder(Material.STRUCTURE_VOID)
                .name("<blue><bold>Shuffle de Inventario")
                .lore("<gray>Desordena aleatoriamente el inventario de todos.")
                .build());
                
        inventory.setItem(23, new ItemBuilder(Material.BEACON)
                .name("<yellow><bold>Rayo Cargado Masivo")
                .lore("<gray>Drena vida de todos los jugadores tras 8 segundos.")
                .build());
                
        // Return button
        inventory.setItem(49, new ItemBuilder(Material.ARROW)
                .name("<red><bold>Volver al Menú General")
                .build());
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getSlot();
        Player boss = player; // The menu opener is the boss

        switch (slot) {
            case 10:
                BossAttacks.executeThorPro(boss);
                break;
            case 11:
                BossAttacks.executeArachnophobia(boss);
                break;
            case 12:
                BossAttacks.executeMagmaFloor(boss);
                break;
            case 13:
                BossAttacks.applyPotionEffect(boss, PotionEffectType.BLINDNESS, 10, 1, "Ceguera");
                break;
            case 14:
                BossAttacks.applyPotionEffect(boss, PotionEffectType.NAUSEA, 20, 1, "Náusea");
                break;
            case 15:
                BossAttacks.applyPotionEffect(boss, PotionEffectType.MINING_FATIGUE, 30, 4, "Fatiga de Minería IV");
                break;
            case 16:
                BossAttacks.applyPotionEffect(boss, PotionEffectType.SLOWNESS, 15, 3, "Lentitud III");
                break;
            case 19:
                BossAttacks.applyPotionEffect(boss, PotionEffectType.WEAKNESS, 30, 3, "Debilidad III");
                break;
            case 20:
                BossAttacks.applyPotionEffect(boss, PotionEffectType.WITHER, 30, 2, "Wither II");
                break;
            case 21:
                BossAttacks.spawnRandomMobs(boss);
                break;
            case 22:
                BossAttacks.executeInventoryShuffle(boss);
                break;
            case 23:
                BossAttacks.executeChargedBeam(boss);
                break;
            case 49:
                new BossGeneralMenu(player).open();
                return;
            default:
                return; // Not an attack slot
        }

        boss.playSound(boss.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }
}

package com.panita.tezzlar3.bossfight.gui;

import com.panita.tezzlar3.bossfight.util.BossAttacks;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.core.gui.Menu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
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
        
        int phase = com.panita.tezzlar3.bossfight.util.BossManager.getInstance().getCurrentPhase();
        ItemStack locked2 = new ItemBuilder(Material.BARRIER).name("<red><bold>Bloqueado (Fase 2)").lore("<gray>Requiere Fase 2.").build();
        ItemStack locked3 = new ItemBuilder(Material.BARRIER).name("<red><bold>Bloqueado (Fase 3)").lore("<gray>Requiere Fase 3.").build();
        ItemStack locked4 = new ItemBuilder(Material.BARRIER).name("<red><bold>Bloqueado (Fase 4)").lore("<gray>Requiere Fase 4.").build();
        
        // --- Phase 2 Attacks ---
        inventory.setItem(10, phase >= 2 ? new ItemBuilder(Material.LIGHTNING_ROD).name("<yellow><bold>Rayos (Thor Pro)").lore("<gray>Golpea con rayos a todos los jugadores cercanos.").build() : locked2);
        inventory.setItem(11, phase >= 2 ? new ItemBuilder(Material.COBWEB).name("<dark_purple><bold>Aracnofobia").lore("<gray>Coloca telarañas en 3x3 y genera una araña por jugador.").build() : locked2);
        inventory.setItem(12, phase >= 2 ? new ItemBuilder(Material.MAGMA_BLOCK).name("<gold><bold>Suelo de Magmablocks").lore("<gray>Pone rastro de magmablock bajo sus pies por 10s.").build() : locked2);
        inventory.setItem(13, phase >= 2 ? new ItemBuilder(Material.BLACK_DYE).name("<dark_gray><bold>Ceguera").lore("<gray>Aplica Ceguera a todos (10s).").build() : locked2);
        inventory.setItem(14, phase >= 2 ? new ItemBuilder(Material.POISONOUS_POTATO).name("<green><bold>Náusea").lore("<gray>Aplica Náusea a todos (20s).").build() : locked2);
        inventory.setItem(15, phase >= 2 ? new ItemBuilder(Material.WOODEN_PICKAXE).name("<dark_red><bold>Fatiga de Minería IV").lore("<gray>Aplica Fatiga IV a todos (30s).").build() : locked2);
        inventory.setItem(16, phase >= 2 ? new ItemBuilder(Material.SOUL_SAND).name("<gray><bold>Lentitud III").lore("<gray>Aplica Lentitud III a todos (15s).").build() : locked2);
        
        inventory.setItem(20, phase >= 2 ? new ItemBuilder(Material.WITHER_ROSE).name("<dark_purple><bold>Debilidad III").lore("<gray>Aplica Debilidad III a todos (30s).").build() : locked2);
        inventory.setItem(21, phase >= 2 ? new ItemBuilder(Material.WITHER_SKELETON_SKULL).name("<black><bold>Wither II").lore("<gray>Aplica Wither II a todos (30s).").build() : locked2);
        inventory.setItem(22, phase >= 2 ? new ItemBuilder(Material.SPAWNER).name("<red><bold>Invocación: Vestigios Errantes").lore("<gray>Invoca 3-5 mobs aleatorios sobre cada jugador.").build() : locked2);
        inventory.setItem(23, phase >= 2 ? new ItemBuilder(Material.STRUCTURE_VOID).name("<blue><bold>Shuffle de Inventario").lore("<gray>Desordena aleatoriamente el inventario de todos.").build() : locked2);
        inventory.setItem(24, phase >= 2 ? new ItemBuilder(Material.BEACON).name("<yellow><bold>Rayo Cargado Masivo").lore("<gray>Drena vida de todos los jugadores tras 8 segundos.").build() : locked2);
                
        // --- Phase 3 Attacks ---
        inventory.setItem(28, phase >= 3 ? new ItemBuilder(Material.ZOMBIE_HEAD).name("<dark_red><bold>Invocación de Javimobs").lore("<gray>Invoca 1-4 Javimobs de élite sobre cada jugador.").build() : locked3);
        inventory.setItem(29, phase >= 3 ? new ItemBuilder(Material.END_CRYSTAL).name("<dark_purple><bold>Vórtice de la Traición").lore("<gray>Atrae y daña a todos los jugadores cercanos.").build() : locked3);
        inventory.setItem(30, phase >= 3 ? new ItemBuilder(Material.BEDROCK).name("<dark_gray><bold>Picos Rocosos").lore("<gray>Hace emerger picos de bedrock que", "<gray>lanzan a los jugadores por los aires.").build() : locked3);
        
        inventory.setItem(32, phase >= 3 ? new ItemBuilder(Material.SNOWBALL).name("<white><bold>Lluvia Atemporal").lore("<gray>Lluvia caótica de fuego y nieve.").build() : locked3);
        inventory.setItem(33, phase >= 3 ? new ItemBuilder(Material.ENDER_PEARL).name("<light_purple><bold>Intercambio de Posiciones").lore("<gray>Teletransporta aleatoriamente a todos", "<gray>los jugadores en el campo de batalla.").build() : locked3);
        inventory.setItem(34, phase >= 3 ? new ItemBuilder(Material.SPIDER_EYE).name("<dark_green><bold>Zonas Tóxicas").lore("<gray>Despliega áreas tóxicas en el suelo", "<gray>que dañan rápidamente a quien las pise.").build() : locked3);
        
        // --- Phase 4 Attacks ---
        inventory.setItem(40, phase >= 4 ? new ItemBuilder(Material.NETHERITE_BLOCK).name("<dark_red><bold>Adoración Divina").lore("<gray>Exige la adoración de estatuas", "<gray>divinas, forzando a los", "<gray>jugadores a destruirlas.").build() : locked4);
                
        // Return button
        inventory.setItem(49, new ItemBuilder(Material.ARROW)
                .name("<red><bold>Volver al Menú General")
                .build());
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BARRIER) {
            return;
        }

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
            case 20:
                BossAttacks.applyPotionEffect(boss, PotionEffectType.WEAKNESS, 30, 3, "Debilidad III");
                break;
            case 21:
                BossAttacks.applyPotionEffect(boss, PotionEffectType.WITHER, 30, 2, "Wither II");
                break;
            case 22:
                BossAttacks.spawnRandomMobs(boss);
                break;
            case 23:
                BossAttacks.executeInventoryShuffle(boss);
                break;
            case 24:
                BossAttacks.executeChargedBeam(boss);
                break;
            case 28:
                BossAttacks.spawnJavimobs(boss);
                break;
            case 29:
                BossAttacks.executeBetrayalVortex(boss);
                break;
            case 30:
                BossAttacks.executeRockySpikes(boss);
                break;
            case 32:
                BossAttacks.executeTimelessRain(boss);
                break;
            case 33:
                BossAttacks.executePositionSwap(boss);
                break;
            case 34:
                BossAttacks.executeToxicZones(boss);
                break;
            case 40:
                BossAttacks.executeDivineAdoration(boss);
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

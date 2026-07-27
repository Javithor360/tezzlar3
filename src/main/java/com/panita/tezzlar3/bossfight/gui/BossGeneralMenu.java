package com.panita.tezzlar3.bossfight.gui;

import com.panita.tezzlar3.bossfight.util.BossManager;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.core.gui.Menu;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BossGeneralMenu extends Menu {

    public BossGeneralMenu(Player player) {
        super(player);
    }

    @Override
    public String getMenuName() {
        return "<dark_red><bold>Menú General - Jefe";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        BossManager manager = BossManager.getInstance();
        int currentPhase = manager.getCurrentPhase();
        boolean fakeDeath = manager.isFakeDeathState();

        // Phase 1
        inventory.setItem(10, getPhaseItem(Material.WOODEN_SWORD, "Fase 1", 1, currentPhase));
        
        // Phase 2
        inventory.setItem(11, getPhaseItem(Material.IRON_SWORD, "Fase 2", 2, currentPhase));
        
        // Phase 3
        inventory.setItem(12, getPhaseItem(Material.DIAMOND_SWORD, "Fase 3", 3, currentPhase));
        
        // Phase 4
        inventory.setItem(13, getPhaseItem(Material.NETHERITE_SWORD, "Fase 4", 4, currentPhase));

        // Fake Death Indicator / Next Phase Button
        if (fakeDeath) {
            ItemStack nextPhaseItem = new ItemBuilder(Material.TOTEM_OF_UNDYING)
                    .name("<yellow><bold>Avanzar Fase (Muerte Falsa)")
                    .lore("<gray>Estás en estado de recarga.", "<gray>Haz clic para pasar a la", "<gray>siguiente fase y revivir.")
                    .build();
            ItemMeta meta = nextPhaseItem.getItemMeta();
            if (meta != null) {
                meta.setEnchantmentGlintOverride(true);
                nextPhaseItem.setItemMeta(meta);
            }
            inventory.setItem(22, nextPhaseItem);
        }

        // Abrir menú de ataques
        inventory.setItem(14, new ItemBuilder(Material.BLAZE_POWDER)
                .name("<gold><bold>Abrir Arsenal de Ataques")
                .lore("<gray>Haz clic para ver y usar los", "<gray>ataques disponibles.")
                .build());

        // Full Heal
        inventory.setItem(15, new ItemBuilder(Material.ENCHANTED_GOLDEN_APPLE)
                .name("<light_purple><bold>Curar Completamente")
                .lore("<gray>Restaura tu vida al 100%.")
                .build());

        // Kit Fase 1
        inventory.setItem(20, new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                .name("<aqua><bold>Recibir Kit Fase 1")
                .lore("<gray>Guarda el set de Superdiamond Lvl 2", "<gray>y las armas en tu inventario.")
                .build());

        // Stop Fight
        inventory.setItem(16, new ItemBuilder(Material.BARRIER)
                .name("<dark_red><bold>Detener Pelea")
                .lore("<gray>Finaliza la batalla de jefe,", "<gray>limpia la BossBar y atributos.")
                .build());
    }

    private ItemStack getPhaseItem(Material material, String name, int phase, int currentPhase) {
        boolean isActive = (phase == currentPhase);
        ItemStack item = new ItemBuilder(material)
                .name((isActive ? "<green><bold>" : "<gray><bold>") + name)
                .lore(isActive ? "<green>Fase Actual" : "<yellow>Click para cambiar a esta fase")
                .build();

        if (isActive) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setEnchantmentGlintOverride(true);
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getSlot();
        BossManager manager = BossManager.getInstance();

        switch (slot) {
            case 10:
                manager.setPhase(1);
                Messenger.prefixedSend(player, "&aCambiado a Fase 1.");
                setMenuItems();
                break;
            case 11:
                manager.setPhase(2);
                Messenger.prefixedSend(player, "&aCambiado a Fase 2.");
                setMenuItems();
                break;
            case 12:
                manager.setPhase(3);
                Messenger.prefixedSend(player, "&aCambiado a Fase 3.");
                setMenuItems();
                break;
            case 13:
                manager.setPhase(4);
                Messenger.prefixedSend(player, "&aCambiado a Fase 4.");
                setMenuItems();
                break;
            case 14:
                new BossAttacksMenu(player).open();
                break;
            case 15:
                if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
                    player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
                    manager.updateBossBar();
                    Messenger.prefixedSend(player, "&d¡Vida restaurada por completo!");
                }
                break;
            case 16:
                player.closeInventory();
                manager.stopFight();
                Messenger.prefixedSend(player, "&cLa pelea ha terminado.");
                break;
            case 20:
                equipPhase1Kit();
                Messenger.prefixedSend(player, "&a¡Kit de la Fase 1 recibido en tu inventario!");
                player.closeInventory();
                break;
            case 22:
                if (manager.isFakeDeathState()) {
                    int nextPhase = manager.getCurrentPhase() + 1;
                    if (nextPhase <= 4) {
                        manager.setPhase(nextPhase);
                        Messenger.prefixedSend(player, "&e¡Avanzaste a la Fase " + nextPhase + "!");
                        setMenuItems();
                    } else {
                        Messenger.prefixedSend(player, "&cYa estás en la última fase.");
                    }
                }
                break;
        }
    }

    private void equipPhase1Kit() {
        // Armadura (guardada en inventario)
        ItemStack helmet = getEnchantedSuperdiamond("white_superdiamond_helmet_lvl2", true);
        ItemStack chestplate = getEnchantedSuperdiamond("white_superdiamond_chestplate_lvl2", true);
        ItemStack leggings = getEnchantedSuperdiamond("white_superdiamond_leggings_lvl2", true);
        ItemStack boots = getEnchantedSuperdiamond("white_superdiamond_boots_lvl2", true);

        if (helmet != null) player.getInventory().addItem(helmet);
        if (chestplate != null) player.getInventory().addItem(chestplate);
        if (leggings != null) player.getInventory().addItem(leggings);
        if (boots != null) player.getInventory().addItem(boots);

        // Espada
        ItemStack sword = CustomItemManager.getItem("superdiamond_sword");
        if (sword != null) {
            addEnchant(sword, "sharpness", 15);
            addEnchant(sword, "fire_aspect", 10);
            setUnbreakable(sword);
            player.getInventory().addItem(sword);
        }

        // Arco
        ItemStack bow = CustomItemManager.getItem("superdiamond_bow");
        if (bow != null) {
            addEnchant(bow, "power", 50);
            addEnchant(bow, "punch", 20);
            addEnchant(bow, "flame", 1);
            addEnchant(bow, "infinity", 1);
            setUnbreakable(bow);
            player.getInventory().addItem(bow);
            
            // Entregar 1 flecha
            player.getInventory().addItem(new ItemStack(Material.ARROW, 1));
        }
    }

    private ItemStack getEnchantedSuperdiamond(String id, boolean isArmor) {
        ItemStack item = CustomItemManager.getItem(id);
        if (item == null) return null;

        if (isArmor) {
            addEnchant(item, "protection", 10);
            addEnchant(item, "fire_protection", 10);
            addEnchant(item, "blast_protection", 10);
            addEnchant(item, "projectile_protection", 10);
        }

        setUnbreakable(item);
        return item;
    }

    private void addEnchant(ItemStack item, String key, int level) {
        Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key));
        if (ench != null) {
            item.addUnsafeEnchantment(ench, level);
        }
    }

    private void setUnbreakable(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
    }
}

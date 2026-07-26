package com.panita.tezzlar3.bossfight.gui;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.core.gui.Menu;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

        inventory.setItem(10, new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                .name("<aqua><bold>Recibir Kit Fase 1")
                .lore("<gray>Equipa el set de Superdiamond Lvl 2", "<gray>y las armas al máximo nivel.")
                .build());
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getSlot();

        if (slot == 10) {
            equipPhase1Kit();
            Messenger.prefixedSend(player, "&a¡Kit de la Fase 1 equipado!");
            player.closeInventory();
        }
    }

    private void equipPhase1Kit() {
        ItemStack helmet = getEnchantedSuperdiamond("superdiamond_helmet_lvl2", true);
        ItemStack chestplate = getEnchantedSuperdiamond("superdiamond_chestplate_lvl2", true);
        ItemStack leggings = getEnchantedSuperdiamond("superdiamond_leggings_lvl2", true);
        ItemStack boots = getEnchantedSuperdiamond("superdiamond_boots_lvl2", true);

        if (helmet != null) player.getInventory().setHelmet(helmet);
        if (chestplate != null) player.getInventory().setChestplate(chestplate);
        if (leggings != null) player.getInventory().setLeggings(leggings);
        if (boots != null) player.getInventory().setBoots(boots);

        // Espada (Slot 0)
        ItemStack sword = CustomItemManager.getItem("superdiamond_sword");
        if (sword != null) {
            addEnchant(sword, "sharpness", 15);
            addEnchant(sword, "fire_aspect", 10);
            setUnbreakable(sword);
            player.getInventory().setItem(0, sword);
        }

        // Arco (Slot 1)
        ItemStack bow = CustomItemManager.getItem("superdiamond_bow");
        if (bow != null) {
            addEnchant(bow, "power", 50);
            addEnchant(bow, "punch", 20);
            addEnchant(bow, "flame", 1);
            addEnchant(bow, "infinity", 1);
            setUnbreakable(bow);
            player.getInventory().setItem(1, bow);

            player.getInventory().setItem(10, new ItemStack(Material.ARROW, 1));
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

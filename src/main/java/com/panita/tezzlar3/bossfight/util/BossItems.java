package com.panita.tezzlar3.bossfight.util;

import com.panita.tezzlar3.core.gui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import com.panita.tezzlar3.Tezzlar;

public class BossItems {

    public static final NamespacedKey BOSS_ITEM_KEY = new NamespacedKey(Tezzlar.getInstance(), "boss_item");
    public static final String GENERAL_MENU = "general_menu";
    public static final String ATTACKS_MENU = "attacks_menu";
    public static final String ATTRIBUTES_MENU = "attributes_menu";

    public static void giveBossItems(Player player) {
        // General Menu Item (Slot 6)
        ItemStack generalItem = new ItemBuilder(Material.NETHER_STAR)
                .name("<gold><bold>Menú General")
                .lore("<gray>Haz clic derecho para gestionar", "<gray>la pelea y las fases.")
                .build();
        setItemTag(generalItem, GENERAL_MENU);
        player.getInventory().setItem(6, generalItem);

        // Attacks Menu Item (Slot 7)
        ItemStack attacksItem = new ItemBuilder(Material.BLAZE_POWDER)
                .name("<red><bold>Menú de Ataques")
                .lore("<gray>Haz clic derecho para abrir", "<gray>el arsenal de ataques.")
                .build();
        setItemTag(attacksItem, ATTACKS_MENU);
        player.getInventory().setItem(7, attacksItem);

        // Attributes Menu Item (Slot 8)
        ItemStack attributesItem = new ItemBuilder(Material.EMERALD)
                .name("<green><bold>Atributos y Efectos")
                .lore("<gray>Haz clic derecho para modificar", "<gray>tus estadísticas al vuelo.")
                .build();
        setItemTag(attributesItem, ATTRIBUTES_MENU);
        player.getInventory().setItem(8, attributesItem);
    }

    public static void removeBossItems(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isBossItem(item)) {
                player.getInventory().setItem(i, null);
            }
        }
    }

    private static void setItemTag(ItemStack item, String value) {
        if (item == null || item.getItemMeta() == null) return;
        item.editMeta(meta -> {
            meta.getPersistentDataContainer().set(BOSS_ITEM_KEY, PersistentDataType.STRING, value);
        });
    }

    public static boolean isBossItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(BOSS_ITEM_KEY, PersistentDataType.STRING);
    }

    public static String getBossItemType(ItemStack item) {
        if (!isBossItem(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(BOSS_ITEM_KEY, PersistentDataType.STRING);
    }
}

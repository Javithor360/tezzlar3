package com.panita.tezzlar3.bossfight.util;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class EndingItems {

    public static final NamespacedKey ENDING_ITEM_KEY = new NamespacedKey(Tezzlar.getInstance(), "ending_item_type");

    public static final String TYPE_FINAL_MESSAGE = "final_message";
    public static final String TYPE_INSTANT_PORTAL = "instant_portal";
    public static final String TYPE_SALVATION = "salvation";
    public static final String TYPE_SUNLIGHT = "sunlight";

    public static ItemStack getFinalMessageItem() {
        ItemStack item = new ItemBuilder(Material.WRITTEN_BOOK)
                .name("<color:#FFD700>Mensaje Final</color>")
                .lore("<gray>Comienza la secuencia de</gray>", "<gray>títulos de despedida del servidor.</gray>")
                .build();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(ENDING_ITEM_KEY, PersistentDataType.STRING, TYPE_FINAL_MESSAGE);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack getInstantPortalItem() {
        ItemStack item = new ItemBuilder(Material.GLOWSTONE)
                .name("<color:#FFFF55>Portal al Spawn</color>")
                .lore("<gray>Genera un portal de Glowstone</gray>", "<gray>que teletransporta al Spawn.</gray>")
                .build();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(ENDING_ITEM_KEY, PersistentDataType.STRING, TYPE_INSTANT_PORTAL);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack getSalvationItem() {
        ItemStack item = new ItemBuilder(Material.TOTEM_OF_UNDYING)
                .name("<color:#55FF55>Salvación</color>")
                .lore("<gray>Purga todas las dificultades</gray>", "<gray>y mecánicas malignas del servidor.</gray>")
                .build();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(ENDING_ITEM_KEY, PersistentDataType.STRING, TYPE_SALVATION);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack getSunlightItem() {
        ItemStack item = new ItemBuilder(Material.SUNFLOWER)
                .name("<color:#FFFF55>La Luz del Sol</color>")
                .lore("<gray>Acelera el tiempo hasta el</gray>", "<gray>amanecer de forma suave.</gray>")
                .build();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(ENDING_ITEM_KEY, PersistentDataType.STRING, TYPE_SUNLIGHT);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String getEndingItemType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(ENDING_ITEM_KEY, PersistentDataType.STRING);
    }
}

package com.panita.tezzlar3.bossfight.commands;

import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import com.panita.tezzlar3.Tezzlar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@CommandSpec(name = "kit", description = "Gives the special kit", playerOnly = true)
public class KitCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (player.hasPermission("group.legend")) {
            Messenger.prefixedSend(player, "<red>Los jugadores con rango Leyenda no pueden reclamar este kit.");
            return;
        }

        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey kitUsedKey = new NamespacedKey(Tezzlar.getInstance(), "kit_used");

        if (pdc.has(kitUsedKey, PersistentDataType.BYTE)) {
            Messenger.prefixedSend(player, "<red>Ya has reclamado tu kit.");
            return;
        }

        List<ItemStack> itemsToGive = new ArrayList<>();

        // superdiamond_helmet
        itemsToGive.add(getCustomEnchanted("superdiamond_helmet", 1, Map.of(
                "minecraft:mending", 1,
                "minecraft:aqua_affinity", 1,
                "minecraft:respiration", 10,
                "minecraft:protection", 5,
                "minecraft:unbreaking", 5
        )));
        // superdiamond_chestplate
        itemsToGive.add(getCustomEnchanted("superdiamond_chestplate", 1, Map.of(
                "minecraft:mending", 1,
                "minecraft:protection", 5,
                "nova_structures:outreach", 4,
                "minecraft:unbreaking", 5
        )));
        // superdiamond_leggings
        itemsToGive.add(getCustomEnchanted("superdiamond_leggings", 1, Map.of(
                "minecraft:mending", 1,
                "minecraft:protection", 5,
                "minecraft:swift_sneak", 5,
                "minecraft:unbreaking", 5
        )));
        // superdiamond_boots
        itemsToGive.add(getCustomEnchanted("superdiamond_boots", 1, Map.of(
                "minecraft:mending", 1,
                "minecraft:feather_falling", 8,
                "minecraft:protection", 5,
                "minecraft:depth_strider", 5,
                "nova_structures:traveler", 3,
                "minecraft:unbreaking", 5
        )));
        // superdiamond_sword
        itemsToGive.add(getCustomEnchanted("superdiamond_sword", 1, Map.of(
                "minecraft:mending", 1,
                "minecraft:bane_of_arthropods", 7,
                "minecraft:sweeping_edge", 5,
                "minecraft:smite", 7,
                "minecraft:sharpness", 10,
                "minecraft:looting", 6,
                "minecraft:unbreaking", 5
        )));
        // superdiamond_bow
        itemsToGive.add(getCustomEnchanted("superdiamond_bow", 1, Map.of(
                "minecraft:punch", 3,
                "minecraft:power", 12,
                "minecraft:flame", 1,
                "minecraft:unbreaking", 5,
                "minecraft:infinity", 1
        )));
        // arrows
        itemsToGive.add(getVanillaItem("minecraft:arrow", 64));
        // superdiamond_shield
        itemsToGive.add(getCustomItem("superdiamond_shield", 1));
        // amethyst_horn
        itemsToGive.add(getCustomItem("amethyst_horn", 1));
        // axolotl_totem x 4
        itemsToGive.add(getCustomItem("axolotl_totem", 4));
        // axolotl_totem x 4
        itemsToGive.add(getCustomItem("axolotl_totem", 4));
        // copper_apple x 67
        itemsToGive.add(getCustomItem("copper_apple", 67));
        // copper_apple x 67
        itemsToGive.add(getCustomItem("copper_apple", 67));
        // copper_apple x 67
        itemsToGive.add(getCustomItem("copper_apple", 67));
        // golden_beetroot x 16
        itemsToGive.add(getCustomItem("golden_beetroot", 16));
        // copper_carrot x 64
        itemsToGive.add(getCustomItem("copper_carrot", 64));
        // soulbound_relic x 2
        itemsToGive.add(getCustomItem("soulbound_relic", 2));
        // repellent x 64
        itemsToGive.add(getCustomItem("repellent", 64));
        // axolotl_totem x 4
        itemsToGive.add(getCustomItem("axolotl_totem", 4));
        // axolotl_totem x 4
        itemsToGive.add(getCustomItem("axolotl_totem", 4));
        
        // vanilla blocks
        itemsToGive.add(getVanillaItem("minecraft:crimson_hyphae", 99));
        itemsToGive.add(getVanillaItem("minecraft:crimson_hyphae", 99));
        itemsToGive.add(getVanillaItem("minecraft:polished_sulfur", 99));
        itemsToGive.add(getVanillaItem("minecraft:polished_sulfur", 99));

        // Add items to inventory and drop the rest if inventory is full
        for (ItemStack item : itemsToGive) {
            if (item != null && item.getType() != Material.AIR) {
                HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item);
                for (ItemStack dropItem : remaining.values()) {
                    player.getWorld().dropItem(player.getLocation(), dropItem);
                }
            }
        }

        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(maxHealthAttr.getBaseValue() + 40.0);
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent settrack ranks reinforcement");

        pdc.set(kitUsedKey, PersistentDataType.BYTE, (byte) 1);
        Messenger.prefixedSend(player, "<green>¡Has recibido tu kit con éxito y tus atributos han sido mejorados!");
    }

    private ItemStack getCustomItem(String id, int amount) {
        ItemStack item = CustomItemManager.getItem(id);
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && amount > item.getMaxStackSize()) {
                meta.setMaxStackSize(amount > 99 ? amount : 99);
                item.setItemMeta(meta);
            }
            item.setAmount(amount);
        }
        return item;
    }

    private ItemStack getCustomEnchanted(String id, int amount, Map<String, Integer> enchants) {
        ItemStack item = getCustomItem(id, amount);
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                    NamespacedKey key = NamespacedKey.fromString(entry.getKey());
                    if (key != null) {
                        Enchantment ench = Registry.ENCHANTMENT.get(key);
                        if (ench != null) {
                            meta.addEnchant(ench, entry.getValue(), true);
                        }
                    }
                }
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    private ItemStack getVanillaItem(String namespaceKey, int amount) {
        NamespacedKey key = NamespacedKey.fromString(namespaceKey);
        if (key != null) {
            Material material = Registry.MATERIAL.get(key);
            if (material != null) {
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null && amount > item.getMaxStackSize()) {
                    meta.setMaxStackSize(amount > 99 ? amount : 99);
                    item.setItemMeta(meta);
                }
                item.setAmount(amount);
                return item;
            }
        }
        return null;
    }
}

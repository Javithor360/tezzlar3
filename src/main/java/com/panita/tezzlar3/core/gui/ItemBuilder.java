package com.panita.tezzlar3.core.gui;

import com.panita.tezzlar3.core.chat.Messenger;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack itemStack) {
        this.item = itemStack.clone();
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        if (meta != null) {
            meta.displayName(Messenger.mini(name));
        }
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder lore(String... lines) {
        if (meta != null) {
            List<Component> lore = new ArrayList<>();
            for (String line : lines) {
                lore.add(Messenger.mini(line));
            }
            meta.lore(lore);
        }
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        if (meta != null) {
            List<Component> lore = new ArrayList<>();
            for (String line : lines) {
                lore.add(Messenger.mini(line));
            }
            meta.lore(lore);
        }
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        if (meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }

    public ItemBuilder skullProfile(PlayerProfile profile) {
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwnerProfile(profile);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}

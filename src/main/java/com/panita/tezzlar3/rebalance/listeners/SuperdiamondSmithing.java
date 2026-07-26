package com.panita.tezzlar3.rebalance.listeners;

import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import com.panita.tezzlar3.Tezzlar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class SuperdiamondSmithing implements Listener {
    private final JavaPlugin plugin;

    public SuperdiamondSmithing(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerRecipes() {
        registerRecipe("superdiamond_helmet_lvl2", "superdiamond_helmet", "superdiamond_ingot");
        registerRecipe("superdiamond_chestplate_lvl2", "superdiamond_chestplate", "superdiamond_ingot");
        registerRecipe("superdiamond_elytra_lvl2", "superdiamond_elytra", "superdiamond_ingot");
        registerRecipe("superdiamond_leggings_lvl2", "superdiamond_leggings", "superdiamond_ingot");
        registerRecipe("superdiamond_boots_lvl2", "superdiamond_boots", "superdiamond_ingot");
        
        // Elytra creation
        registerRecipeWithVanillaAddition("superdiamond_elytra", "superdiamond_chestplate", Material.ELYTRA);
    }

    private void registerRecipe(String resultId, String baseId, String additionId) {
        NamespacedKey key = new NamespacedKey(plugin, "smithing_" + resultId);

        ItemStack templateItem = CustomItemManager.getItem("scroll_upgrade");
        ItemStack baseItem = CustomItemManager.getItem(baseId);
        ItemStack additionItem = CustomItemManager.getItem(additionId);
        ItemStack resultItem = CustomItemManager.getItem(resultId);

        if (templateItem == null || baseItem == null || additionItem == null || resultItem == null) {
            plugin.getLogger().warning("Could not register recipe for " + resultId + " because an item was missing in customitems.json");
            return;
        }

        RecipeChoice templateChoice = new RecipeChoice.MaterialChoice(templateItem.getType());
        RecipeChoice baseChoice = new RecipeChoice.MaterialChoice(baseItem.getType());
        RecipeChoice additionChoice = new RecipeChoice.MaterialChoice(additionItem.getType());

        SmithingTransformRecipe recipe = new SmithingTransformRecipe(key, resultItem, templateChoice, baseChoice, additionChoice);
        // Only add if not already registered (to allow reloads without errors)
        if (plugin.getServer().getRecipe(key) == null) {
            plugin.getServer().addRecipe(recipe);
        }
    }
    
    private void registerRecipeWithVanillaAddition(String resultId, String baseId, org.bukkit.Material vanillaAddition) {
        NamespacedKey key = new NamespacedKey(plugin, "smithing_" + resultId);

        ItemStack templateItem = CustomItemManager.getItem("scroll_upgrade");
        ItemStack baseItem = CustomItemManager.getItem(baseId);
        ItemStack resultItem = CustomItemManager.getItem(resultId);

        if (templateItem == null || baseItem == null || resultItem == null) {
            plugin.getLogger().warning("Could not register recipe for " + resultId + " because an item was missing in customitems.json");
            return;
        }

        RecipeChoice templateChoice = new RecipeChoice.MaterialChoice(templateItem.getType());
        RecipeChoice baseChoice = new RecipeChoice.MaterialChoice(baseItem.getType());
        RecipeChoice additionChoice = new RecipeChoice.MaterialChoice(vanillaAddition);

        SmithingTransformRecipe recipe = new SmithingTransformRecipe(key, resultItem, templateChoice, baseChoice, additionChoice);
        if (plugin.getServer().getRecipe(key) == null) {
            plugin.getServer().addRecipe(recipe);
        }
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        ItemStack nativeResult = event.getResult();
        if (nativeResult == null || nativeResult.getType().isAir()) return;

        // Only enforce strict validation if Bukkit matched one of our custom recipes
        if (!CustomItemManager.isCustomItem(nativeResult)) return;

        ItemStack template = event.getInventory().getItem(0);
        ItemStack base = event.getInventory().getItem(1);
        ItemStack addition = event.getInventory().getItem(2);

        if (template == null || base == null || addition == null) {
            event.setResult(null);
            return;
        }

        // Verify template strictly
        if (!CustomItemManager.isCustomItem(template, "scroll_upgrade")) {
            event.setResult(null);
            return;
        }

        // Check base and addition to map to correct result
        String resultId = null;
        
        if (CustomItemManager.isCustomItem(addition, "superdiamond_ingot")) {
            if (CustomItemManager.isCustomItem(base, "superdiamond_helmet")) resultId = "superdiamond_helmet_lvl2";
            else if (CustomItemManager.isCustomItem(base, "superdiamond_chestplate")) resultId = "superdiamond_chestplate_lvl2";
            else if (CustomItemManager.isCustomItem(base, "superdiamond_elytra")) resultId = "superdiamond_elytra_lvl2";
            else if (CustomItemManager.isCustomItem(base, "superdiamond_leggings")) resultId = "superdiamond_leggings_lvl2";
            else if (CustomItemManager.isCustomItem(base, "superdiamond_boots")) resultId = "superdiamond_boots_lvl2";
        } else if (addition.getType() == org.bukkit.Material.ELYTRA && !CustomItemManager.isCustomItem(addition)) {
            if (CustomItemManager.isCustomItem(base, "superdiamond_chestplate")) {
                resultId = "superdiamond_elytra";
            }
        }

        if (resultId == null) {
            event.setResult(null);
            return;
        }

        ItemStack lvl2Template = CustomItemManager.getItem(resultId);
        
        if (lvl2Template != null) {
            ItemMeta resultMeta = nativeResult.getItemMeta();
            ItemMeta baseMeta = base.getItemMeta();
            ItemMeta templateMeta = lvl2Template.getItemMeta();
            
            if (baseMeta != null && templateMeta != null) {
                ItemStack finalResult = lvl2Template.clone();
                ItemMeta finalMeta = finalResult.getItemMeta();
                
                // 1. Transfer Damage
                if (baseMeta instanceof org.bukkit.inventory.meta.Damageable baseDamageable && finalMeta instanceof org.bukkit.inventory.meta.Damageable finalDamageable) {
                    finalDamageable.setDamage(baseDamageable.getDamage());
                }
                
                // 2. Transfer Enchantments
                for (java.util.Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : baseMeta.getEnchants().entrySet()) {
                    finalMeta.addEnchant(entry.getKey(), entry.getValue(), true);
                }
                
                // 3. Transfer Repair Cost
                if (baseMeta instanceof org.bukkit.inventory.meta.Repairable baseRepairable && finalMeta instanceof org.bukkit.inventory.meta.Repairable finalRepairable) {
                    if (baseRepairable.hasRepairCost()) {
                        finalRepairable.setRepairCost(baseRepairable.getRepairCost());
                    }
                }
                
                // 4. Append all custom NBT tags from the base item (e.g. UberEnchant)
                baseMeta.getPersistentDataContainer().copyTo(finalMeta.getPersistentDataContainer(), false);
                
                // 5. Manually extract custom enchant lore from base and prepend to the pristine template lore
                if (baseMeta.hasLore() && templateMeta.hasLore()) {
                    java.util.List<net.kyori.adventure.text.Component> baseLore = baseMeta.lore();
                    java.util.List<net.kyori.adventure.text.Component> resultLore = templateMeta.lore();
                    
                    java.util.List<net.kyori.adventure.text.Component> customEnchantLore = new java.util.ArrayList<>();
                    for (net.kyori.adventure.text.Component line : baseLore) {
                        String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(line);
                        if (plain.contains("Nivel de Armadura") || plain.contains("→")) {
                            while (!customEnchantLore.isEmpty()) {
                                String lastPlain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(customEnchantLore.getLast());
                                if (lastPlain.trim().isEmpty()) {
                                    customEnchantLore.removeLast();
                                } else {
                                    break;
                                }
                            }
                            break;
                        }
                        customEnchantLore.add(line);
                    }
                    
                    if (!customEnchantLore.isEmpty()) {
                        customEnchantLore.addAll(resultLore);
                        finalMeta.lore(customEnchantLore);
                    }
                }
                
                finalResult.setItemMeta(finalMeta);
                event.setResult(finalResult);
            }
        }
    }
}

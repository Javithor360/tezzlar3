package com.panita.tezzlar3.rebalance.listeners;

import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.Bukkit;
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
        registerRecipe("superdiamond_helmet_lvl2", "superdiamond_helmet");
        registerRecipe("superdiamond_chestplate_lvl2", "superdiamond_chestplate");
        registerRecipe("superdiamond_elytra_lvl2", "superdiamond_elytra");
        registerRecipe("superdiamond_leggings_lvl2", "superdiamond_leggings");
        registerRecipe("superdiamond_boots_lvl2", "superdiamond_boots");
    }

    private void registerRecipe(String resultId, String baseId) {
        NamespacedKey key = new NamespacedKey(plugin, "smithing_" + resultId);

        ItemStack templateItem = CustomItemManager.getItem("scroll_upgrade");
        ItemStack baseItem = CustomItemManager.getItem(baseId);
        ItemStack additionItem = CustomItemManager.getItem("superdiamond_ingot");
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
        if (Bukkit.getRecipe(key) == null) {
            Bukkit.addRecipe(recipe);
        }
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        ItemStack template = event.getInventory().getItem(0);
        ItemStack base = event.getInventory().getItem(1);
        ItemStack addition = event.getInventory().getItem(2);

        if (template == null || base == null || addition == null) return;

        // Verify template
        if (!CustomItemManager.isCustomItem(template, "scroll_upgrade")) return;

        // Verify addition
        if (!CustomItemManager.isCustomItem(addition, "superdiamond_ingot")) return;

        // Check base and map to correct result
        String resultId = null;
        if (CustomItemManager.isCustomItem(base, "superdiamond_helmet")) resultId = "superdiamond_helmet_lvl2";
        else if (CustomItemManager.isCustomItem(base, "superdiamond_chestplate")) resultId = "superdiamond_chestplate_lvl2";
        else if (CustomItemManager.isCustomItem(base, "superdiamond_elytra")) resultId = "superdiamond_elytra_lvl2";
        else if (CustomItemManager.isCustomItem(base, "superdiamond_leggings")) resultId = "superdiamond_leggings_lvl2";
        else if (CustomItemManager.isCustomItem(base, "superdiamond_boots")) resultId = "superdiamond_boots_lvl2";

        if (resultId != null) {
            ItemStack nativeResult = event.getResult();
            if (nativeResult != null && !nativeResult.getType().isAir()) {
                ItemMeta resultMeta = nativeResult.getItemMeta();
                ItemMeta baseMeta = base.getItemMeta();
                
                if (resultMeta != null && baseMeta != null) {
                    // Update custom item ID to Level 2
                    NamespacedKey idKey = new NamespacedKey(Tezzlar.getInstance(), "custom_item_id");
                    resultMeta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, resultId);
                    
                    // Merge lore: Prepend custom enchant lore from base to the native Level 2 result
                    if (baseMeta.hasLore()) {
                        List<Component> baseLore = baseMeta.lore();
                        List<Component> resultLore = resultMeta.lore();
                        if (resultLore == null) resultLore = new ArrayList<>();
                        
                        List<Component> customEnchantLore = new ArrayList<>();
                        
                        for (Component line : baseLore) {
                            String plain = PlainTextComponentSerializer.plainText().serialize(line);
                            if (plain.contains("Nivel de Armadura") || plain.contains("→")) {
                                while (!customEnchantLore.isEmpty()) {
                                    String lastPlain = PlainTextComponentSerializer.plainText().serialize(customEnchantLore.getLast());
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
                            customEnchantLore.add(Component.empty());
                            customEnchantLore.addAll(resultLore);
                            resultMeta.lore(customEnchantLore);
                        }
                    }
                    
                    nativeResult.setItemMeta(resultMeta);
                    event.setResult(nativeResult);
                }
            }
        } else {
            // Cancel any invalid match where the template was our custom scroll
            event.setResult(null);
        }
    }
}

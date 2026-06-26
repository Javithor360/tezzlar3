package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

public class PremiumArmorSetMechanic extends DifficultyMechanic {

    private final NamespacedKey CUSTOM_ID_KEY;

    public PremiumArmorSetMechanic(JavaPlugin plugin) {
        super(plugin, 1); // Available from the start
        
        CUSTOM_ID_KEY = new NamespacedKey(plugin, "custom_item_id");
        
        // Run a check every second (20 ticks) for all players
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (hasFullPremiumSet(player)) {
                    // Strength II (amplifier 1) for 1.5 seconds (30 ticks)
                    // Hidden particles to not obstruct vision (false, false, false)
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 1, false, false, false));
                }
            }
        }, 20L, 20L);
    }

    private boolean hasFullPremiumSet(Player player) {
        EntityEquipment eq = player.getEquipment();
        if (eq == null) return false;
        
        return isPremiumPiece(eq.getHelmet(), "premium_iron_helmet") &&
               isPremiumPiece(eq.getChestplate(), "premium_iron_chestplate") &&
               isPremiumPiece(eq.getLeggings(), "premium_iron_leggings") &&
               isPremiumPiece(eq.getBoots(), "premium_iron_boots");
    }

    private boolean isPremiumPiece(ItemStack item, String expectedId) {
        String customId = ItemUtils.getCustomItemId(item, CUSTOM_ID_KEY);
        return expectedId.equals(customId);
    }
}

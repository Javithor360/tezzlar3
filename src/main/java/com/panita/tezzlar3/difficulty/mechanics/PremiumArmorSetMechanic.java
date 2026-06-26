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

    private final NamespacedKey HELMET_KEY;
    private final NamespacedKey CHESTPLATE_KEY;
    private final NamespacedKey LEGGINGS_KEY;
    private final NamespacedKey BOOTS_KEY;

    public PremiumArmorSetMechanic(JavaPlugin plugin) {
        super(plugin, 1); // Available from the start
        
        HELMET_KEY = new NamespacedKey(plugin, "premium_iron_helmet");
        CHESTPLATE_KEY = new NamespacedKey(plugin, "premium_iron_chestplate");
        LEGGINGS_KEY = new NamespacedKey(plugin, "premium_iron_leggings");
        BOOTS_KEY = new NamespacedKey(plugin, "premium_iron_boots");
        
        // Run a check every second (20 ticks) for all players
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (hasFullPremiumSet(player)) {
                    // Strength II (amplifier 1) for 1.5 seconds (30 ticks)
                    // Hidden particles to not obstruct vision (false, false, true)
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 1, false, false, true));
                }
            }
        }, 20L, 20L);
    }

    private boolean hasFullPremiumSet(Player player) {
        EntityEquipment eq = player.getEquipment();
        if (eq == null) return false;
        
        return isPremiumPiece(eq.getHelmet(), HELMET_KEY) &&
               isPremiumPiece(eq.getChestplate(), CHESTPLATE_KEY) &&
               isPremiumPiece(eq.getLeggings(), LEGGINGS_KEY) &&
               isPremiumPiece(eq.getBoots(), BOOTS_KEY);
    }

    private boolean isPremiumPiece(ItemStack item, NamespacedKey expectedKey) {
        NamespacedKey key = ItemUtils.getItemModel(item);
        return expectedKey.equals(key);
    }
}

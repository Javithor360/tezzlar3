package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class LavaDepthStriderMechanic extends DifficultyMechanic {

    public LavaDepthStriderMechanic(JavaPlugin plugin) {
        super(plugin, 9); // Day 9
        
        // Loop every tick to apply swimming boost in lava
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.isInLava()) {
                    ItemStack boots = p.getInventory().getBoots();
                    if (boots != null && boots.containsEnchantment(Enchantment.DEPTH_STRIDER)) {
                        // Only boost if the player is actively swimming/moving forward
                        if (p.isSwimming()) {
                            int lvl = boots.getEnchantmentLevel(Enchantment.DEPTH_STRIDER);
                            Vector dir = p.getLocation().getDirection().normalize().multiply(0.04 * lvl);
                            p.setVelocity(p.getVelocity().add(dir));
                        }
                    }
                }
            }
        }, 1L, 1L);
    }
}

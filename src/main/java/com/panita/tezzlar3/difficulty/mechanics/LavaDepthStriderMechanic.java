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
                        Vector vel = p.getVelocity();
                        // Verify if the player is trying to move horizontally (velocity > 0.005)
                        if (Math.sqrt(vel.getX() * vel.getX() + vel.getZ() * vel.getZ()) > 0.005) {
                            int lvl = boots.getEnchantments().getOrDefault(Enchantment.DEPTH_STRIDER, 0);
                            // Boost in the direction they are currently moving
                            Vector boost = vel.clone().setY(0).normalize().multiply(0.03 * lvl);
                            p.setVelocity(vel.add(boost));
                        }
                    }
                }
            }
        }, 1L, 1L);
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.util.PlayerUtils;

public class CopperDamageMechanic extends DifficultyMechanic {

    public CopperDamageMechanic(JavaPlugin plugin) {
        super(plugin, 11);
        
        // Check every 10 ticks (0.5 seconds)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead() || player.isFlying() || !PlayerUtils.isSurvival(player)) continue;
                
                Block blockBelow = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
                if (isCopperBlock(blockBelow.getType())) {
                    double currentHealth = player.getHealth();
                    if (currentHealth > 0) {
                        // Subtract health directly to pierce armor
                        player.setHealth(Math.max(0, currentHealth - 2.0)); 
                        // Trigger hurt animation and sound with microscopic damage
                        player.damage(0.00001); 
                    }
                }
            }
        }, 10L, 10L);
    }
    
    private boolean isCopperBlock(Material material) {
        return material.isBlock() && material.name().contains("COPPER");
    }
}

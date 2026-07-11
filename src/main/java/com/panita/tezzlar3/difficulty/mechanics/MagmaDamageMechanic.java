package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.util.PlayerUtils;

public class MagmaDamageMechanic extends DifficultyMechanic {

    public MagmaDamageMechanic(JavaPlugin plugin) {
        super(plugin, 12);
        
        // Execute every 2 ticks (10 times faster than vanilla's 20 ticks of fire/magma damage)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead() || player.isFlying() || !PlayerUtils.isSurvival(player)) continue;
                
                Block blockBelow = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
                
                if (blockBelow.getType() == Material.MAGMA_BLOCK) {
                    if (player.isSneaking()) continue; // Vanilla respects sneaking

                    // Clear no damage ticks to allow rapid natural damage
                    player.setNoDamageTicks(0);
                    
                    // Use natural damage which respects Armor and Fire Protection
                    player.damage(1.0);
                    
                    // Set on fire for 3 minutes (180 seconds = 3600 ticks)
                    player.setFireTicks(3600);
                }
            }
        }, 2L, 2L);
    }
}

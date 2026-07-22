package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.core.util.SoundUtils;

public class CopperDamageMechanic extends DifficultyMechanic {

    public CopperDamageMechanic(JavaPlugin plugin) {
        super(plugin, 11);
        
        // Check every 10 ticks (0.5 seconds)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead() || player.isFlying() || !PlayerUtils.isSurvival(player)) continue;
                
                Block blockBelow = player.getLocation().clone().subtract(0, 0.1, 0).getBlock();
                if (isDamagingBlock(blockBelow.getType())) {
                    double currentHealth = player.getHealth();
                    if (currentHealth > 0) {
                        if (currentHealth <= 2.0) {
                            boolean isSlab = blockBelow.getType().name().contains("SLAB");
                            player.setMetadata(isSlab ? "slab_damage" : "copper_damage", new FixedMetadataValue(plugin, System.currentTimeMillis()));
                            player.setHealth(0.1);
                            player.damage(10.0);
                        } else {
                            // Subtract health directly to pierce armor
                            player.setHealth(currentHealth - 2.0); 
                            // Trigger hurt animation and sound with microscopic damage
                            player.damage(0.00001); 
                        }
                        
                        SoundUtils.playInRadius(player.getLocation(), "entity.generic.burn", 10.0f, 1.0f);
                    }
                }
            }
        }, 10L, 10L);
    }
    
    private boolean isDamagingBlock(Material material) {
        if (!material.isBlock()) return false;
        String name = material.name();
        return name.contains("COPPER") || name.contains("SLAB");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!isActive()) return;
        Player player = event.getEntity();
        
        if (player.hasMetadata("copper_damage")) {
            long time = player.getMetadata("copper_damage").get(0).asLong();
            if (System.currentTimeMillis() - time <= 100) {
                event.setDeathMessage(player.getName() + " murió por intoxicación de cobre.");
            }
            player.removeMetadata("copper_damage", plugin);
        } else if (player.hasMetadata("slab_damage")) {
            long time = player.getMetadata("slab_damage").get(0).asLong();
            if (System.currentTimeMillis() - time <= 100) {
                event.setDeathMessage(player.getName() + " murió por intoxicación de slab.");
            }
            player.removeMetadata("slab_damage", plugin);
        }
    }
}

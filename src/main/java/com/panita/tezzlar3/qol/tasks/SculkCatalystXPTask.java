package com.panita.tezzlar3.qol.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SculkCatalystXPTask extends BukkitRunnable {

    private final Map<UUID, Double> pendingXp = new HashMap<>();

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLevel() >= 100) continue;
            
            // Check if player is standing on a SCULK_CATALYST
            Block standingOn = player.getLocation().clone().subtract(0, 0.1, 0).getBlock();
            if (standingOn.getType() != Material.SCULK_CATALYST) {
                continue;
            }
            
            int level = player.getLevel();
            int targetSeconds = getTargetSecondsForLevel(level);
            if (targetSeconds <= 0) continue;
            
            int expToNext = player.getExpToLevel();
            
            // We run every 10 ticks (0.5 seconds), meaning we need (TargetSeconds / 0.5) executions to complete it.
            double executionsNeeded = targetSeconds * 2.0;
            double xpPerTick = expToNext / executionsNeeded;
            
            UUID uuid = player.getUniqueId();
            double accumulated = pendingXp.getOrDefault(uuid, 0.0) + xpPerTick;
            
            int xpToGive = (int) accumulated;
            if (xpToGive > 0) {
                player.giveExp(xpToGive);
                accumulated -= xpToGive;
                
                // Visual feedback that the block is feeding them XP
                if (Math.random() < 0.25) { 
                    player.getWorld().spawnParticle(Particle.SCULK_SOUL, player.getLocation().add(0, 0.1, 0), 2, 0.3, 0.1, 0.3, 0.02);
                }
            }
            
            if (accumulated > 0) {
                pendingXp.put(uuid, accumulated);
            } else {
                pendingXp.remove(uuid);
            }
        }
    }
    
    private int getTargetSecondsForLevel(int level) {
        if (level < 10) return 5;
        if (level < 20) return 10;
        if (level < 30) return 20;
        if (level < 40) return 60;
        if (level < 50) return 120;
        if (level < 60) return 180;
        if (level < 70) return 210;
        if (level < 80) return 240;
        if (level < 90) return 270;
        if (level < 100) return 300;
        return 0; 
    }
}

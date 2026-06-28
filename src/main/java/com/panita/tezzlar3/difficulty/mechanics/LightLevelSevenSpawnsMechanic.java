package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.util.EntityUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LightLevelSevenSpawnsMechanic extends DifficultyMechanic {

    private final Random random = new Random();
    private final EntityType[] spawnPool = {
        EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER, EntityType.SLIME
    };

    public LightLevelSevenSpawnsMechanic(JavaPlugin plugin) {
        super(plugin, 18);
        
        // Run every 5 seconds
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                handlePlayerSpawns(player);
            }
        }, 100L, 100L);
    }

    private void handlePlayerSpawns(Player player) {
        World world = player.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) return; // Only apply in overworld
        
        // Find a valid spot near the player (between 16 and 32 blocks away, light <= 7)
        Location spawnLoc = findValidSpawnLocation(player);
        if (spawnLoc == null) return;
        
        // Find a far monster to replace to avoid exceeding mobcap
        Monster farMonster = findReplaceableMonster(player);
        if (farMonster != null) {
            farMonster.remove();
            
            // Pick a random mob type
            EntityType type = spawnPool[random.nextInt(spawnPool.length)];
            
            // Spawn it!
            world.spawnEntity(spawnLoc, type);
        }
    }

    private Monster findReplaceableMonster(Player player) {
        // Look for monsters between 40 and 64 blocks away that are NOT custom mobs
        List<Monster> candidates = new ArrayList<>();
        
        for (Entity entity : player.getNearbyEntities(64, 64, 64)) {
            if (entity instanceof Monster monster) {
                // Must be at least 40 blocks away
                if (monster.getLocation().distanceSquared(player.getLocation()) > 1600) { // 40^2 = 1600
                    if (!EntityUtils.isCustomMob(monster)) {
                        candidates.add(monster);
                    }
                }
            }
        }
        
        if (candidates.isEmpty()) return null;
        
        Collections.shuffle(candidates);
        return candidates.get(0);
    }

    private Location findValidSpawnLocation(Player player) {
        Location pLoc = player.getLocation();
        World world = pLoc.getWorld();
        
        // Try up to 10 random locations
        for (int i = 0; i < 10; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 16 + random.nextDouble() * 16; // 16 to 32 blocks
            
            int x = pLoc.getBlockX() + (int) (Math.cos(angle) * distance);
            int z = pLoc.getBlockZ() + (int) (Math.sin(angle) * distance);
            
            // Find highest solid block around Y level of player
            int startY = Math.min(world.getMaxHeight() - 1, pLoc.getBlockY() + 15);
            int minY = Math.max(world.getMinHeight(), pLoc.getBlockY() - 15);
            
            for (int y = startY; y >= minY; y--) {
                Block block = world.getBlockAt(x, y, z);
                Block above1 = block.getRelative(0, 1, 0);
                Block above2 = block.getRelative(0, 2, 0);
                
                // Solid ground, air above
                if (block.getType().isSolid() && above1.getType().isAir() && above2.getType().isAir()) {
                    // Check light level
                    if (above1.getLightLevel() <= 7) {
                        return above1.getLocation().add(0.5, 0.0, 0.5);
                    }
                }
            }
        }
        return null;
    }
}

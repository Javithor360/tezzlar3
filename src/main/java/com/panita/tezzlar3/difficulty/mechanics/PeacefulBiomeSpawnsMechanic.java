package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PeacefulBiomeSpawnsMechanic extends DifficultyMechanic {
    private final Random random = new Random();
    private final List<EntityType> COMMON_MOBS = Arrays.asList(
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, 
            EntityType.SPIDER, EntityType.ENDERMAN, EntityType.WITCH, EntityType.SLIME
    );

    public PeacefulBiomeSpawnsMechanic(JavaPlugin plugin) {
        super(plugin, 10); // Day 10
        
        // Run every 20 seconds
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead() || !player.isValid()) continue;
                
                Location loc = player.getLocation();
                Biome biome = loc.getWorld().getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                
                // Deep Dark and Mushroom Fields are usually peaceful
                if (biome == Biome.DEEP_DARK || biome == Biome.MUSHROOM_FIELDS) {
                    
                    // Attempt to spawn a few mobs around the player
                    int mobsToSpawn = 1 + random.nextInt(3); // 1 to 3 mobs
                    
                    for (int i = 0; i < mobsToSpawn; i++) {
                        // Find a random location in a 20 block radius
                        double offsetX = (random.nextDouble() * 40) - 20;
                        double offsetZ = (random.nextDouble() * 40) - 20;
                        
                        int x = (int) (loc.getX() + offsetX);
                        int z = (int) (loc.getZ() + offsetZ);
                        
                        // We will check Y from player Y - 10 to player Y + 10
                        int startY = Math.max(loc.getWorld().getMinHeight(), loc.getBlockY() - 10);
                        int endY = Math.min(loc.getWorld().getMaxHeight(), loc.getBlockY() + 10);
                        
                        Location spawnLoc = findValidSpawnLocation(loc.getWorld(), x, z, startY, endY);
                        if (spawnLoc != null) {
                            // Enforce light level 0 for block light, and for sky light it must be <= 7 (night)
                            if (spawnLoc.getBlock().getLightFromBlocks() == 0 && spawnLoc.getBlock().getLightFromSky() <= 7) {
                                EntityType type = COMMON_MOBS.get(random.nextInt(COMMON_MOBS.size()));
                                // Spawn using NATURAL reason so it passes through other mechanics
                                loc.getWorld().spawnEntity(spawnLoc, type, CreatureSpawnEvent.SpawnReason.NATURAL);
                            }
                        }
                    }
                }
            }
        }, 400L, 400L); // 400 ticks = 20 seconds
    }
    
    private Location findValidSpawnLocation(World world, int x, int z, int startY, int endY) {
        for (int y = endY; y >= startY; y--) {
            Block block = world.getBlockAt(x, y, z);
            Block above1 = world.getBlockAt(x, y + 1, z);
            Block above2 = world.getBlockAt(x, y + 2, z);
            
            // Check if the block is solid and there is a 2-block high air space above it
            if (block.isSolid() && block.getType() != Material.BEDROCK && block.getType() != Material.BARRIER) {
                if (above1.getType().isAir() && above2.getType().isAir()) {
                    return above1.getLocation().add(0.5, 0, 0.5);
                }
            }
        }
        return null;
    }
}

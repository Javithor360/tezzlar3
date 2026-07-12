package com.panita.tezzlar3.difficulty.tasks;

import com.panita.tezzlar3.difficulty.util.RegeneratingSpawner;
import com.panita.tezzlar3.difficulty.util.SpawnerRegenManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

public class SpawnerRegenTask extends BukkitRunnable {
    private final SpawnerRegenManager manager;
    private final long tickIntervalMillis;

    public SpawnerRegenTask(SpawnerRegenManager manager, long tickIntervalTicks) {
        this.manager = manager;
        this.tickIntervalMillis = tickIntervalTicks * 50L;
    }

    @Override
    public void run() {
        Iterator<RegeneratingSpawner> iterator = manager.getActiveSpawners().iterator();
        while (iterator.hasNext()) {
            RegeneratingSpawner spawner = iterator.next();
            
            // Generate SOUL particles if chunk is loaded
            Location loc = spawner.getLocation();
            if (loc.getWorld() != null && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                loc.getWorld().spawnParticle(Particle.SOUL, loc.clone().add(0.5, 0.5, 0.5), 3, 0.3, 0.3, 0.3, 0.05);
            }

            spawner.setTimeRemainingMillis(spawner.getTimeRemainingMillis() - tickIntervalMillis);

            if (spawner.getTimeRemainingMillis() <= 0) {
                // Restore the spawner
                restoreSpawner(spawner);
                iterator.remove();
            }
        }
    }

    private void restoreSpawner(RegeneratingSpawner spawnerInfo) {
        Location loc = spawnerInfo.getLocation();
        if (loc.getWorld() == null) return;
        
        // This automatically loads the chunk if it's not loaded
        Block block = loc.getBlock();
        block.setType(Material.SPAWNER, false);
        
        if (block.getState() instanceof CreatureSpawner worldSpawner) {
            if (spawnerInfo.getSpawnerItem().getItemMeta() instanceof BlockStateMeta meta) {
                if (meta.getBlockState() instanceof CreatureSpawner storedSpawner) {
                    
                    // Copy basic Bukkit properties
                    worldSpawner.setSpawnedType(storedSpawner.getSpawnedType());
                    worldSpawner.setDelay(storedSpawner.getDelay());
                    worldSpawner.setMinSpawnDelay(storedSpawner.getMinSpawnDelay());
                    worldSpawner.setMaxSpawnDelay(storedSpawner.getMaxSpawnDelay());
                    worldSpawner.setSpawnCount(storedSpawner.getSpawnCount());
                    worldSpawner.setMaxNearbyEntities(storedSpawner.getMaxNearbyEntities());
                    worldSpawner.setRequiredPlayerRange(storedSpawner.getRequiredPlayerRange());
                    worldSpawner.setSpawnRange(storedSpawner.getSpawnRange());

                    // Copy Paper's EntitySnapshot via reflection to maintain NBT, equipment, and custom mob data
                    try {
                        java.lang.reflect.Method getEntityMethod = storedSpawner.getClass().getMethod("getSpawnedEntity");
                        Object snapshot = getEntityMethod.invoke(storedSpawner);
                        
                        if (snapshot != null) {
                            java.lang.reflect.Method setEntityMethod = worldSpawner.getClass().getMethod("setSpawnedEntity", Class.forName("org.bukkit.entity.EntitySnapshot"));
                            setEntityMethod.invoke(worldSpawner, snapshot);
                        }
                    } catch (Exception ignored) {
                        // Not running on a Paper version that supports EntitySnapshot, fallback to standard behavior
                    }
                }
            }
            worldSpawner.update(true, false);
        }
    }
}

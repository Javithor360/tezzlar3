package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Phantom;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhantomAnchorTurretMechanic extends DifficultyMechanic {
    private final Map<Location, Long> anchorCooldowns = new HashMap<>();

    public PhantomAnchorTurretMechanic(JavaPlugin plugin) {
        super(plugin, 15);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;

            long now = System.currentTimeMillis();
            Map<Phantom, Location> phantomLocs = new HashMap<>();
            Set<Chunk> chunksToSnapshot = new HashSet<>();

            for (World world : Bukkit.getWorlds()) {
                if (world.getEnvironment() != World.Environment.NORMAL) continue;
                
                for (Phantom phantom : world.getEntitiesByClass(Phantom.class)) {
                    if (phantom.isDead() || !phantom.isValid()) continue;

                    Location loc = phantom.getLocation();
                    phantomLocs.put(phantom, loc);

                    int px = loc.getBlockX();
                    int pz = loc.getBlockZ();

                    for (int cx = (px - 15) >> 4; cx <= (px + 15) >> 4; cx++) {
                        for (int cz = (pz - 15) >> 4; cz <= (pz + 15) >> 4; cz++) {
                            if (world.isChunkLoaded(cx, cz)) {
                                chunksToSnapshot.add(world.getChunkAt(cx, cz));
                            }
                        }
                    }
                }
            }

            if (phantomLocs.isEmpty()) return;

            List<ChunkSnapshot> snapshots = new ArrayList<>();
            for (Chunk c : chunksToSnapshot) {
                snapshots.add(c.getChunkSnapshot());
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Set<Location> foundAnchors = new HashSet<>();

                for (Map.Entry<Phantom, Location> entry : phantomLocs.entrySet()) {
                    Location ploc = entry.getValue();
                    int px = ploc.getBlockX();
                    int pz = ploc.getBlockZ();
                    int py = ploc.getBlockY();
                    World world = ploc.getWorld();

                    int minX = px - 15;
                    int maxX = px + 15;
                    int minZ = pz - 15;
                    int maxZ = pz + 15;

                    for (ChunkSnapshot snap : snapshots) {
                        if (!snap.getWorldName().equals(world.getName())) continue;

                        int cx = snap.getX() << 4;
                        int cz = snap.getZ() << 4;

                        int startX = Math.max(cx, minX);
                        int endX = Math.min(cx + 15, maxX);
                        int startZ = Math.max(cz, minZ);
                        int endZ = Math.min(cz + 15, maxZ);

                        for (int x = startX; x <= endX; x++) {
                            for (int z = startZ; z <= endZ; z++) {
                                int localX = x & 15;
                                int localZ = z & 15;

                                // Optimize: Scan only from phantom Y downwards
                                for (int y = py; y >= world.getMinHeight(); y--) {
                                    if (snap.getBlockType(localX, y, localZ) == Material.RESPAWN_ANCHOR) {
                                        BlockData data = snap.getBlockData(localX, y, localZ);
                                        if (data instanceof RespawnAnchor) {
                                            RespawnAnchor anchorData = (RespawnAnchor) data;
                                            if (anchorData.getCharges() > 0) { // Can shoot with at least 1 charge
                                                foundAnchors.add(new Location(world, x, y, z));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (foundAnchors.isEmpty()) return;

                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (Location anchorLoc : foundAnchors) {
                        long nextAttack = anchorCooldowns.getOrDefault(anchorLoc, 0L);
                        if (now >= nextAttack) {
                            boolean fired = fireAnchor(anchorLoc);
                            if (fired) {
                                long delay = 5000 + (long) (Math.random() * 15000);
                                anchorCooldowns.put(anchorLoc, now + delay);
                            }
                        }
                    }

                    // Clean up old cooldowns for anchors that no longer have phantoms above them
                    anchorCooldowns.keySet().removeIf(loc -> !foundAnchors.contains(loc) && (now - anchorCooldowns.get(loc) > 30000));
                });
            });
        }, 20L, 20L);
    }

    private boolean fireAnchor(Location anchorLoc) {
        Block block = anchorLoc.getBlock();
        if (block.getType() != Material.RESPAWN_ANCHOR) return false;

        BlockData data = block.getBlockData();
        if (!(data instanceof RespawnAnchor)) return false;
        
        RespawnAnchor anchorData = (RespawnAnchor) data;
        if (anchorData.getCharges() == 0) return false;

        List<Phantom> targets = new ArrayList<>();
        for (Phantom p : anchorLoc.getWorld().getEntitiesByClass(Phantom.class)) {
            if (p.isDead() || !p.isValid()) continue;
            if (p.getLocation().getBlockY() < anchorLoc.getBlockY()) continue; // Only Phantoms ABOVE the anchor

            double dx = p.getLocation().getX() - anchorLoc.getX();
            double dz = p.getLocation().getZ() - anchorLoc.getZ();
            if (dx * dx + dz * dz <= 225) { // 15 block radius
                targets.add(p);
            }
        }

        if (targets.isEmpty()) return false;

        Collections.shuffle(targets);
        int shotCount = 0;

        for (Phantom target : targets) {
            if (shotCount >= 3) break; // Max 3 phantoms at once

            double damage = 10.0 + (Math.random() * 15.0);
            target.damage(damage);

            drawEpicLaser(anchorLoc, target);
            shotCount++;
        }

        // 1 in 50 chance to consume a charge
        if (Math.random() < 0.02) {
            int newCharges = anchorData.getCharges() - 1;
            anchorData.setCharges(newCharges);
            block.setBlockData(anchorData);

            if (newCharges == 0) {
                block.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, anchorLoc.clone().add(0.5, 1.2, 0.5), 30, 0.5, 0.5, 0.5, 0.05);
                SoundUtils.playInRadius(anchorLoc, "block.lava.extinguish", 1.0f, 1.0f);
            }
        }

        return true;
    }

    private void drawEpicLaser(Location anchorLoc, Phantom target) {
        Location start = anchorLoc.clone().add(0.5, 1.0, 0.5);
        Location end = target.getLocation().add(0, target.getHeight() / 2.0, 0);

        Vector dir = end.toVector().subtract(start.toVector());
        double distance = dir.length();

        if (distance > 0) {
            dir.normalize();
            for (double d = 0; d <= distance; d += 0.5) {
                Location point = start.clone().add(dir.clone().multiply(d));
                
                // Core of the laser (white/bright)
                start.getWorld().spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);
                
                // Outer aura of the laser (purple magic)
                start.getWorld().spawnParticle(Particle.PORTAL, point, 3, 0.2, 0.2, 0.2, 0.05);
                
                // Occasional electric sparks
                if (Math.random() < 0.2) {
                    start.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, point, 1, 0.1, 0.1, 0.1, 0);
                }
            }
            
            // Impact explosion
            start.getWorld().spawnParticle(Particle.SONIC_BOOM, end, 1, 0, 0, 0, 0);
            start.getWorld().spawnParticle(Particle.REVERSE_PORTAL, end, 30, 1.0, 1.0, 1.0, 0.1);
        }

        SoundUtils.playInRadius(start, "block.respawn_anchor.deplete", 1.0f, 2.0f);
        SoundUtils.playInRadius(start, "entity.wither.shoot", 0.5f, 1.5f);
        SoundUtils.playInRadius(end, "entity.phantom.hurt", 1.0f, 0.8f);
    }
}

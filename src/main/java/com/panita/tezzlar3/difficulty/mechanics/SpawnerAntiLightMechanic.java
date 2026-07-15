package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.core.util.SoundUtils;

import java.util.ArrayList;
import java.util.List;

public class SpawnerAntiLightMechanic extends DifficultyMechanic {
    private static final int PLAYER_SCAN_RADIUS = 16; // Spawner activation range is 16 blocks
    private static final int LIGHT_CLEAR_RADIUS = 12; // Radius around the spawner to clear lights

    public SpawnerAntiLightMechanic(JavaPlugin plugin) {
        super(plugin, 15); // Active from day 15

        // Run every 100 ticks (5 seconds)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;

            List<Player> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.isDead() && PlayerUtils.isSurvival(p)) {
                    players.add(p);
                }
            }

            if (players.isEmpty()) return;

            // Staggered execution: process one player per tick to prevent server lag
            new BukkitRunnable() {
                private int index = 0;

                @Override
                public void run() {
                    if (index >= players.size()) {
                        this.cancel();
                        return;
                    }

                    Player player = players.get(index++);
                    if (!player.isOnline() || player.isDead()) return;

                    Block center = player.getLocation().getBlock();
                    
                    // 1. Find spawners near the player
                    List<Block> nearbySpawners = new ArrayList<>();
                    for (int x = -PLAYER_SCAN_RADIUS; x <= PLAYER_SCAN_RADIUS; x++) {
                        for (int y = -PLAYER_SCAN_RADIUS; y <= PLAYER_SCAN_RADIUS; y++) {
                            for (int z = -PLAYER_SCAN_RADIUS; z <= PLAYER_SCAN_RADIUS; z++) {
                                Block b = center.getRelative(x, y, z);
                                if (b.getType() == Material.SPAWNER) {
                                    nearbySpawners.add(b);
                                }
                            }
                        }
                    }

                    // 2. For each spawner found, clear light blocks around it
                    boolean removedLight = false;
                    for (Block spawner : nearbySpawners) {
                        for (int x = -LIGHT_CLEAR_RADIUS; x <= LIGHT_CLEAR_RADIUS; x++) {
                            for (int y = -LIGHT_CLEAR_RADIUS; y <= LIGHT_CLEAR_RADIUS; y++) {
                                for (int z = -LIGHT_CLEAR_RADIUS; z <= LIGHT_CLEAR_RADIUS; z++) {
                                    Block b = spawner.getRelative(x, y, z);
                                    if (isLightBlock(b.getType())) {
                                        b.setType(Material.AIR);
                                        removedLight = true;
                                    }
                                }
                            }
                        }
                    }

                    if (removedLight) {
                        SoundUtils.playInRadius(player.getLocation(), "block.fire.extinguish", 1.0f, 1.0f);
                    }
                }
            }.runTaskTimer(plugin, 1L, 1L);

        }, 100L, 100L);
    }

    private boolean isLightBlock(Material mat) {
        if (!mat.isBlock()) return false;
        String name = mat.name();
        
        // We avoid blocks like MAGMA_BLOCK to prevent destroying massive amounts of nether terrain.
        return name.contains("TORCH")
            || name.contains("CAMPFIRE")
            || name.contains("LANTERN")
            || name.contains("FROGLIGHT")
            || name.contains("CANDLE")
            || name.contains("GLOWSTONE")
            || name.contains("SHROOMLIGHT")
            || name.contains("GLOW_LICHEN")
            || name.contains("REDSTONE_LAMP")
            || name.contains("END_ROD")
            || name.contains("BULB")
            || name.contains("BEACON")
            || name.contains("CONDUIT")
            || name.contains("PICKLE")
            || name.contains("REDSTONE_BLOCK")
            || name.contains("ANCHOR")
            || name.contains("OBSIDIAN")
            || mat == Material.GLOW_BERRIES
            || mat == Material.LIGHT
            || mat == Material.FIRE
            || mat == Material.SOUL_FIRE;
    }
}

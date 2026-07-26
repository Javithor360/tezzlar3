package com.panita.tezzlar3.bossfight.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.bossfight.util.EndingItems;
import com.panita.tezzlar3.difficulty.DifficultyModule;
import com.panita.tezzlar3.core.chat.Messenger;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.time.Duration;

public class EndingListener implements Listener {

    private boolean isTimeLapseRunning = false;

    @EventHandler
    public void onEndingItemUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        String type = EndingItems.getEndingItemType(item);
        if (type == null) return;
        
        event.setCancelled(true);
        
        switch (type) {
            case EndingItems.TYPE_FINAL_MESSAGE:
                playFinalMessageSequence();
                break;
            case EndingItems.TYPE_INSTANT_PORTAL:
                if (event.getClickedBlock() != null) {
                    buildInstantPortal(event.getClickedBlock().getLocation().add(0, 1, 0), player.getFacing());
                }
                break;
            case EndingItems.TYPE_SALVATION:
                triggerSalvation();
                break;
            case EndingItems.TYPE_SUNLIGHT:
                startSunlightTimeLapse(player.getWorld());
                break;
        }
    }

    @EventHandler
    public void onPlayerEnterGlowstonePortal(PlayerPortalEvent event) {
        Location from = event.getFrom();
        if (from.getBlock().getType() == Material.NETHER_PORTAL) {
            // Check if surrounded by glowstone (a quick check of the frame)
            boolean hasGlowstone = false;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 4; dy++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        if (from.clone().add(dx, dy, dz).getBlock().getType() == Material.GLOWSTONE) {
                            hasGlowstone = true;
                            break;
                        }
                    }
                }
            }
            
            if (hasGlowstone) {
                event.setCancelled(true);
                Bukkit.getScheduler().runTask(Tezzlar.getInstance(), () -> {
                    Bukkit.dispatchCommand(event.getPlayer(), "tz coordinates teleport Spawn");
                });
            }
        }
    }

    private void playFinalMessageSequence() {
        String mainTitle = "<b><gradient:#F12F2F:#EC9550:#F12F2F><shadow:#5D3108:1>El Final de una Historia</shadow></gradient></b>";
        
        String[] subtitles = {
            "<white>Luego de un mes lleno de intensidad...</white>",
            "<white>ya es momento de un merecido descanso...</white>",
            "<white>dicen que todo lo bueno se acaba...</white>",
            "<white>pero también sabemos que de lo malo siempre...</white>",
            "<white>podemos sacar algo bueno.</white>", // Delay 10s after this
            "<white>Muchas gracias a todos ustedes...</white>",
            "<white>por afrontar el reto de la maldición...</white>",
            "<white>y salir victoriosos una vez más.</white>", // Delay 10s after this
            "<white>Gracias por librar a Tezzlar III de su condena...</white>",
            "<white>y por la paciencia que tuvieron...</white>",
            "<white>para llegar hasta el final.</white>", // Delay 10s after this
            "<aqua>Ha llegado a su fin...</aqua>",
            "<aqua>¡Gracias a todos por jugar!</aqua>"
        };
        
        // 0-4: 5s each. 4 to 5: 10s gap
        // 5-7: 5s each. 7 to 8: 10s gap
        // 8-10: 5s each. 10 to 11: 10s gap
        // 11 to 12: 5s gap
        long[] delays = {0, 100, 200, 300, 400, 600, 700, 800, 1000, 1100, 1200, 1400, 1500};
        
        for (int i = 0; i < subtitles.length; i++) {
            final int index = i;
            final String sub = subtitles[i];
            
            Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                String title = mainTitle;
                if (index >= 11) {
                    title = "<b><gradient:#5FE2C5:#C6DEF1:#5FE2C5><shadow:#0D1E40:1>TEZZLAR III</shadow></gradient></b>";
                }
                
                long fadeIn = (index == 0 || index == 11) ? 1000 : 0;
                long fadeOut = (index == 4 || index == 7 || index == 10 || index == 12) ? 1000 : 0;
                long stayMillis = 5000 - fadeIn - fadeOut;
                if (stayMillis < 0) stayMillis = 0;
                
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Messenger.showTitle(
                            p, 
                            title, 
                            sub, 
                            Duration.ofMillis(fadeIn), 
                            Duration.ofMillis(stayMillis), 
                            Duration.ofMillis(fadeOut)
                    );
                    
                    if (index == 11) {
                        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                        p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 0.8f);
                        p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.0f);
                    } else if (index != 12) {
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
                    }
                }
            }, delays[i]);
        }
    }

    private void buildInstantPortal(Location center, BlockFace playerFacing) {
        // Player faces NORTH (Z-), SOUTH (Z+). Portal should be placed along X axis (EAST-WEST)
        // Player faces EAST (X+), WEST (X-). Portal should be placed along Z axis (NORTH-SOUTH)
        
        boolean alongX = (playerFacing == BlockFace.NORTH || playerFacing == BlockFace.SOUTH);
        Axis axis = alongX ? Axis.X : Axis.Z;
        
        World w = center.getWorld();
        
        // Generate Frame (4 wide, 5 tall)
        // Bottom and Top
        for (int i = -1; i <= 2; i++) {
            Block bottom = alongX ? w.getBlockAt(center.getBlockX() + i, center.getBlockY(), center.getBlockZ()) 
                                  : w.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ() + i);
            bottom.setType(Material.GLOWSTONE);
            
            Block top = alongX ? w.getBlockAt(center.getBlockX() + i, center.getBlockY() + 4, center.getBlockZ()) 
                               : w.getBlockAt(center.getBlockX(), center.getBlockY() + 4, center.getBlockZ() + i);
            top.setType(Material.GLOWSTONE);
        }
        
        // Left and Right pillars
        for (int y = 1; y <= 3; y++) {
            Block left = alongX ? w.getBlockAt(center.getBlockX() - 1, center.getBlockY() + y, center.getBlockZ()) 
                                : w.getBlockAt(center.getBlockX(), center.getBlockY() + y, center.getBlockZ() - 1);
            left.setType(Material.GLOWSTONE);
            
            Block right = alongX ? w.getBlockAt(center.getBlockX() + 2, center.getBlockY() + y, center.getBlockZ()) 
                                 : w.getBlockAt(center.getBlockX(), center.getBlockY() + y, center.getBlockZ() + 2);
            right.setType(Material.GLOWSTONE);
        }
        
        // Fill inside with Portal Blocks
        for (int i = 0; i <= 1; i++) {
            for (int y = 1; y <= 3; y++) {
                Block inside = alongX ? w.getBlockAt(center.getBlockX() + i, center.getBlockY() + y, center.getBlockZ()) 
                                      : w.getBlockAt(center.getBlockX(), center.getBlockY() + y, center.getBlockZ() + i);
                inside.setType(Material.NETHER_PORTAL);
                if (inside.getBlockData() instanceof Orientable orientable) {
                    orientable.setAxis(axis);
                    inside.setBlockData(orientable);
                }
            }
        }
        
        // Spawn particles around the frame
        new BukkitRunnable() {
            @Override
            public void run() {
                // If the bottom center block is no longer glowstone, assume portal is destroyed
                Block checkBlock = w.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ());
                if (checkBlock.getType() != Material.GLOWSTONE && checkBlock.getType() != Material.NETHER_PORTAL) {
                    this.cancel();
                    return;
                }
                
                double minX = alongX ? center.getX() - 1 : center.getX();
                double maxX = alongX ? center.getX() + 3 : center.getX() + 1;
                double minZ = alongX ? center.getZ() : center.getZ() - 1;
                double maxZ = alongX ? center.getZ() + 1 : center.getZ() + 3;
                double minY = center.getY();
                double maxY = center.getY() + 5;
                
                for (int i = 0; i < 25; i++) {
                    double x = minX + (maxX - minX) * Math.random();
                    double y = minY + (maxY - minY) * Math.random();
                    double z = minZ + (maxZ - minZ) * Math.random();
                    
                    w.spawnParticle(Particle.END_ROD, x, y, z, 2, 0.1, 0.1, 0.1, 0.02);
                    w.spawnParticle(Particle.TOTEM_OF_UNDYING, x, y, z, 3, 0.3, 0.3, 0.3, 0.1);
                    
                    if (Math.random() < 0.4) {
                        w.spawnParticle(Particle.GLOW, x, y, z, 2, 0.2, 0.2, 0.2, 0.05);
                    }
                }
            }
        }.runTaskTimer(Tezzlar.getInstance(), 0L, 5L);
    }

    private void triggerSalvation() {
        DifficultyModule difficultyModule = (DifficultyModule) Tezzlar.getModuleManager().getModule("difficulty");
        if (difficultyModule != null && difficultyModule.isEnabled()) {
            difficultyModule.onDisable(Tezzlar.getInstance());
            Tezzlar.getConfigManager().updateBoolean("difficulty.enabled", false, null);
            for (Player p : Bukkit.getOnlinePlayers()) {
                Messenger.prefixedSend(p, "<green>¡El mal ha sido erradicado! Las mecánicas de dificultad han sido desactivadas.</green>");
                p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
            }
        }
    }

    private void startSunlightTimeLapse(World world) {
        if (isTimeLapseRunning) return;
        isTimeLapseRunning = true;
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                long time = world.getTime();
                
                if (time >= 23500 || (time >= 0 && time < 12000)) {
                    isTimeLapseRunning = false;
                    this.cancel();
                    return;
                }
                
                if (ticks % 2 == 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 0.4f, 1.0f);
                    }
                }
                
                world.setTime(time + 100);
                ticks++;
            }
        }.runTaskTimer(Tezzlar.getInstance(), 0L, 1L);
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class GigaMagmaCubeMechanic extends DifficultyMechanic {
    private final Random random = new Random();
    public static NamespacedKey BOSS_KEY;
    public static NamespacedKey BLAZE_KEY;
    public static NamespacedKey MINION_KEY;
    
    // Map to keep track of active bosses
    private static final Map<UUID, GigaMagmaCubeBoss> activeBosses = new HashMap<>();

    public static void removeActiveBoss(UUID uuid) {
        activeBosses.remove(uuid);
    }

    public GigaMagmaCubeMechanic(JavaPlugin plugin) {
        super(plugin, 10); // Day 10
        BOSS_KEY = new NamespacedKey(plugin, "giga_magma_cube");
        BLAZE_KEY = new NamespacedKey(plugin, "giga_blaze");
        MINION_KEY = new NamespacedKey(plugin, "giga_minion");
        CustomMobManager.register(CustomMobType.GIGA_MAGMA_CUBE, this::spawnManual);
        
        // Scan for existing bosses that were loaded when the plugin started or chunk loaded
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            for (World world : Bukkit.getWorlds()) {
                if (world.getEnvironment() != World.Environment.NETHER) continue;
                for (Entity entity : world.getEntitiesByClass(MagmaCube.class)) {
                    if (entity.getPersistentDataContainer().has(BOSS_KEY, PersistentDataType.BYTE)) {
                        if (!activeBosses.containsKey(entity.getUniqueId())) {
                            activeBosses.put(entity.getUniqueId(), new GigaMagmaCubeBoss((MagmaCube) entity, plugin, false));
                        }
                    }
                }
            }
        }, 100L, 100L);
    }
    
    public void spawnManual(Location loc) {
        MagmaCube boss = (MagmaCube) EntityUtils.spawnNatural(loc, EntityType.MAGMA_CUBE);
        transform(boss);
    }

    private void transform(MagmaCube boss) {
        boss.getPersistentDataContainer().set(BOSS_KEY, PersistentDataType.BYTE, (byte) 1);
        activeBosses.put(boss.getUniqueId(), new GigaMagmaCubeBoss(boss, plugin, true));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getLocation().getWorld().getEnvironment() != World.Environment.NETHER) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;
        if (!(event.getEntity() instanceof Mob)) return;
        
        if (event.getEntityType() != EntityType.MAGMA_CUBE && event.getEntityType() != EntityType.STRIDER) return;
        if (event.getEntity().getPersistentDataContainer().has(BOSS_KEY, PersistentDataType.BYTE)) return;
        if (event.getEntity().getPersistentDataContainer().has(MINION_KEY, PersistentDataType.BYTE)) return;
        
        Material type = event.getLocation().getBlock().getType();
        Material downType = event.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
        boolean isLava = (type == Material.LAVA || downType == Material.LAVA);
        
        if (isLava) {
            // Lava: 1 in 150 chance, 11x11x10 space (radius 5)
            if (random.nextInt(150) != 0) return;
            if (!hasOpenSpace(event.getLocation(), 5, 10)) return;
        } else {
            // Not lava: 1 in 500 chance, 15x15x15 space (radius 7)
            if (random.nextInt(500) != 0) return;
            if (!hasOpenSpace(event.getLocation(), 7, 15)) return;
        }
        
        event.setCancelled(true);
        spawnManual(event.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity().getPersistentDataContainer().has(BOSS_KEY, PersistentDataType.BYTE)) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL || event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        
        // Handle boss damage
        if (event.getEntity().getPersistentDataContainer().has(BOSS_KEY, PersistentDataType.BYTE)) {
            // 50% immune to arrows
            if (event.getDamager() instanceof AbstractArrow) {
                if (random.nextBoolean()) {
                    event.setCancelled(true);
                    return;
                }
            }
            
            // Track attacker
            Player attacker = null;
            if (event.getDamager() instanceof Player) {
                attacker = (Player) event.getDamager();
            } else if (event.getDamager() instanceof AbstractArrow arrow && arrow.getShooter() instanceof Player) {
                attacker = (Player) arrow.getShooter();
            }
            
            if (attacker != null) {
                GigaMagmaCubeBoss bossLogic = activeBosses.get(event.getEntity().getUniqueId());
                if (bossLogic != null) {
                    bossLogic.addAttacker(attacker.getUniqueId());
                }
            }
        }
        
        // Handle minion hit (10% chance to spawn another minion)
        if (event.getDamager().getPersistentDataContainer().has(MINION_KEY, PersistentDataType.BYTE)) {
            if (event.getEntity() instanceof Player p) {
                if (random.nextInt(100) < 10) {
                    Location loc = p.getLocation().add(random.nextInt(5) - 2, 1, random.nextInt(5) - 2);
                    MagmaCube newMinion = (MagmaCube) EntityUtils.spawnNatural(loc, EntityType.MAGMA_CUBE);
                    EntityUtils.setCustomName(newMinion, "<#FFCA28>Magma Cube Secuaz</#FFCA28>");
                    newMinion.getPersistentDataContainer().set(MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(BOSS_KEY, PersistentDataType.BYTE)) {
            GigaMagmaCubeBoss bossLogic = activeBosses.remove(event.getEntity().getUniqueId());
            if (bossLogic != null) {
                bossLogic.handleDeath();
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSlimeSplit(SlimeSplitEvent event) {
        if (!isActive()) return;
        if (event.getEntity().getPersistentDataContainer().has(BOSS_KEY, PersistentDataType.BYTE)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!isActive()) return;
        if (event.getEntity().getShooter() instanceof Entity shooter) {
            if (shooter.getPersistentDataContainer().has(BLAZE_KEY, PersistentDataType.BYTE)) {
                Location hitLoc = event.getHitEntity() != null 
                    ? event.getHitEntity().getLocation() 
                    : (event.getHitBlock() != null ? event.getHitBlock().getLocation() : null);
                    
                if (hitLoc != null) {
                    hitLoc.getWorld().strikeLightning(hitLoc);
                }
            }
        }
    }
    
    private boolean hasOpenSpace(Location loc, int radius, int height) {
        World world = loc.getWorld();
        int cx = loc.getBlockX();
        int cy = loc.getBlockY();
        int cz = loc.getBlockZ();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= height; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block b = world.getBlockAt(cx + x, cy + y, cz + z);
                    if (b.getType().isSolid()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}

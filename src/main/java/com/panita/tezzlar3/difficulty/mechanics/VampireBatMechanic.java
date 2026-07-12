package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import org.bukkit.event.world.ChunkLoadEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;public class VampireBatMechanic extends DifficultyMechanic {

    private final NamespacedKey VAMPIRE_KEY;
    private final NamespacedKey LAST_BITE_KEY;
    private final NamespacedKey STOLEN_HEARTS_KEY;
    private final Random random = new Random();
    private final Set<Bat> activeBats = new HashSet<>();

    public VampireBatMechanic(JavaPlugin plugin) {
        super(plugin, 24);
        VAMPIRE_KEY = new NamespacedKey(plugin, "vampire_bat");
        LAST_BITE_KEY = new NamespacedKey(plugin, "last_bite");
        STOLEN_HEARTS_KEY = new NamespacedKey(plugin, "stolen_hearts");
        
        CustomMobManager.register(CustomMobType.VAMPIRE_BAT, this::spawnManual);

        // Pre-populate loaded bats
        for (World world : Bukkit.getWorlds()) {
            for (Bat bat : world.getEntitiesByClass(Bat.class)) {
                if (bat.getPersistentDataContainer().has(VAMPIRE_KEY, PersistentDataType.BYTE)) {
                    activeBats.add(bat);
                }
            }
        }

        // Bat attack logic thread
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) return;
                
                Iterator<Bat> iterator = activeBats.iterator();
                while (iterator.hasNext()) {
                    Bat bat = iterator.next();
                    if (!bat.isValid() || bat.isDead()) {
                        iterator.remove();
                        continue;
                    }
                    
                    // Find closest player using mathematical distance
                    Player target = null;
                    double closestDistSq = 256.0; // 16 blocks radius
                    
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.isValid() && !p.isDead() && p.getWorld().equals(bat.getWorld())) {
                            double distSq = p.getLocation().distanceSquared(bat.getLocation());
                            if (distSq < closestDistSq) {
                                closestDistSq = distSq;
                                target = p;
                            }
                        }
                    }
                    
                    if (target != null) {
                        // Homing missile logic
                        Vector dir = target.getEyeLocation().toVector().subtract(bat.getLocation().toVector()).normalize();
                        bat.setVelocity(dir.multiply(0.4)); // Fly aggressively towards player
                        
                        // Check collision
                        if (bat.getLocation().distanceSquared(target.getEyeLocation()) < 2.25) { // 1.5 blocks
                            executeBite(bat, target);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // Runs every 2 ticks (very responsive)
    }

    private void executeBite(Bat bat, Player player) {
        long lastBite = bat.getPersistentDataContainer().getOrDefault(LAST_BITE_KEY, PersistentDataType.LONG, 0L);
        long now = System.currentTimeMillis();
        
        if (now - lastBite > 3000) {
            bat.getPersistentDataContainer().set(LAST_BITE_KEY, PersistentDataType.LONG, now);
            
            if (random.nextDouble() <= 0.3) {
                int stolen = bat.getPersistentDataContainer().getOrDefault(STOLEN_HEARTS_KEY, PersistentDataType.INTEGER, 0);
                if (stolen < 5) {
                    AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
                    if (maxHealthAttr != null) {
                        double currentMax = maxHealthAttr.getBaseValue();
                        if (currentMax > 2.0) { // Keep at least 1 heart
                            maxHealthAttr.setBaseValue(currentMax - 2.0); // Steal 1 heart
                            if (player.getHealth() > maxHealthAttr.getValue()) {
                                player.setHealth(maxHealthAttr.getValue());
                            }
                            
                            bat.getPersistentDataContainer().set(STOLEN_HEARTS_KEY, PersistentDataType.INTEGER, stolen + 1);
                            
                            // Drop Tezzlar Heart
                            ItemStack heart = CustomItemManager.getItem("tezzlar_heart");
                            if (heart != null) {
                                player.getWorld().dropItemNaturally(player.getLocation(), heart);
                            }
                            
                            // VFX / SFX
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_DEATH, 1.0f, 0.5f);
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, 1.0f, 1.0f);
                            player.getWorld().spawnParticle(Particle.BLOCK, player.getEyeLocation(), 30, 0.3, 0.3, 0.3, 0.1, org.bukkit.Material.REDSTONE_BLOCK.createBlockData());
                            
                            Messenger.prefixedSend(player, "<#FF2020>¡Un Murciélago Vampiro te ha robado un corazón!</#FF2020>");
                        } else {
                            player.damage(2.0, bat);
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_AMBIENT, 1.0f, 2.0f);
                        }
                    }
                } else {
                    player.damage(2.0, bat);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_AMBIENT, 1.0f, 2.0f);
                }
            } else {
                player.damage(2.0, bat);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_AMBIENT, 1.0f, 2.0f);
            }
        } else {
            // Standard melee damage on cooldown
            player.damage(2.0, bat);
        }
        
        // Bounce bat back slightly
        bat.setVelocity(bat.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(0.5));
    }

    public void spawnManual(Location loc) {
        Bat bat = (Bat) EntityUtils.spawnNatural(loc, EntityType.BAT);
        if (bat != null) {
            makeVampire(bat);
        }
    }

    private void makeVampire(Bat bat) {
        bat.getPersistentDataContainer().set(VAMPIRE_KEY, PersistentDataType.BYTE, (byte) 1);
        EntityUtils.setCustomName(bat, "&4Murciélago Vampiro");
        activeBats.add(bat);
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!bat.isValid() || bat.isDead()) return;
            
            if (bat.getAttribute(Attribute.MAX_HEALTH) != null) {
                bat.getAttribute(Attribute.MAX_HEALTH).setBaseValue(24.0);
                bat.setHealth(24.0);
            }
        });
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity e : event.getChunk().getEntities()) {
            if (e instanceof Bat bat && bat.getPersistentDataContainer().has(VAMPIRE_KEY, PersistentDataType.BYTE)) {
                activeBats.add(bat);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMonsterSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Monster)) return;
        
        if (EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) {
            // 0.2% chance of any monster spawn being a Vampire Bat
            if (random.nextDouble() <= 0.002) {
                event.setCancelled(true);
                spawnManual(entity.getLocation());
            }
        }
    }
}

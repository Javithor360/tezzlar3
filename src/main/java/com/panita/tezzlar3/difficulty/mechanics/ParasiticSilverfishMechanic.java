package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import org.bukkit.Location;

import java.util.Random;

public class ParasiticSilverfishMechanic extends DifficultyMechanic {

    private final NamespacedKey PARASITE_KEY;
    private final NamespacedKey ATTACH_TIME_KEY;
    private final Random random = new Random();

    public ParasiticSilverfishMechanic(JavaPlugin plugin) {
        super(plugin, 6);
        PARASITE_KEY = new NamespacedKey(plugin, "is_parasite");
        ATTACH_TIME_KEY = new NamespacedKey(plugin, "attach_time");
        CustomMobManager.register(CustomMobType.PARASITE_SILVERFISH, this::spawnManual);

        // Repetitive task every 1.5 seconds (30 ticks) for the parasite damage
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead() || !player.isValid()) continue;
                
                for (Entity passenger : player.getPassengers()) {
                    if (passenger instanceof Silverfish parasite && parasite.getPersistentDataContainer().has(PARASITE_KEY, PersistentDataType.BYTE)) {
                        int time = parasite.getPersistentDataContainer().getOrDefault(ATTACH_TIME_KEY, PersistentDataType.INTEGER, 0);
                        time++;
                        parasite.getPersistentDataContainer().set(ATTACH_TIME_KEY, PersistentDataType.INTEGER, time);
                        
                        // Exponential damage: Starts at 1.0 (half heart) and increases
                        // f(t) = 1.0 * (1.15 ^ t)
                        double damage = Math.pow(1.15, time);
                        
                        // Cap at 70 HP max damage per second
                        if (damage > 70.0) {
                            damage = 70.0;
                        }
                        
                        player.damage(damage, parasite);
                    }
                }
            }
        }, 30L, 30L);
    }
    
    public void spawnManual(Location loc) {
        Silverfish silverfish = (Silverfish) EntityUtils.spawnNatural(loc, EntityType.SILVERFISH);
        makeParasite(silverfish);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMonsterSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Monster)) return;
        if (entity instanceof Silverfish) return; // Prevent infinite loop
        
        if (EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) {
            World world = entity.getWorld();
            boolean isCave = world.getEnvironment() == World.Environment.NORMAL && entity.getLocation().getY() < 64;
            boolean isNether = world.getEnvironment() == World.Environment.NETHER;
            
            if (isCave || isNether) {
                // 3% chance
                if (random.nextDouble() < 0.03) {
                    event.setCancelled(true);
                    spawnManual(entity.getLocation());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onParasiteDamagePlayer(EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        
        if (event.getDamager() instanceof Silverfish parasite && parasite.getPersistentDataContainer().has(PARASITE_KEY, PersistentDataType.BYTE)) {
            
            // 15% chance to clone on hit
            if (random.nextDouble() < 0.25) {
                Silverfish newParasite = (Silverfish) EntityUtils.spawnNatural(parasite.getLocation(), EntityType.SILVERFISH);
                makeParasite(newParasite);
            }
            
            // If attacking player, attempt to mount
            if (event.getEntity() instanceof Player player) {
                // Verify the player doesn't already have a parasite
                boolean hasParasite = false;
                for (Entity pass : player.getPassengers()) {
                    if (pass instanceof Silverfish && pass.getPersistentDataContainer().has(PARASITE_KEY, PersistentDataType.BYTE)) {
                        hasParasite = true;
                        break;
                    }
                }
                
                if (!hasParasite) {
                    parasite.getPersistentDataContainer().set(ATTACH_TIME_KEY, PersistentDataType.INTEGER, 0);
                    player.addPassenger(parasite);
                    event.setCancelled(true); // Cancels the initial melee damage
                    Messenger.prefixedSend(player, "<#1F7A55>Un <red>Lepisma Parásito</red> se ha subido en ti, ¡spammea SHIFT para sacudirte!</#1F7A55>");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onParasiteInfest(EntityChangeBlockEvent event) {
        if (!isActive()) return;
        
        // Prevent parasites from infesting blocks (turning stone into infested stone)
        if (event.getEntity() instanceof Silverfish parasite && parasite.getPersistentDataContainer().has(PARASITE_KEY, PersistentDataType.BYTE)) {
            event.setCancelled(true);
        }
    }

    // Allow the player to dismount the parasite by pressing SHIFT (sneaking), with a 10% chance of success
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (!isActive()) return;
        
        Player player = event.getPlayer();
        if (event.isSneaking()) {
            for (Entity passenger : player.getPassengers()) {
                if (passenger instanceof Silverfish parasite && parasite.getPersistentDataContainer().has(PARASITE_KEY, PersistentDataType.BYTE)) {
                    // Only 10% chance to successfully shake it off
                    if (random.nextDouble() <= 0.10) {
                        // Dismount
                        player.removePassenger(parasite);
                        parasite.getPersistentDataContainer().set(ATTACH_TIME_KEY, PersistentDataType.INTEGER, 0);
                        
                        // Push it further in the direction the player is looking
                        parasite.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(0.6));
                        
                        // Apply Glowing for 2 seconds (40 ticks)
                        parasite.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false));
                        
                        // Apply Slowness for 5 seconds (100 ticks) to make it easier to hit
                        parasite.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2, false, false));
                    }
                }
            }
        }
    }

    private void makeParasite(Silverfish silverfish) {
        silverfish.getPersistentDataContainer().set(PARASITE_KEY, PersistentDataType.BYTE, (byte) 1);
        // Custom name (hidden by default)
        EntityUtils.setCustomName(silverfish, "&cLepisma Parásito");
        
        // Wrapping in 1 tick delay (runTask) ensures Spigot/Paper has finished the spawn
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!silverfish.isValid() || silverfish.isDead()) return;
            
            AttributeInstance scale = silverfish.getAttribute(Attribute.SCALE);
            if (scale != null) scale.setBaseValue(2.0);
            
            AttributeInstance health = silverfish.getAttribute(Attribute.MAX_HEALTH);
            if (health != null) {
                health.setBaseValue(16.0);
                if (silverfish.getHealth() > 16.0) silverfish.setHealth(16.0);
            }
            
            AttributeInstance speed = silverfish.getAttribute(Attribute.MOVEMENT_SPEED);
            if (speed != null) speed.setBaseValue(speed.getBaseValue() * 1.7); // x1.5 speed
        });
    }
}

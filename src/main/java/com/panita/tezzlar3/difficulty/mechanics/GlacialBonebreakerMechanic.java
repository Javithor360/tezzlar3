package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Particle;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class GlacialBonebreakerMechanic extends DifficultyMechanic {
    private final Random random = new Random();
    public static final NamespacedKey BOSS_KEY = new NamespacedKey(Tezzlar.getInstance(), "glacial_bonebreaker");
    public static NamespacedKey FROZEN_BOW_KEY;
    public static NamespacedKey MINION_KEY;
    public static NamespacedKey FAKE_CLONE_KEY;
    public static NamespacedKey PROJECTILE_KEY;

    private static final Map<UUID, GlacialBonebreakerBoss> activeBosses = new HashMap<>();

    public static void removeActiveBoss(UUID uuid) {
        activeBosses.remove(uuid);
    }
    
    public static void addActiveBoss(UUID uuid, GlacialBonebreakerBoss boss) {
        activeBosses.put(uuid, boss);
    }
    
    public static GlacialBonebreakerBoss getBoss(UUID uuid) {
        return activeBosses.get(uuid);
    }

    public GlacialBonebreakerMechanic(JavaPlugin plugin) {
        super(plugin, 19);
        FROZEN_BOW_KEY = new NamespacedKey(plugin, "glacial_frozen_bow");
        MINION_KEY = new NamespacedKey(plugin, "glacial_minion");
        FAKE_CLONE_KEY = new NamespacedKey(plugin, "glacial_fake_clone");
        PROJECTILE_KEY = new NamespacedKey(plugin, "glacial_projectile");

        CustomMobManager.register(CustomMobType.GLACIAL_BONEBREAKER, this::spawnManual);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntitiesByClass(Stray.class)) {
                    if (entity.getPersistentDataContainer().has(BOSS_KEY, PersistentDataType.BYTE)) {
                        if (!activeBosses.containsKey(entity.getUniqueId())) {
                            activeBosses.put(entity.getUniqueId(), new GlacialBonebreakerBoss((Stray) entity, plugin, false));
                        }
                    }
                }
            }
        }, 100L, 100L);
    }

    public void spawnManual(Location loc) {
        Stray boss = (Stray) EntityUtils.spawnNatural(loc, EntityType.STRAY);
        if (boss != null) transform(boss);
    }

    private void transform(Stray boss) {
        boss.getPersistentDataContainer().set(BOSS_KEY, PersistentDataType.BYTE, (byte) 1);
        activeBosses.put(boss.getUniqueId(), new GlacialBonebreakerBoss(boss, plugin, true));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;
        if (event.getEntityType() != EntityType.STRAY) return;
        
        if (event.getEntity().getPersistentDataContainer().has(BOSS_KEY, PersistentDataType.BYTE)) return;
        if (event.getEntity().getPersistentDataContainer().has(FAKE_CLONE_KEY, PersistentDataType.BYTE)) return;
        
        // 1 in 1000 chance to spawn naturally
        if (random.nextInt(1000) != 0) return;
        
        event.setCancelled(true);
        spawnManual(event.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        if (event.getEntity().getPersistentDataContainer().has(BOSS_KEY, PersistentDataType.BYTE)) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL || 
                event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION ||
                event.getCause() == EntityDamageEvent.DamageCause.FREEZE ||
                event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                event.setCancelled(true);
                return;
            }
            
            GlacialBonebreakerBoss bossLogic = activeBosses.get(event.getEntity().getUniqueId());
            if (bossLogic != null && bossLogic.isInvulnerable()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isActive()) return;

        if (event.getEntity().getPersistentDataContainer().has(BOSS_KEY, PersistentDataType.BYTE)) {
            GlacialBonebreakerBoss bossLogic = activeBosses.get(event.getEntity().getUniqueId());
            if (bossLogic != null && bossLogic.isInvulnerable()) {
                event.setCancelled(true);
                return;
            }

            // Negate Smite damage
            Player attacker = null;
            if (event.getDamager() instanceof Player p) {
                attacker = p;
            } else if (event.getDamager() instanceof AbstractArrow arrow && arrow.getShooter() instanceof Player p) {
                attacker = p;
            }
            
            if (attacker != null) {
                if (bossLogic != null) bossLogic.addAttacker(attacker.getUniqueId());
                
                if (event.getDamager() instanceof Player p) {
                    ItemStack weapon = p.getInventory().getItemInMainHand();
                    if (weapon.hasItemMeta() && weapon.getItemMeta().hasEnchant(Enchantment.SMITE)) {
                        int smiteLevel = weapon.getItemMeta().getEnchantLevel(Enchantment.SMITE);
                        double smiteBonus = 2.5 * smiteLevel;
                        double newDamage = Math.max(1.0, event.getDamage() - smiteBonus);
                        event.setDamage(newDamage);
                    }
                }
            }
        }
        
        // Handle Fake Clone logic
        if (event.getEntity().getPersistentDataContainer().has(FAKE_CLONE_KEY, PersistentDataType.BYTE)) {
            event.setCancelled(true);
            event.getEntity().getWorld().spawnParticle(Particle.SNOWFLAKE, event.getEntity().getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5, 0.1);
            event.getEntity().remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(BOSS_KEY, PersistentDataType.BYTE)) {
            GlacialBonebreakerBoss bossLogic = activeBosses.remove(event.getEntity().getUniqueId());
            if (bossLogic != null) {
                bossLogic.handleDeath();
            }
        }
        if (event.getEntity().getPersistentDataContainer().has(FAKE_CLONE_KEY, PersistentDataType.BYTE) || 
            event.getEntity().getPersistentDataContainer().has(MINION_KEY, PersistentDataType.BYTE)) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShootBow(EntityShootBowEvent event) {
        if (!isActive()) return;
        
        // Handle boss shoot
        if (event.getEntity().getPersistentDataContainer().has(BOSS_KEY, PersistentDataType.BYTE)) {
            event.setCancelled(true);
            GlacialBonebreakerBoss bossLogic = activeBosses.get(event.getEntity().getUniqueId());
            if (bossLogic != null) {
                bossLogic.fireHomingToAll();
            }
            return;
        }

        // Handle frozen bow debuff for players
        if (event.getEntity() instanceof Player p) {
            if (p.getPersistentDataContainer().has(FROZEN_BOW_KEY, PersistentDataType.BYTE)) {
                event.setCancelled(true);
                Snowball snowball = p.launchProjectile(Snowball.class);
                if (snowball != null) {
                    snowball.setVelocity(event.getProjectile().getVelocity().multiply(0.5));
                }
                p.playSound(p.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1.0f, 1.0f);
            }
        }
    }
}

package com.panita.tezzlar3.qol.listeners;

import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
public class SuperDiamondListener implements Listener {

    public SuperDiamondListener() {
        // Scheduler for passive armor effects
        Bukkit.getScheduler().runTaskTimer(Tezzlar.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (hasFullSet(player)) {
                    // 120 ticks = 6 seconds, to ensure it doesn't run out between checks (every 3 secs)
                    player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 120, 1, false, false, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120, 0, false, false, true));
                }
            }
        }, 0L, 60L); // Check every 3 seconds (60 ticks)
    }

    private boolean hasFullSet(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chest = player.getInventory().getChestplate();
        ItemStack legs = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();
        
        return CustomItemManager.isCustomItem(helmet, "superdiamond_helmet") &&
               CustomItemManager.isCustomItem(chest, "superdiamond_chestplate") &&
               CustomItemManager.isCustomItem(legs, "superdiamond_leggings") &&
               CustomItemManager.isCustomItem(boots, "superdiamond_boots");
    }

    @EventHandler
    public void onPhantomSpawn(PhantomPreSpawnEvent event) {
        if (!(event.getSpawningEntity() instanceof Player player)) return;
        
        ItemStack helmet = player.getInventory().getHelmet();
        if (CustomItemManager.isCustomItem(helmet, "superdiamond_helmet")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSwordHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!CustomItemManager.isCustomItem(hand, "superdiamond_sword")) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        
        // 1. Instant Kill (1/100)
        if (Math.random() < 0.01 && target.getHealth() <= 500.0) {
            event.setDamage(target.getHealth() + 1000.0);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.5f);
            player.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
            return; // Instakill overrides double damage
        }
        
        // Custom Sharpness: Sharpness normally adds (0.5 * level + 0.5).
        // Since vanilla already applies this once, we add it again to double its effect.
        int sharpLevel = hand.getEnchantmentLevel(Enchantment.SHARPNESS);
        if (sharpLevel > 0) {
            double extraSharpnessDamage = 0.5 * sharpLevel + 0.5;
            event.setDamage(event.getDamage() + extraSharpnessDamage);
        }
        
        // 2. Double Damage (1/30)
        if (Math.random() < 0.033) {
            event.setDamage(event.getDamage() * 2);
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.5f);
            player.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.2);
        }
    }

    @EventHandler
    public void onSwordInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!CustomItemManager.isCustomItem(hand, "superdiamond_sword")) return;
        
        // Visual Cooldown handling via Paper/Bukkit API
        if (player.hasCooldown(hand.getType())) return;
        player.setCooldown(hand.getType(), 200); // 200 ticks = 10 seconds
        
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.5f);
        
        Location center = player.getLocation().clone();
        center.setY(Math.floor(center.getY()));
        
        new BukkitRunnable() {
            int radius = 1;
            @Override
            public void run() {
                if (radius > 5 || !player.isValid()) {
                    this.cancel();
                    return;
                }
                
                int points = radius * 8;
                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location loc = center.clone().add(x, 0, z);
                    
                    BlockDisplay bd = (BlockDisplay) loc.getWorld().spawnEntity(loc, EntityType.BLOCK_DISPLAY);
                    bd.setBlock(Bukkit.createBlockData(Material.DIAMOND_BLOCK));
                    Transformation t = bd.getTransformation();
                    t.getScale().set(0.5f, 2.0f, 0.5f);
                    bd.setTransformation(t);
                    
                    Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                        if (bd.isValid()) {
                            bd.getWorld().spawnParticle(Particle.BLOCK, bd.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, Bukkit.createBlockData(Material.DIAMOND_BLOCK));
                            bd.remove();
                        }
                    }, 10L);
                }
                
                for (Entity e : center.getWorld().getNearbyEntities(center, 6, 4, 6)) {
                    if (!(e instanceof LivingEntity target)) continue;
                    if (target.equals(player)) continue; // ignore self
                    
                    if (target.getLocation().distance(center) <= radius && target.getLocation().distance(center) > radius - 1.5) {
                        if (target.getLocation().getY() - center.getY() < 2.0) {
                            target.damage(25.0, player);
                            
                            Vector pushDir = target.getLocation().toVector().subtract(center.toVector());
                            pushDir.setY(0); // Only calculate horizontal push direction
                            if (pushDir.lengthSquared() > 0.001) {
                                pushDir.normalize().multiply(1.2); // Horizontal force
                            }
                            pushDir.setY(0.4); // Add slight vertical bump
                            
                            target.setVelocity(pushDir);
                        }
                    }
                }
                radius++;
            }
        }.runTaskTimer(Tezzlar.getInstance(), 0L, 2L);
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        // SuperDiamond Arrow logic
        ItemStack consumed = event.getConsumable();
        if (consumed != null && CustomItemManager.isCustomItem(consumed, "superdiamond_arrow")) {
            event.getProjectile().setMetadata("superdiamond_arrow", new FixedMetadataValue(Tezzlar.getInstance(), true));
        }

        // SuperDiamond Bow logic
        ItemStack bow = event.getBow();
        if (bow != null && CustomItemManager.isCustomItem(bow, "superdiamond_bow")) {
            event.getProjectile().setMetadata("superdiamond_bow_arrow", new FixedMetadataValue(Tezzlar.getInstance(), true));
            if (player.isSneaking()) {
                if (!player.hasCooldown(bow.getType())) {
                    player.setCooldown(bow.getType(), 60); // 3 seconds (60 ticks)
                    event.getProjectile().setMetadata("superdiamond_bow_sneak", new FixedMetadataValue(Tezzlar.getInstance(), true));
                }
            }
        }
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;
        
        Location hitLoc = event.getHitBlock() != null ? event.getHitBlock().getLocation() : (event.getHitEntity() != null ? event.getHitEntity().getLocation() : event.getEntity().getLocation());
        
        // SuperDiamond Bow Logic
        if (event.getEntity().hasMetadata("superdiamond_bow_arrow")) {
            // 20% lightning
            if (Math.random() < 0.20) {
                hitLoc.getWorld().strikeLightning(hitLoc);
            }

            if (event.getEntity().hasMetadata("superdiamond_bow_sneak")) {
                double chance = Math.random();
                if (chance < 0.50) {
                    // Homing arrows rain
                    List<LivingEntity> nearby = new ArrayList<>();
                    for (Entity e : hitLoc.getWorld().getNearbyEntities(hitLoc, 15, 15, 15)) {
                        if (e instanceof LivingEntity le && !e.equals(shooter)) {
                            nearby.add(le);
                        }
                    }
                    
                    if (!nearby.isEmpty()) {
                        int arrowCount = ThreadLocalRandom.current().nextInt(5, 21);
                        hitLoc.getWorld().playSound(hitLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
                        
                        for (int i = 0; i < arrowCount; i++) {
                            LivingEntity target = nearby.get(ThreadLocalRandom.current().nextInt(nearby.size()));
                            Location spawnLoc = hitLoc.clone().add(0, 3 + Math.random() * 2, 0);
                            
                            Arrow homingArrow = hitLoc.getWorld().spawnArrow(spawnLoc, new Vector(0, 0.5, 0), 1.0f, 12.0f);
                            homingArrow.setShooter(shooter);
                            homingArrow.setMetadata("superdiamond_bow_arrow", new FixedMetadataValue(Tezzlar.getInstance(), true)); // Double damage for these too
                            
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (!homingArrow.isValid() || homingArrow.isOnGround() || !target.isValid()) {
                                        this.cancel();
                                        return;
                                    }
                                    
                                    Vector dir = target.getEyeLocation().toVector().subtract(homingArrow.getLocation().toVector());
                                    if (dir.lengthSquared() > 0.001) {
                                        dir.normalize();
                                    }
                                    homingArrow.setVelocity(dir.multiply(1.5));
                                    homingArrow.getWorld().spawnParticle(Particle.CRIT, homingArrow.getLocation(), 2, 0, 0, 0, 0);
                                }
                            }.runTaskTimer(Tezzlar.getInstance(), 1L, 1L);
                        }
                    }
                } else {
                    // Vortex attack
                    hitLoc.getWorld().playSound(hitLoc, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 0.5f);
                    hitLoc.getWorld().playSound(hitLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.5f);
                    
                    for (int i = 0; i < 50; i++) {
                        Location pLoc = hitLoc.clone().add(Math.random() * 10 - 5, Math.random() * 5, Math.random() * 10 - 5);
                        hitLoc.getWorld().spawnParticle(Particle.PORTAL, pLoc, 1, 0, 0, 0, 0.1);
                        hitLoc.getWorld().spawnParticle(Particle.SOUL, hitLoc, 10, 2, 2, 2, 0.1);
                    }

                    for (Entity e : hitLoc.getWorld().getNearbyEntities(hitLoc, 10, 10, 10)) {
                        if (e instanceof LivingEntity && !e.equals(shooter)) {
                            Vector pull = hitLoc.toVector().subtract(e.getLocation().toVector());
                            if (pull.lengthSquared() > 0.001) {
                                pull.normalize();
                            }
                            pull.multiply(1.5).setY(0.6);
                            e.setVelocity(pull);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSuperDiamondArrowDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof AbstractArrow arrow) {
            if (arrow.hasMetadata("superdiamond_bow_arrow")) {
                event.setDamage(event.getDamage() * 2.0);
            }
            if (arrow.hasMetadata("superdiamond_arrow")) {
                if (event.getEntity() instanceof LivingEntity target && !(target instanceof Player)) {
                    if (arrow.getShooter() instanceof Player shooter) {
                        boolean isBoss = false;
                        NamespacedKey mobKey = new NamespacedKey(Tezzlar.getInstance(), "custom_mob_id");
                        if (target.getPersistentDataContainer().has(mobKey, PersistentDataType.STRING)) {
                            String customId = target.getPersistentDataContainer().get(mobKey, PersistentDataType.STRING);
                            if (customId != null && (customId.equals("giga_magma_cube") || customId.equals("glacial_bonebreaker"))) {
                                isBoss = true;
                            }
                        }
                        
                        if (!isBoss) {
                            if (Math.random() < 0.10) {
                                shooter.setMetadata("superdiamond_death", new FixedMetadataValue(Tezzlar.getInstance(), true));
                                shooter.damage(500.0, arrow);
                                Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                                    if (shooter.isValid() && shooter.hasMetadata("superdiamond_death")) {
                                        shooter.removeMetadata("superdiamond_death", Tezzlar.getInstance());
                                    }
                                }, 1L);
                            } else {
                                event.setDamage(500.0);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().hasMetadata("superdiamond_death")) {
            event.getEntity().removeMetadata("superdiamond_death", Tezzlar.getInstance());
            event.setDeathMessage("Parece que " + event.getEntity().getName() + " tentó la suerte con una flecha de superdiamante pero perdió su vida en el intento.");
        }
    }
}

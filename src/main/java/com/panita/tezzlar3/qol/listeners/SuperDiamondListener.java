package com.panita.tezzlar3.qol.listeners;

import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.bukkit.Location;

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
        
        // 2. Double Damage (1/30)
        if (Math.random() < 0.033) {
            event.setDamage(event.getDamage() * 2);
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.5f);
            player.getWorld().spawnParticle(Particle.FLASH, target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.END_ROD, target.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.2);
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
                
                Attribute attackAttr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic.attack_damage"));
                double baseDamage = (attackAttr != null && player.getAttribute(attackAttr) != null) ? 
                                    player.getAttribute(attackAttr).getValue() : 8.0;
                
                int sharpLevel = hand.getEnchantmentLevel(Enchantment.SHARPNESS);
                double sharpBonus = sharpLevel > 0 ? (0.5 * sharpLevel + 0.5) : 0.0;
                
                double totalSwordDamage = baseDamage + sharpBonus;
                double aoeDamage = totalSwordDamage * 2.0;
                
                for (Entity e : center.getWorld().getNearbyEntities(center, 6, 4, 6)) {
                    if (!(e instanceof LivingEntity target)) continue;
                    if (target.equals(player)) continue; // ignore self
                    
                    if (target.getLocation().distance(center) <= radius && target.getLocation().distance(center) > radius - 1.5) {
                        if (target.getLocation().getY() - center.getY() < 2.0) {
                            target.damage(aoeDamage, player);
                            target.setVelocity(new Vector(0, 0.5, 0));
                        }
                    }
                }
                radius++;
            }
        }.runTaskTimer(Tezzlar.getInstance(), 0L, 2L);
    }
}

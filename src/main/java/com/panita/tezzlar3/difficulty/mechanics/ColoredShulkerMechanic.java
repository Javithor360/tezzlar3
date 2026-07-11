package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import com.panita.tezzlar3.core.util.PlayerUtils;

import java.util.Random;

public class ColoredShulkerMechanic extends DifficultyMechanic {

    private final Random random = new Random();

    public ColoredShulkerMechanic(JavaPlugin plugin) {
        super(plugin, 22);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShulkerSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getEntityType() == EntityType.SHULKER) {
            Shulker shulker = (Shulker) event.getEntity();
            int sType = random.nextInt(200);
            
            if (sType <= 4) {
                shulker.setColor(DyeColor.RED);
            } else if (sType <= 15) {
                shulker.setColor(DyeColor.YELLOW);
            } else if (sType <= 25) {
                shulker.setColor(DyeColor.LIME);
            } else if (sType <= 35) {
                shulker.setColor(DyeColor.GREEN);
            } else if (sType <= 45) {
                shulker.setColor(DyeColor.BLACK);
            } else if (sType <= 65) {
                shulker.setColor(DyeColor.PURPLE);
            } else if (sType <= 75) {
                shulker.setColor(DyeColor.WHITE);
            } else if (sType <= 95) {
                shulker.setColor(DyeColor.GRAY);
            } else if (sType <= 115) {
                // Kept original behavior of not changing color
                shulker.setColor(shulker.getColor());
            } else if (sType <= 120) {
                shulker.setColor(DyeColor.BLUE);
            } else if (sType <= 130) {
                shulker.setColor(DyeColor.LIGHT_BLUE);
            } else if (sType <= 140) {
                shulker.setColor(DyeColor.PINK);
            } else if (sType <= 150) {
                shulker.setColor(DyeColor.MAGENTA);
            } else if (sType <= 170) {
                shulker.setColor(DyeColor.LIGHT_GRAY);
            } else if (sType <= 185) {
                shulker.setColor(DyeColor.CYAN);
            } else if (sType <= 195) {
                shulker.setColor(DyeColor.ORANGE);
            } else {
                shulker.setColor(DyeColor.BROWN);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBulletHit(EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        
        if (event.getDamager() instanceof ShulkerBullet bullet) {
            ProjectileSource src = bullet.getShooter();
            
            if (src instanceof Shulker shulker && event.getEntity() instanceof Player player) {
                if (!PlayerUtils.isSurvival(player)) return;
                
                DyeColor color = shulker.getColor();
                if (color == null) return;
                
                switch (color) {
                    case RED -> {
                        Location loc = player.getLocation();
                        World w = loc.getWorld();
                        if (w != null) {
                            w.createExplosion(loc, 6.0F, true, true);
                        }
                    }
                    case YELLOW -> player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0, false, true, true));
                    case LIME -> player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 300, 0, false, true, true));
                    case GREEN -> player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 2, false, true, true));
                    case BLACK -> player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 0, false, true, true));
                    case PURPLE -> {
                        int co = random.nextInt(4);
                        Location loc = player.getLocation().clone();
                        if (co == 0) {
                            loc.add(random.nextInt(40) - 20.0, 0, random.nextInt(40) - 20.0);
                        } else if (co == 1) {
                            loc.add(-(random.nextInt(40) - 20.0), 0, -(random.nextInt(40) - 20.0));
                        } else if (co == 2) {
                            loc.add(random.nextInt(40) - 20.0, 0, -(random.nextInt(40) - 20.0));
                        } else {
                            loc.add(-(random.nextInt(40) - 20.0), 0, random.nextInt(40) - 20.0);
                        }
                        player.teleport(loc);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    }
                    case WHITE -> player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 160, 0, false, true, true));
                    case GRAY -> {
                        event.setDamage(4.0);
                        event.setCancelled(true);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 200, 127, false, false, false));
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
                    }
                    case BLUE -> {
                        World w = player.getWorld();
                        w.spawnEntity(player.getLocation(), EntityType.GLOW_SQUID);
                        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0F, 0.0F);
                    }
                    case LIGHT_BLUE -> {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 126, false, true, true));
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
                    }
                    case PINK -> player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1, false, false, false));
                    case MAGENTA -> {
                        World w = player.getWorld();
                        Slime slime = (Slime) w.spawnEntity(player.getLocation(), EntityType.SLIME);
                        slime.setSize(random.nextInt(8));
                    }
                    case LIGHT_GRAY -> player.setVelocity(player.getLocation().getDirection().multiply(100));
                    case CYAN -> {
                        event.setDamage(4.0);
                        event.setCancelled(true);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 254, false, false, false));
                        player.setFreezeTicks(200);
                    }
                    case ORANGE -> player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 200, 1, false, false, false));
                    case BROWN -> player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 1200, 2, false, false, false));
                    default -> {}
                }
            }
        }
    }
}

package com.panita.tezzlar3.bossfight.util;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.bossfight.listeners.BossListener;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BossAttacks {

    private static final Random random = new Random();

    // The list of allowed random mobs
    private static final EntityType[] VESTIGIO_MOBS = {
            EntityType.WITCH, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.PHANTOM,
            EntityType.SLIME, EntityType.VEX, EntityType.PILLAGER, EntityType.VINDICATOR,
            EntityType.EVOKER, EntityType.RAVAGER, EntityType.ZOGLIN, EntityType.CREEPER,
            EntityType.BREEZE, EntityType.BLAZE, EntityType.ILLUSIONER, EntityType.ENDERMAN,
            EntityType.SILVERFISH, EntityType.ENDERMITE, EntityType.SHULKER
    };

    /**
     * Gets all valid targets (Survival/Adventure players) within 100 blocks of the boss.
     */
    public static List<Player> getTargets(Player boss) {
        List<Player> targets = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(boss) && p.getWorld().equals(boss.getWorld()) && p.getLocation().distance(boss.getLocation()) <= 100.0) {
                if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                    targets.add(p);
                }
            }
        }
        return targets;
    }

    public static void executeThorPro(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;
        
        Messenger.prefixedBroadcast("<#FF5252>¡El Jefe ha desatado una <#FFF200>Tormenta Eléctrica</#FFF200> global!</#FF5252>");
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 3.0f, 0.5f);

        for (Player target : targets) {
            Location center = target.getLocation();
            World world = target.getWorld();
            
            // Fake lightnings around
            for (int i = 0; i < 5; i++) {
                double offsetX = (Math.random() - 0.5) * 10;
                double offsetZ = (Math.random() - 0.5) * 10;
                Location stormLoc = center.clone().add(offsetX, 0, offsetZ);
                world.strikeLightningEffect(world.getHighestBlockAt(stormLoc).getLocation());
            }
            
            // Real lightning on target
            Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                if (target.isOnline()) {
                    world.strikeLightning(target.getLocation());
                }
            }, 10L);
        }
    }

    public static void executeArachnophobia(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedBroadcast("<#FF5252>¡El Jefe ha invocado <#825C88>Aracnofobia</#825C88> sobre todos!</#FF5252>");
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 3.0f, 0.5f);

        for (Player target : targets) {
            Location loc = target.getLocation().getBlock().getLocation();
            
            // Place 3x3 cobwebs
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Location cobwebLoc = loc.clone().add(x, 0, z);
                    if (cobwebLoc.getBlock().getType().isAir()) {
                        cobwebLoc.getBlock().setType(Material.COBWEB);
                        
                        // Remove cobwebs after 10 seconds
                        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                            if (cobwebLoc.getBlock().getType() == Material.COBWEB) {
                                cobwebLoc.getBlock().setType(Material.AIR);
                            }
                        }, 200L);
                    }
                }
            }
            
            // Spawn 1 spider per target
            LivingEntity spider = EntityUtils.spawnNatural(loc.clone().add(0, 1, 0), EntityType.SPIDER);
            if (spider != null) {
                EntityUtils.setCustomName(spider, "<#825C88>Engendro del Jefe</#825C88>");
            }
        }
    }

    public static void executeMagmaFloor(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedBroadcast("<#FF5252>¡El suelo se ha convertido en <#FF8C00>Magma</#FF8C00>!</#FF5252>");
        boss.getWorld().playSound(boss.getLocation(), Sound.ITEM_BUCKET_FILL_LAVA, 3.0f, 0.5f);

        for (Player target : targets) {
            BossListener.addMagmaTrailTarget(target.getUniqueId());
        }
    }

    public static void applyPotionEffect(Player boss, PotionEffectType type, int seconds, int level, String name) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedBroadcast("<#FF5252>¡El Jefe ha aplicado <#FFF200>" + name + "</#FFF200> a todos!</#FF5252>");
        
        PotionEffect effect = new PotionEffect(type, seconds * 20, level - 1);
        for (Player target : targets) {
            target.addPotionEffect(effect);
            target.playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 1.0f);
        }
    }

    public static void spawnRandomMobs(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedBroadcast("<#FF5252>¡El Jefe ha invocado a los <#A020F0>Vestigios Errantes</#A020F0>!</#FF5252>");
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2.0f, 1.0f);

        for (Player target : targets) {
            int amount = 3 + random.nextInt(3); // 3 to 5
            for (int i = 0; i < amount; i++) {
                EntityType type = VESTIGIO_MOBS[random.nextInt(VESTIGIO_MOBS.length)];
                Location spawnLoc = target.getLocation().add(random.nextInt(11) - 5, 1, random.nextInt(11) - 5);
                LivingEntity mob = EntityUtils.spawnNatural(spawnLoc, type);
                
                if (mob != null) {
                    EntityUtils.setCustomName(mob, "&cVestigios Errantes");
                    if (mob instanceof Mob) {
                        ((Mob) mob).setTarget(target);
                    }
                }
            }
        }
    }

    public static void executeInventoryShuffle(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedBroadcast("<#FF5252>¡El Jefe ha <#4A90E2>desordenado los inventarios</#4A90E2>!</#FF5252>");
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.5f);

        for (Player target : targets) {
            List<ItemStack> contents = new ArrayList<>();
            for (int i = 0; i < 36; i++) {
                contents.add(target.getInventory().getItem(i));
            }
            Collections.shuffle(contents);
            for (int i = 0; i < 36; i++) {
                target.getInventory().setItem(i, contents.get(i));
            }
            Messenger.prefixedSend(target, "&c¡Tu inventario ha sido desordenado!");
        }
    }

    public static void executeChargedBeam(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedBroadcast("<#FF5252>¡El Jefe prepara un <#FFF200>Rayo Cargado</#FFF200> masivo!</#FF5252>");
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 2.0f, 0.5f);
        
        // 8 seconds = 160 ticks
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (boss.isDead() || !boss.isOnline() || ticks >= 160) {
                    if (boss.isOnline() && !boss.isDead()) {
                        for (Player p : targets) {
                            if (p.isOnline() && p.getWorld().equals(boss.getWorld()) && p.getLocation().distance(boss.getLocation()) <= 100) {
                                double dmg = 40.0 + random.nextInt(61); // 40 to 100
                                p.damage(dmg, boss); // Attack source is the boss
                                p.getWorld().strikeLightningEffect(p.getLocation());
                            }
                        }
                        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.5f);
                    }
                    this.cancel();
                    return;
                }
                
                // Draw particles towards all players
                for (Player p : targets) {
                    if (p.isOnline() && p.getWorld().equals(boss.getWorld())) {
                        Location start = boss.getLocation().add(0, boss.getHeight() / 2, 0);
                        Location end = p.getLocation().add(0, p.getHeight() / 2, 0);
                        double distance = start.distance(end);
                        if (distance <= 100) {
                            Vector dir = end.toVector().subtract(start.toVector()).normalize().multiply(0.5);
                            
                            Location current = start.clone();
                            int g = Math.max(0, 255 - (int) (((double) ticks / 160) * 255)); // Green fades out
                            int r = Math.min(255, (int) (((double) ticks / 160) * 255));    // Red fades in
                            
                            Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(r, g, 0), 2.0f);
                            for (double i = 0; i < distance; i += 0.5) {
                                current.getWorld().spawnParticle(Particle.DUST, current, 1, 0, 0, 0, 0, dust);
                                current.add(dir);
                            }
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(Tezzlar.getInstance(), 0L, 1L);
    }
}

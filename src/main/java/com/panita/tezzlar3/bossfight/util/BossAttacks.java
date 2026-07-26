package com.panita.tezzlar3.bossfight.util;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.bossfight.listeners.BossListener;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import org.joml.AxisAngle4f;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

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

    private static final EntityType[] JAVIMOBS = {
            EntityType.ZOMBIE, EntityType.HUSK, EntityType.SKELETON,
            EntityType.STRAY, EntityType.BOGGED, EntityType.WITHER,
            EntityType.PARCHED
    };

    /**
     * Gets all valid targets (Survival/Adventure players) within 100 blocks of the boss.
     */
    public static List<Player> getTargets(Player boss) {
        List<Player> targets = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isValidTarget(p, boss) && p.getLocation().distance(boss.getLocation()) <= 100.0) {
                targets.add(p);
            }
        }
        return targets;
    }

    public static boolean isValidTarget(Player p, Player boss) {
        if (p.equals(boss)) return false;
        if (!p.getWorld().equals(boss.getWorld())) return false;
        return p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE;
    }

    public static void executeThorPro(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;
        
        Messenger.prefixedSend(boss, "&aHas activado el ataque: &eRayos (Thor Pro)");
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

        Messenger.prefixedSend(boss, "&aHas activado el ataque: &5Aracnofobia");
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

        Messenger.prefixedSend(boss, "&aHas activado el ataque: &6Suelo de Magmablocks");
        Messenger.prefixedBroadcast("<#FF5252>¡El suelo se ha convertido en <#FF8C00>Magma</#FF8C00>!</#FF5252>");
        boss.getWorld().playSound(boss.getLocation(), Sound.ITEM_BUCKET_FILL_LAVA, 3.0f, 0.5f);

        for (Player target : targets) {
            BossListener.addMagmaTrailTarget(target.getUniqueId());
        }
    }

    public static void applyPotionEffect(Player boss, PotionEffectType type, int seconds, int level, String name) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedSend(boss, "&aHas activado el ataque: &e" + name);
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

        Messenger.prefixedSend(boss, "&aHas activado el ataque: &dInvocación de Vestigios");
        Messenger.prefixedBroadcast("<#FF5252>¡El Jefe ha invocado a los <#A020F0>Vestigios Errantes</#A020F0>!</#FF5252>");
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2.0f, 1.0f);

        for (Player target : targets) {
            int amount = 3 + random.nextInt(3); // 3 to 5
            for (int i = 0; i < amount; i++) {
                EntityType type = VESTIGIO_MOBS[random.nextInt(VESTIGIO_MOBS.length)];
                Location spawnLoc = target.getLocation().add(random.nextInt(11) - 5, 1, random.nextInt(11) - 5);
                LivingEntity mob = EntityUtils.spawnNatural(spawnLoc, type);
                
                if (mob != null) {
                    Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                        if (!mob.isValid()) return;
                        EntityUtils.setCustomName(mob, "&cVestigios Errantes");
                        if (mob.getEquipment() != null) {
                            mob.getEquipment().clear();
                        }
                        AttributeInstance scale = mob.getAttribute(Attribute.SCALE);
                        if (scale != null) {
                            scale.setBaseValue(2.0);
                        }
                        if (mob instanceof Mob) {
                            ((Mob) mob).setTarget(target);
                        }
                    }, 2L);
                }
            }
        }
    }

    public static void executeInventoryShuffle(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedSend(boss, "&aHas activado el ataque: &9Shuffle de Inventario");
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

        Messenger.prefixedSend(boss, "&aHas activado el ataque: &eRayo Cargado Masivo");
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

    public static void spawnJavimobs(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedSend(boss, "&aHas activado el ataque: &4Invocación de Javimobs");
        Messenger.prefixedBroadcast("<#FF5252>¡El Jefe ha invocado a los <#8B0000>Javimobs</#8B0000>!</#FF5252>");
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 2.0f, 0.5f);

        for (Player target : targets) {
            int amount = 1 + random.nextInt(4); // 1 to 4
            for (int i = 0; i < amount; i++) {
                EntityType type = JAVIMOBS[random.nextInt(JAVIMOBS.length)];
                Location spawnLoc = target.getLocation().add(random.nextInt(11) - 5, 1, random.nextInt(11) - 5);
                EntityUtils.setForceSpawnReason(SpawnReason.CUSTOM);
                LivingEntity mob = EntityUtils.spawnNatural(spawnLoc, type);
                EntityUtils.clearForceSpawnReason();
                     if (mob != null) {
                    Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                        if (!mob.isValid()) return;
                        String nameStr = type.name().toLowerCase();
                        nameStr = nameStr.substring(0, 1).toUpperCase() + nameStr.substring(1).replace("_", "");
                        EntityUtils.setCustomName(mob, "&cJavi" + nameStr);
                        
                        // Attributes
                        double hp = (type == EntityType.WITHER) ? 40.0 : 80.0;
                        AttributeInstance maxHealth = mob.getAttribute(Attribute.MAX_HEALTH);
                        if (maxHealth != null) maxHealth.setBaseValue(hp);
                        mob.setHealth(hp);
                        
                        AttributeInstance scale = mob.getAttribute(Attribute.SCALE);
                        if (scale != null) scale.setBaseValue(2.0);
                        
                        AttributeInstance armor = mob.getAttribute(Attribute.ARMOR);
                        if (armor != null) armor.setBaseValue(50.0);
                        
                        AttributeInstance damage = mob.getAttribute(Attribute.ATTACK_DAMAGE);
                        if (damage != null) damage.setBaseValue(35.0);

                        // Equipment
                        if (type != EntityType.WITHER) {
                            ItemStack weapon;
                            if (type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.BOGGED) {
                                if (random.nextBoolean()) {
                                    weapon = CustomItemManager.getItem("superdiamond_sword");
                                } else {
                                    weapon = CustomItemManager.getItem("superdiamond_bow");
                                }
                            } else {
                                weapon = CustomItemManager.getItem("superdiamond_sword");
                            }
                            
                            if (weapon != null && mob.getEquipment() != null) {
                                mob.getEquipment().setItemInMainHand(weapon);
                                mob.getEquipment().setItemInMainHandDropChance(0.0f);
                            }
                        }
                        
                        if (mob instanceof Mob) {
                            ((Mob) mob).setTarget(target);
                        }
                    }, 2L);
                }
            }
        }
    }

    public static void executeBetrayalVortex(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedSend(boss, "&aHas activado el ataque: &5Vórtice de la Traición");
        Messenger.prefixedBroadcast("<#FF5252>¡El Jefe ha abierto un <#8A2BE2>Vórtice de la Traición</#8A2BE2>!</#FF5252>");
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2.0f, 0.5f);

        int durationTicks = (5 + random.nextInt(3)) * 20; // 5 to 7 seconds

        new BukkitRunnable() {
            int ticks = 0;
            Map<UUID, Integer> nextDamageTick = new HashMap<>();

            @Override
            public void run() {
                if (boss.isDead() || !boss.isOnline() || ticks >= durationTicks) {
                    this.cancel();
                    return;
                }

                for (Player p : targets) {
                    if (p.isOnline() && p.getLocation().distance(boss.getLocation()) <= 40) {
                        Vector direction = boss.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
                        p.setVelocity(p.getVelocity().add(direction.multiply(0.15)));

                        UUID id = p.getUniqueId();
                        int nextTick = nextDamageTick.getOrDefault(id, 0);
                        if (ticks >= nextTick) {
                            double dmg = 10.0 + random.nextInt(21); // 10 to 30
                            p.damage(dmg, boss);
                            
                            // Schedule next damage between 4 and 30 ticks (0.2s - 1.5s)
                            nextDamageTick.put(id, ticks + 4 + random.nextInt(27));
                        }
                    }
                }

                double angle = ticks * 0.5;
                double radius = 5.0 - ((double)ticks / durationTicks) * 4.0;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                boss.getWorld().spawnParticle(Particle.PORTAL, boss.getLocation().add(x, 1, z), 10, 0.2, 0.2, 0.2, 0.05);

                ticks++;
            }
        }.runTaskTimer(Tezzlar.getInstance(), 0L, 1L);
    }

    public static void executeRockySpikes(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedSend(boss, "&aHas activado el ataque: &8Picos Rocosos");
        Messenger.prefixedBroadcast("<#FF5252>¡El Jefe hace emerger <#555555>Picos Rocosos</#555555>!</#FF5252>");

        for (Player target : targets) {
            Location center = target.getLocation().clone();
            List<Location> stalagmiteLocs = new ArrayList<>();
            stalagmiteLocs.add(center.clone());

            int spikeCount = 20 + random.nextInt(6); // 20 to 25
            for (int i = 0; i < spikeCount; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 12; 
                double offsetZ = (random.nextDouble() - 0.5) * 12;
                Location loc = center.clone().add(offsetX, 0, offsetZ);

                boolean found = false;
                for (int y = 5; y >= -10; y--) {
                    if (loc.clone().add(0, y, 0).getBlock().getType().isSolid()) {
                        loc.add(0, y + 1, 0);
                        found = true;
                        break;
                    }
                }
                if (!found) loc.setY(Math.floor(center.getY()));
                stalagmiteLocs.add(loc);
            }

            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks < 30) {
                        for (Location loc : stalagmiteLocs) {
                            loc.getWorld().spawnParticle(Particle.ASH, loc, 5, 0.5, 0.1, 0.5, 0);
                            loc.getWorld().spawnParticle(Particle.BLOCK, loc, 2, 0.3, 0.1, 0.3, Bukkit.createBlockData(Material.BEDROCK));
                        }
                    } else if (ticks == 30) {
                        center.getWorld().playSound(center, Sound.BLOCK_STONE_BREAK, 3.0f, 0.5f);
                        for (Location loc : stalagmiteLocs) {
                            int height = 4 + random.nextInt(4); // 4 to 7
                            float baseScale = 0.8f + random.nextFloat() * 0.7f; 

                            for (int h = 0; h < height; h++) {
                                float scale = baseScale * (1.0f - ((float)h / height));
                                Location blockLoc = loc.clone().add(0, h + 0.5, 0);
                                
                                BlockDisplay display = (BlockDisplay) loc.getWorld().spawnEntity(blockLoc, EntityType.BLOCK_DISPLAY);
                                display.setBlock(Bukkit.createBlockData(Material.BEDROCK));
                                display.setTransformation(new Transformation(
                                    new Vector3f(-scale/2, 0, -scale/2),
                                    new AxisAngle4f(),
                                    new Vector3f(scale, 1.0f, scale),
                                    new AxisAngle4f()
                                ));
                                
                                Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), display::remove, 60L);
                            }

                            // Launch players very high
                            for (Player p : center.getWorld().getPlayers()) {
                                if (p.getLocation().distanceSquared(loc) < 4.0 && !p.equals(boss)) {
                                    p.damage(25.0, boss);
                                    p.setVelocity(p.getVelocity().add(new Vector(0, 2.5, 0))); // High push
                                }
                            }
                        }
                    } else if (ticks > 90) {
                        this.cancel();
                    }
                    ticks++;
                }
            }.runTaskTimer(Tezzlar.getInstance(), 0L, 1L);
        }
    }

    public static void executeTimelessRain(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedSend(boss, "&aHas activado el ataque: &fLluvia Atemporal");
        Messenger.prefixedBroadcast("<#FF5252>¡El Jefe ha invocado una <#AAAAAA>Lluvia Atemporal</#AAAAAA>!</#FF5252>");
        boss.getWorld().playSound(boss.getLocation(), Sound.WEATHER_RAIN, 2.0f, 0.5f);

        new BukkitRunnable() {
            int count = 0;
            int maxProjectiles = 8 + random.nextInt(5); // 8 to 12
            @Override
            public void run() {
                if (boss.isDead() || count >= maxProjectiles) {
                    this.cancel();
                    return;
                }

                for (Player p : targets) {
                    if (p.isOnline()) {
                        Location spawnLoc = p.getLocation().add(random.nextInt(10)-5, 20, random.nextInt(10)-5);
                        
                        if (random.nextBoolean()) {
                            // Fireball
                            LargeFireball fireball = (LargeFireball) p.getWorld().spawnEntity(spawnLoc, EntityType.FIREBALL);
                            fireball.setDirection(new Vector(0, -1, 0));
                            fireball.setYield(3.0f); // Yield is kept >0 for visual explosion, but blocks are protected in Listener
                            fireball.setIsIncendiary(false);
                            fireball.setShooter(boss);
                        } else {
                            // Snowball
                            Snowball snowball = (Snowball) p.getWorld().spawnEntity(spawnLoc, EntityType.SNOWBALL);
                            snowball.setVelocity(new Vector(0, -2.0, 0)); // Fast fall
                            snowball.setShooter(boss);
                            
                            ItemDisplay display = (ItemDisplay) p.getWorld().spawnEntity(spawnLoc, EntityType.ITEM_DISPLAY);
                            display.setItemStack(new ItemStack(Material.SNOWBALL));
                            display.setTransformation(new Transformation(
                                new Vector3f(), new AxisAngle4f(), new Vector3f(4.0f, 4.0f, 4.0f), new AxisAngle4f()
                            ));
                            snowball.addPassenger(display);
                        }
                    }
                }
                count++;
            }
        }.runTaskTimer(Tezzlar.getInstance(), 0L, 10L); // 2 per second
    }

    public static void executePositionSwap(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedSend(boss, "&aHas activado el ataque: &dIntercambio de Posiciones");
        Messenger.prefixedBroadcast("<#FF5252>¡El espacio se distorsiona con un <#FF00FF>Intercambio de Posiciones</#FF00FF>!</#FF5252>");
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 0.5f);

        List<Player> allParticipants = new ArrayList<>(targets);
        allParticipants.add(boss);

        List<Location> locations = new ArrayList<>();
        for (Player p : allParticipants) {
            locations.add(p.getLocation().clone());
        }

        Collections.shuffle(locations, random);

        for (int i = 0; i < allParticipants.size(); i++) {
            Player p = allParticipants.get(i);
            Location newLoc = locations.get(i);
            
            p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation(), 50, 0.5, 1, 0.5, 0.1);
            p.teleport(newLoc);
            p.getWorld().spawnParticle(Particle.PORTAL, newLoc, 50, 0.5, 1, 0.5, 0.1);
            p.playSound(newLoc, Sound.ENTITY_ENDERMITE_DEATH, 1.0f, 1.0f);
        }
    }

    public static void executeToxicZones(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedSend(boss, "&aHas activado el ataque: &2Zonas Tóxicas");
        Messenger.prefixedBroadcast("<#FF5252>¡El Jefe ha desatado <#00FF00>Zonas Tóxicas</#00FF00> en la arena!</#FF5252>");
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 2.0f, 0.5f);

        int zoneCount = 1 + random.nextInt(5); // 1 to 5 zones
        List<Location> zones = new ArrayList<>();
        
        for (int i = 0; i < zoneCount; i++) {
            Location center;
            if (targets.isEmpty() || random.nextBoolean()) {
                double offsetX = (random.nextDouble() - 0.5) * 100; // 50 radius (100 diameter)
                double offsetZ = (random.nextDouble() - 0.5) * 100;
                center = boss.getLocation().add(offsetX, 0, offsetZ);
            } else {
                Player target = targets.get(random.nextInt(targets.size()));
                double offsetX = (random.nextDouble() - 0.5) * 10;
                double offsetZ = (random.nextDouble() - 0.5) * 10;
                center = target.getLocation().add(offsetX, 0, offsetZ);
            }
            
            boolean found = false;
            for (int y = 5; y >= -15; y--) {
                if (center.clone().add(0, y, 0).getBlock().getType().isSolid()) {
                    center.add(0, y + 1, 0);
                    found = true;
                    break;
                }
            }
            if (!found) center.setY(Math.floor(boss.getLocation().getY()));
            zones.add(center);
        }

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (boss.isDead() || !boss.isOnline() || ticks >= 300) { // 15 seconds
                    this.cancel();
                    return;
                }

                for (Location loc : zones) {
                    if (ticks % 10 == 0) { 
                        for (double t = 0; t <= 2 * Math.PI; t += 0.15) {
                            double x = 16 * Math.pow(Math.sin(t), 3);
                            double z = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
                            
                            x *= 0.375;
                            z *= 0.375;
                            
                            loc.getWorld().spawnParticle(Particle.NAUTILUS, loc.clone().add(x, 0.1, -z), 1, 0, 0, 0, 0);
                        }
                    }

                    if (ticks % 4 == 0) {
                        for (int i = 0; i < 5; i++) {
                            double ox = (random.nextDouble() - 0.5) * 12; // 12 diameter
                            double oz = (random.nextDouble() - 0.5) * 12;
                            if (ox*ox + oz*oz <= 36) { // inside the circle
                                loc.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, loc.clone().add(ox, 1, oz), 0, 0, 0.1, 0);
                                loc.getWorld().spawnParticle(Particle.NAUTILUS, loc.clone().add(ox, 0.2, oz), 0, 0, 0.1, 0);
                            }
                        }
                    }

                    if (ticks % 5 == 0) { // Damage 4 times a second
                        for (Player p : loc.getWorld().getPlayers()) {
                            if (isValidTarget(p, boss)) {
                                if (p.getLocation().distanceSquared(loc) <= 36.0) { // Radius 6 -> 36 squared
                                    p.damage(5.0, boss);
                                }
                            }
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(Tezzlar.getInstance(), 0L, 1L);
    }

    public static void executeDivineAdoration(Player boss) {
        List<Player> targets = getTargets(boss);
        if (targets.isEmpty()) return;

        Messenger.prefixedSend(boss, "&aHas activado el ataque: &4Adoración Divina");
        Messenger.prefixedBroadcast("<#FF5252>¡El Jefe exige <#8B0000>Adoración Divina</#8B0000>!</#FF5252>");
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.5f);

        int amount = 3 + random.nextInt(8); // 3 to 10
        BossManager manager = BossManager.getInstance();

        for (int i = 0; i < amount; i++) {
            Location loc;
            if (targets.isEmpty() || random.nextBoolean()) {
                double offsetX = (random.nextDouble() - 0.5) * 60;
                double offsetZ = (random.nextDouble() - 0.5) * 60;
                loc = boss.getLocation().add(offsetX, 0, offsetZ);
            } else {
                Player target = targets.get(random.nextInt(targets.size()));
                double offsetX = (random.nextDouble() - 0.5) * 20;
                double offsetZ = (random.nextDouble() - 0.5) * 20;
                loc = target.getLocation().add(offsetX, 0, offsetZ);
            }

            // Find floor
            boolean found = false;
            for (int y = 5; y >= -15; y--) {
                if (loc.clone().add(0, y, 0).getBlock().getType().isSolid()) {
                    loc.add(0, y + 1, 0);
                    found = true;
                    break;
                }
            }
            if (!found) loc.setY(Math.floor(boss.getLocation().getY()));
            
            // Snap to block grid to ensure perfectly centered placement
            Location blockLoc = loc.getBlock().getLocation();
            
            // Set 3x3 block to netherite
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    blockLoc.clone().add(dx, 0, dz).getBlock().setType(Material.NETHERITE_BLOCK);
                }
            }
            
            // Spawn exactly centered on top of the central block
            Location spawnLoc = blockLoc.clone().add(0.5, 1.0, 0.5);
            EntityUtils.setForceSpawnReason(SpawnReason.CUSTOM);
            LivingEntity mob = EntityUtils.spawnNatural(spawnLoc, EntityType.ZOMBIE);
            EntityUtils.clearForceSpawnReason();
            
            if (mob != null) {
                ((Zombie) mob).setBaby(false);
                mob.setAI(false);
                mob.setSilent(true);
                mob.setInvisible(true);
                mob.setGravity(false);
                
                EntityUtils.setCustomName(mob, "&4Estatua Divina");
                
                AttributeInstance maxHealth = mob.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth != null) maxHealth.setBaseValue(100.0);
                mob.setHealth(100.0);
                
                if (mob.getEquipment() != null) {
                    mob.getEquipment().clear();
                }
                
                Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                    if (!mob.isValid()) return;
                    
                    AttributeInstance scale = mob.getAttribute(Attribute.SCALE);
                    if (scale != null) scale.setBaseValue(2.0f);
                    
                    // Force teleport to perfectly aligned location in case spawnNatural shifted it
                    mob.teleport(spawnLoc);
                    
                    Location displayLoc = spawnLoc.clone();
                    displayLoc.setYaw(random.nextFloat() * 360f);
                    
                    ItemDisplay display = (ItemDisplay) blockLoc.getWorld().spawnEntity(displayLoc, EntityType.ITEM_DISPLAY);
                    display.setItemStack(CustomItemManager.getItem("legend_statue"));
                    display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                    display.setTransformation(new Transformation(
                        new Vector3f(0f, 0f, 0f),
                        new AxisAngle4f((float) Math.toRadians(-90), 1f, 0f, 0f),
                        new Vector3f(1f, 1f, 1f),
                        new AxisAngle4f()
                    ));
                    EntityUtils.setColoredGlowing(display, NamedTextColor.DARK_RED);
                    
                    manager.getActiveStatues().add(mob.getUniqueId());
                    
                }, 2L);
            }
        }
    }
}

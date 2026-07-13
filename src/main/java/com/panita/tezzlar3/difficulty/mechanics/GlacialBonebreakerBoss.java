package com.panita.tezzlar3.difficulty.mechanics;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

public class GlacialBonebreakerBoss {
    private final Stray boss;
    private final JavaPlugin plugin;
    private final BukkitRunnable mainTask;
    private final Set<UUID> attackers = new HashSet<>();
    private final Random random = new Random();
    
    private final String bossId;
    private boolean invulnerable = false;
    private boolean meleePhase = false;
    private final List<BlockDisplay> orbitalShields = new ArrayList<>();
    
    public GlacialBonebreakerBoss(Stray boss, JavaPlugin plugin, boolean isNewSpawn) {
        this.boss = boss;
        this.plugin = plugin;
        this.bossId = boss.getUniqueId().toString();
        
        EntityUtils.setCustomName(boss, "<#00FFFF><b>Quebrantahuesos Glacial</b></#00FFFF>", true);
        boss.leaveVehicle();
        boss.getPassengers().forEach(Entity::remove);
        EntityUtils.setColoredGlowing(boss, NamedTextColor.AQUA);

        // Attributes
        EntityUtils.trySetAttribute(boss, Attribute.MAX_HEALTH, 1500.0);
        EntityUtils.trySetAttribute(boss, Attribute.ARMOR, 80.0);
        EntityUtils.trySetAttribute(boss, Attribute.ATTACK_DAMAGE, 30.0);
        EntityUtils.trySetAttribute(boss, Attribute.FOLLOW_RANGE, 100.0);
        EntityUtils.trySetAttribute(boss, Attribute.KNOCKBACK_RESISTANCE, 1.0);
        EntityUtils.trySetAttribute(boss, Attribute.SCALE, 8.0);

        boss.setHealth(boss.getAttribute(Attribute.MAX_HEALTH) != null ? boss.getAttribute(Attribute.MAX_HEALTH).getValue() : 1500.0);

        // Gear (Netherite Dragonslayer)
        EntityEquipment eq = boss.getEquipment();
        if (eq != null) {
            Key modelKey = Key.key("panita", "dragonslayer");
            Material[] pieces = {Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS};
            
            ItemStack helmet = new ItemStack(pieces[0]);
            Equippable eqComp = helmet.getData(DataComponentTypes.EQUIPPABLE);
            if (eqComp != null) helmet.setData(DataComponentTypes.EQUIPPABLE, eqComp.toBuilder().assetId(modelKey).build());
            eq.setHelmet(helmet);
            eq.setHelmetDropChance(0.0f);
            
            ItemStack chest = new ItemStack(pieces[1]);
            eqComp = chest.getData(DataComponentTypes.EQUIPPABLE);
            if (eqComp != null) chest.setData(DataComponentTypes.EQUIPPABLE, eqComp.toBuilder().assetId(modelKey).build());
            eq.setChestplate(chest);
            eq.setChestplateDropChance(0.0f);
            
            ItemStack legs = new ItemStack(pieces[2]);
            eqComp = legs.getData(DataComponentTypes.EQUIPPABLE);
            if (eqComp != null) legs.setData(DataComponentTypes.EQUIPPABLE, eqComp.toBuilder().assetId(modelKey).build());
            eq.setLeggings(legs);
            eq.setLeggingsDropChance(0.0f);
            
            ItemStack boots = new ItemStack(pieces[3]);
            eqComp = boots.getData(DataComponentTypes.EQUIPPABLE);
            if (eqComp != null) boots.setData(DataComponentTypes.EQUIPPABLE, eqComp.toBuilder().assetId(modelKey).build());
            eq.setBoots(boots);
            eq.setBootsDropChance(0.0f);
            
            eq.setItemInMainHand(new ItemStack(Material.BOW));
            eq.setItemInMainHandDropChance(0.0f);
        }

        if (isNewSpawn) {
            Messenger.prefixedBroadcast("<#00FFFF>❄ Una tormenta eterna ha llegado. El Quebrantahuesos Glacial ha despertado... ("
                + boss.getLocation().getBlockX() + ", " + boss.getLocation().getBlockY() + ", " + boss.getLocation().getBlockZ() + ")</#00FFFF>");
            for (Player p : Bukkit.getOnlinePlayers()) {
                SoundUtils.play(p, "entity.wither.spawn", 1, 0.5f);
            }
        }

        // Main Task (1 tick)
        this.mainTask = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    handleDeath();
                    return;
                }
                
                float progress = (float) (boss.getHealth() / (boss.getAttribute(Attribute.MAX_HEALTH) != null ? boss.getAttribute(Attribute.MAX_HEALTH).getValue() : 1500.0));
                progress = Math.max(0.0f, Math.min(1.0f, progress));
                
                List<Player> nearby = getNearbyPlayers(100);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (nearby.contains(p) && PlayerUtils.isSurvival(p)) {
                        Messenger.showBossBar(p, bossId, "<#00FFFF><b>Quebrantahuesos Glacial</b></#00FFFF>", BossBar.Color.BLUE, BossBar.Overlay.NOTCHED_20, progress);
                    } else {
                        Messenger.hideBossBar(p, bossId);
                    }
                }
                
                // Absolute Zero Aura
                if (ticks % 10 == 0) {
                    checkAbsoluteZero(getNearbyPlayers(50));
                }

                // Dynamic Aggro Switch
                if (ticks % 100 == 0) { // Every 5 seconds
                    List<Player> targets = getNearbyPlayers(40);
                    if (!targets.isEmpty()) {
                        Player newTarget = targets.get(random.nextInt(targets.size()));
                        boss.setTarget(newTarget);
                    }
                }

                // Orbital Shields
                if (invulnerable && !orbitalShields.isEmpty()) {
                    orbitalShields.removeIf(shield -> shield.isDead() || !shield.isValid());
                    if (orbitalShields.isEmpty()) {
                        invulnerable = false;
                        boss.getWorld().playSound(boss.getLocation(), Sound.BLOCK_GLASS_BREAK, 2.0f, 0.5f);
                    } else {
                        double angleStep = (2 * Math.PI) / orbitalShields.size();
                        double angleOffset = (ticks * 0.03) % (2 * Math.PI);
                        for (int i = 0; i < orbitalShields.size(); i++) {
                            BlockDisplay shield = orbitalShields.get(i);
                            double angle = angleOffset + (i * angleStep);
                            double x = Math.cos(angle) * 8.0;
                            double z = Math.sin(angle) * 8.0;
                            
                            Location dest = boss.getLocation().add(x, boss.getHeight() / 2, z);
                            shield.teleport(dest);
                        }
                    }
                }

                ticks++;
            }
        };
        this.mainTask.runTaskTimer(plugin, 1L, 1L);

        scheduleAttackTask();
        schedulePhaseChange();
    }
    
    private void scheduleAttackTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) return;
                
                List<Player> targets = getNearbyPlayers(50);
                if (!targets.isEmpty() && !invulnerable) {
                    executeRandomAttack(targets);
                }
                
                int nextCooldown = 20 * (15 + random.nextInt(46)); // 15 to 60 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        scheduleAttackTask();
                    }
                }.runTaskLater(plugin, nextCooldown);
            }
        }.runTaskLater(plugin, 20 * 10);
    }
    
    private void schedulePhaseChange() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) return;
                
                meleePhase = !meleePhase;
                EntityEquipment eq = boss.getEquipment();
                if (meleePhase) {
                    alert("¡Cambio de Fase: Cuerpo a Cuerpo!");
                    ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
                    sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
                    if (eq != null) eq.setItemInMainHand(sword);
                } else {
                    alert("¡Cambio de Fase: Rango!");
                    if (eq != null) eq.setItemInMainHand(new ItemStack(Material.BOW));
                }
                
                schedulePhaseChange();
            }
        }.runTaskLater(plugin, 20L * (60 + random.nextInt(121))); // 60 to 180 seconds
    }

    private List<Player> getNearbyPlayers(double radius) {
        List<Player> list = new ArrayList<>();
        for (Entity e : boss.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Player p && PlayerUtils.isSurvival(p)) {
                list.add(p);
            }
        }
        return list;
    }
    
    private void checkAbsoluteZero(List<Player> players) {
        if (players.size() < 2) return;
        
        Set<Player> punished = new HashSet<>();
        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                Player p1 = players.get(i);
                Player p2 = players.get(j);
                if (p1.getLocation().distance(p2.getLocation()) < 5.0) {
                    punished.add(p1);
                    punished.add(p2);
                }
            }
        }
        
        for (Player p : punished) {
            p.setFreezeTicks(Math.min(p.getMaxFreezeTicks(), p.getFreezeTicks() + 40));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, false, false));
        }
    }

    public void fireHomingToAll() {
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_SNOW_GOLEM_SHOOT, 1.0f, 0.5f);
        List<Player> targets = getNearbyPlayers(40);
        for (Player target : targets) {
            Snowball projectile = boss.launchProjectile(Snowball.class);
            if (projectile == null) continue;
            projectile.getPersistentDataContainer().set(GlacialBonebreakerMechanic.PROJECTILE_KEY, PersistentDataType.BYTE, (byte) 1);
            
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (projectile.isDead() || !projectile.isValid() || ticks > 100 || !target.isOnline()) {
                        if (projectile.isValid()) projectile.remove();
                        this.cancel();
                        return;
                    }
                    
                    Vector dir = target.getLocation().add(0, target.getHeight() / 2, 0).toVector().subtract(projectile.getLocation().toVector()).normalize();
                    projectile.setVelocity(dir.multiply(1.2));
                    projectile.getWorld().spawnParticle(Particle.SNOWFLAKE, projectile.getLocation(), 2, 0.1, 0.1, 0.1, 0);
                    
                    ticks++;
                }
            }.runTaskTimer(plugin, 1L, 1L);
        }
    }
    
    public void forceAttack(int attackId) {
        List<Player> targets = getNearbyPlayers(50);
        if (!targets.isEmpty()) {
            if (meleePhase) {
                if (attackId < 0 || attackId >= 4) attackId = random.nextInt(4);
                executeSpecificMeleeAttack(targets, attackId);
            } else {
                if (attackId < 0 || attackId >= 11) attackId = random.nextInt(11);
                executeSpecificRangedAttack(targets, attackId);
            }
        }
    }

    private void executeRandomAttack(List<Player> players) {
        if (meleePhase) {
            executeSpecificMeleeAttack(players, random.nextInt(4));
        } else {
            executeSpecificRangedAttack(players, random.nextInt(11));
        }
    }

    private void executeSpecificMeleeAttack(List<Player> players, int r) {
        switch (r) {
            case 0: executeRadialShockwave(); break;
            case 1: executeInertialCharge(players); break;
            case 2: executeFreezingSlash(players); break;
            case 3: executeWhiteShadowAssault(players); break;
        }
    }

    private void executeSpecificRangedAttack(List<Player> players, int r) {
        switch (r) {
            case 0: executeImpalerStalagmite(players); break;
            case 1: executeOrbitalShields(); break;
            case 2: executeMirageClones(); break;
            case 3: executeBlizzardWall(); break;
            case 4: executeZeroFrictionFloor(); break;
            case 5: executeFragileFloor(); break;
            case 6: executeBlizzardGhosts(); break;
            case 7: executeSnowMines(players); break;
            case 8: executeVortexCavalry(); break;
            case 9: executeTundraAmbush(players); break;
            case 10: executeStatusDebuffs(players); break;
        }
    }

    private void alert(String msg) {
        for (Player p : getNearbyPlayers(100)) {
            Messenger.prefixedSend(p, "<#FF5252>❄ <#00FFFF>" + msg + "</#00FFFF></#FF5252>");
            SoundUtils.play(p, "entity.wither.ambient", 1, 0.5f);
        }
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }
    
    private void executeRadialShockwave() {
        alert("Onda de Choque Radial");
        Location center = boss.getLocation().clone();
        center.setY(Math.floor(center.getY()));
        
        new BukkitRunnable() {
            int radius = 1;
            @Override
            public void run() {
                if (radius > 15 || boss.isDead()) {
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
                    bd.setBlock(Bukkit.createBlockData(Material.PACKED_ICE));
                    Transformation t = bd.getTransformation();
                    t.getScale().set(0.5f, 2.0f, 0.5f);
                    bd.setTransformation(t);
                    
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (bd.isValid()) {
                            bd.getWorld().spawnParticle(Particle.BLOCK, bd.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, Bukkit.createBlockData(Material.PACKED_ICE));
                            bd.remove();
                        }
                    }, 10L);
                }
                
                for (Player p : getNearbyPlayers(20)) {
                    if (p.getLocation().distance(center) <= radius && p.getLocation().distance(center) > radius - 1.5) {
                        if (p.getLocation().getY() - center.getY() < 2.0) {
                            p.damage(20.0, boss);
                            p.setVelocity(new Vector(0, 1.5, 0));
                        }
                    }
                }
                radius++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void executeInertialCharge(List<Player> players) {
        if (players.isEmpty()) return;
        alert("Carga Inercial");
        Player target = players.get(random.nextInt(players.size()));
        
        Vector dir = target.getLocation().toVector().subtract(boss.getLocation().toVector());
        dir.setY(0).normalize();
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 20 || boss.isDead()) {
                    this.cancel();
                    return;
                }
                boss.setVelocity(dir.clone().multiply(2.5));
                boss.getWorld().spawnParticle(Particle.SNOWFLAKE, boss.getLocation(), 20, 1, 1, 1, 0.1);
                
                for (Player p : getNearbyPlayers(3)) {
                    p.damage(25.0, boss);
                    p.setVelocity(dir.clone().multiply(3.0).setY(1.0));
                }
                
                if (ticks > 2 && boss.getVelocity().lengthSquared() < 0.1) {
                    // Hit a wall
                    boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 5));
                    boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 2.0f, 0.5f);
                    this.cancel();
                    return;
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeFreezingSlash(List<Player> players) {
        if (players.isEmpty()) return;
        alert("Tajo Congelante");
        Player target = players.get(random.nextInt(players.size()));
        Vector dir = target.getLocation().toVector().subtract(boss.getLocation().toVector()).setY(0).normalize();
        Location current = boss.getLocation().clone();
        
        new BukkitRunnable() {
            int steps = 0;
            @Override
            public void run() {
                if (steps > 20 || boss.isDead()) {
                    this.cancel();
                    return;
                }
                current.add(dir.clone().multiply(1.5));
                current.setY(current.getWorld().getHighestBlockYAt(current) + 1);
                current.getWorld().spawnEntity(current, EntityType.EVOKER_FANGS);
                current.getWorld().spawnParticle(Particle.SNOWFLAKE, current, 10, 0.5, 0.5, 0.5, 0.1);
                steps++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeWhiteShadowAssault(List<Player> players) {
        if (players.isEmpty()) return;
        alert("Asalto de Sombra Blanca");
        boss.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, false, false));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 2, false, false));
        
        new BukkitRunnable() {
            int strikes = 0;
            @Override
            public void run() {
                if (strikes > 3 || boss.isDead() || players.isEmpty()) {
                    boss.removePotionEffect(PotionEffectType.INVISIBILITY);
                    this.cancel();
                    return;
                }
                Player p = players.get(random.nextInt(players.size()));
                if (p.isOnline()) {
                    Vector look = p.getLocation().getDirection().setY(0).normalize();
                    Location behind = p.getLocation().subtract(look.multiply(2));
                    boss.teleport(behind);
                    boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
                    boss.getWorld().spawnParticle(Particle.SNOWFLAKE, boss.getLocation(), 50, 1, 1, 1, 0.1);
                    p.damage(20.0, boss);
                }
                strikes++;
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    private void executeImpalerStalagmite(List<Player> players) {
        if (players.isEmpty()) return;
        alert("¡Bosque de Estalagmitas Empaladoras!");
        Player target = players.get(random.nextInt(players.size()));
        Location center = target.getLocation().clone();
        
        List<Location> stalagmiteLocs = new ArrayList<>();
        stalagmiteLocs.add(center.clone());
        
        for (int i = 0; i < 15; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 10; // 5 block radius (10x10 area)
            double offsetZ = (random.nextDouble() - 0.5) * 10;
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
                if (ticks < 40) {
                    for (Location loc : stalagmiteLocs) {
                        loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 5, 0.5, 0.1, 0.5, 0);
                        loc.getWorld().spawnParticle(Particle.BLOCK, loc, 2, 0.3, 0.1, 0.3, Bukkit.createBlockData(Material.ICE));
                    }
                } else if (ticks == 40) {
                    center.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 3.0f, 0.5f);
                    for (Location loc : stalagmiteLocs) {
                        int height = 3 + random.nextInt(4); // 3 to 6
                        float baseScale = 0.8f + random.nextFloat() * 0.7f; // 0.8 to 1.5
                        
                        for (int y = 0; y < height; y++) {
                            BlockDisplay bd = (BlockDisplay) loc.getWorld().spawnEntity(loc.clone().add(0, y, 0), EntityType.BLOCK_DISPLAY);
                            Material mat = random.nextBoolean() ? Material.BLUE_ICE : Material.PACKED_ICE;
                            bd.setBlock(Bukkit.createBlockData(mat));
                            
                            Transformation t = bd.getTransformation();
                            float scaleX = baseScale * (1.0f - ((float)y / height));
                            float scaleZ = baseScale * (1.0f - ((float)y / height));
                            t.getScale().set(scaleX, 1.0f, scaleZ);
                            t.getTranslation().set((1.0f - scaleX) / 2.0f, 0.0f, (1.0f - scaleZ) / 2.0f);
                            bd.setTransformation(t);
                            
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                if (bd.isValid()) {
                                    bd.getWorld().spawnParticle(Particle.BLOCK, bd.getLocation(), 10, 0.5, 0.5, 0.5, Bukkit.createBlockData(mat));
                                    bd.remove();
                                }
                            }, 60L + random.nextInt(20));
                        }
                    }
                    
                    for (Player p : getNearbyPlayers(50)) {
                        for (Location loc : stalagmiteLocs) {
                            if (p.getWorld().equals(loc.getWorld()) && p.getLocation().distance(loc) < 2.5) {
                                p.damage(25.0, boss);
                                p.setVelocity(new Vector(0, 1.5, 0));
                                p.setFreezeTicks(Math.min(p.getMaxFreezeTicks(), p.getFreezeTicks() + 100));
                                break;
                            }
                        }
                    }
                } else if (ticks > 45) {
                    this.cancel();
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeOrbitalShields() {
        alert("Escudo de Fractales Orbitales");
        invulnerable = true;
        for (int i = 0; i < 4; i++) {
            BlockDisplay bd = (BlockDisplay) boss.getWorld().spawnEntity(boss.getLocation(), EntityType.BLOCK_DISPLAY);
            bd.setBlock(Bukkit.createBlockData(Material.BLUE_ICE));
            bd.setTeleportDuration(3);
            
            Transformation t = bd.getTransformation();
            t.getScale().set(2.0f, 2.0f, 2.0f);
            bd.setTransformation(t);
            
            Slime slime = (Slime) boss.getWorld().spawnEntity(boss.getLocation(), EntityType.SLIME);
            slime.setSize(4);
            slime.setInvisible(true);
            slime.setAI(false);
            slime.setGravity(false);
            slime.setSilent(true);
            slime.getPersistentDataContainer().set(GlacialBonebreakerMechanic.SHIELD_KEY, PersistentDataType.BYTE, (byte) 1);
            bd.addPassenger(slime);
            
            orbitalShields.add(bd);
        }
    }

    private void executeMirageClones() {
        alert("Clones Espejismo");
        for (int i = 0; i < 3; i++) {
            Stray clone = (Stray) EntityUtils.spawnNatural(boss.getLocation(), EntityType.STRAY);
            if (clone != null) {
                clone.getPersistentDataContainer().set(GlacialBonebreakerMechanic.FAKE_CLONE_KEY, PersistentDataType.BYTE, (byte) 1);
                EntityUtils.setCustomName(clone, boss.getCustomName());
                if (clone.getEquipment() != null) {
                    clone.getEquipment().setArmorContents(boss.getEquipment().getArmorContents());
                    clone.getEquipment().setItemInMainHand(boss.getEquipment().getItemInMainHand());
                }
                Vector randDir = new Vector(random.nextDouble() - 0.5, 0.5, random.nextDouble() - 0.5).normalize();
                clone.setVelocity(randDir);
            }
        }
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_SKELETON_AMBIENT, 2.0f, 0.5f);
        boss.getWorld().spawnParticle(Particle.SNOWFLAKE, boss.getLocation(), 100, 1, 1, 1, 0.1);
        boss.setVelocity(new Vector(random.nextDouble() - 0.5, 0.5, random.nextDouble() - 0.5).normalize());
    }

    private void executeBlizzardWall() {
        alert("Muro de Ventisca Rotatorio");
        Location center = boss.getLocation().clone();
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 200 || boss.isDead()) {
                    this.cancel();
                    return;
                }
                double angle = ticks * 0.1;
                for (int d = 0; d < 2; d++) {
                    double offset = d * Math.PI;
                    for (double r = 1; r < 20; r += 0.5) {
                        double x = Math.cos(angle + offset) * r;
                        double z = Math.sin(angle + offset) * r;
                        Location pLoc = center.clone().add(x, 1, z);
                        pLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, pLoc, 5, 0.2, 1.0, 0.2, 0);
                        
                        for (Player p : getNearbyPlayers(25)) {
                            if (p.getLocation().distance(pLoc) < 1.0) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 1));
                                p.setVelocity(new Vector(x, 0.5, z).normalize().multiply(1.5));
                            }
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeZeroFrictionFloor() {
        alert("Suelo de Fricción Cero");
        Map<Location, BlockData> old = replaceFloor(Material.BLUE_ICE);
        Bukkit.getScheduler().runTaskLater(plugin, () -> restoreFloor(old), 10 * 20L);
    }

    private void executeFragileFloor() {
        alert("Suelo Quebradizo");
        Map<Location, BlockData> old = replaceFloor(Material.POWDER_SNOW);
        Bukkit.getScheduler().runTaskLater(plugin, () -> restoreFloor(old), 10 * 20L);
    }

    private Map<Location, BlockData> replaceFloor(Material newMat) {
        Map<Location, BlockData> old = new HashMap<>();
        Location center = boss.getLocation();
        for (int x = -15; x <= 15; x++) {
            for (int z = -15; z <= 15; z++) {
                if (random.nextDouble() > 0.6) continue;
                Location loc = center.clone().add(x, -1, z);
                Block b = loc.getBlock();
                if (b.getType().isSolid() && b.getType() != Material.BEDROCK && b.getType() != Material.OBSIDIAN) {
                    old.put(loc, b.getBlockData().clone());
                    b.setType(newMat);
                }
            }
        }
        return old;
    }

    private void restoreFloor(Map<Location, BlockData> old) {
        for (Map.Entry<Location, BlockData> entry : old.entrySet()) {
            entry.getKey().getBlock().setBlockData(entry.getValue());
        }
    }

    private void executeBlizzardGhosts() {
        alert("Fantasmas de la Ventisca");
        for (int i = 0; i < 3; i++) {
            Vex vex = (Vex) EntityUtils.spawnNatural(boss.getLocation().add(0, 2, 0), EntityType.VEX);
            if (vex != null) {
                vex.getPersistentDataContainer().set(GlacialBonebreakerMechanic.MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                if (vex.getEquipment() != null) {
                    vex.getEquipment().setHelmet(new ItemStack(Material.BLUE_ICE));
                    vex.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
                }
            }
        }
    }

    private void executeSnowMines(List<Player> players) {
        alert("Minas de Nieve");
        for (Player p : players) {
            Location loc = p.getLocation().add(random.nextInt(10) - 5, 0, random.nextInt(10) - 5);
            Snowman sm = (Snowman) EntityUtils.spawnNatural(loc, EntityType.SNOW_GOLEM);
            if (sm != null) {
                sm.setAI(false); // Static
                sm.getPersistentDataContainer().set(GlacialBonebreakerMechanic.MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                Creeper creeper = (Creeper) EntityUtils.spawnNatural(loc, EntityType.CREEPER);
                if (creeper != null) {
                    creeper.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, false, false));
                    creeper.getPersistentDataContainer().set(GlacialBonebreakerMechanic.MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                    sm.addPassenger(creeper);
                }
            }
        }
    }

    private void executeVortexCavalry() {
        alert("Caballería del Vórtice");
        for (int i = 0; i < 3; i++) {
            Location loc = boss.getLocation().add(random.nextInt(10) - 5, 10, random.nextInt(10) - 5);
            Phantom phantom = (Phantom) EntityUtils.spawnNatural(loc, EntityType.PHANTOM);
            if (phantom != null) {
                phantom.getPersistentDataContainer().set(GlacialBonebreakerMechanic.MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                Stray stray = (Stray) EntityUtils.spawnNatural(loc, EntityType.STRAY);
                if (stray != null) {
                    stray.getPersistentDataContainer().set(GlacialBonebreakerMechanic.MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                    phantom.addPassenger(stray);
                }
            }
        }
    }

    private void executeTundraAmbush(List<Player> players) {
        alert("Emboscada de Tundra");
        for (Player p : players) {
            if (random.nextBoolean()) {
                Vindicator vindicator = (Vindicator) EntityUtils.spawnNatural(p.getLocation(), EntityType.VINDICATOR);
                if (vindicator != null) {
                    vindicator.getPersistentDataContainer().set(GlacialBonebreakerMechanic.MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                    if (vindicator.getEquipment() != null) {
                        vindicator.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_AXE));
                    }
                }
            }
        }
    }

    private void executeStatusDebuffs(List<Player> players) {
        if (random.nextBoolean()) {
            alert("Apagón de la Tundra");
            for (Player p : players) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
                SoundUtils.play(p, "entity.warden.roar", 1, 0.5f);
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!boss.isDead() && !meleePhase) {
                    fireHomingToAll();
                }
            }, 40L);
        } else {
            if (random.nextBoolean()) {
                alert("Hipotermia");
                Player target = players.get(random.nextInt(players.size()));
                target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                target.setFreezeTicks(Math.min(target.getMaxFreezeTicks(), target.getFreezeTicks() + 100));
            } else {
                alert("Cuerdas Congeladas");
                for (Player p : players) {
                    p.getPersistentDataContainer().set(GlacialBonebreakerMechanic.FROZEN_BOW_KEY, PersistentDataType.BYTE, (byte) 1);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (p.isOnline()) p.getPersistentDataContainer().remove(GlacialBonebreakerMechanic.FROZEN_BOW_KEY);
                    }, 200L); // 10 seconds
                }
            }
        }
    }

    public void addAttacker(UUID uuid) {
        attackers.add(uuid);
    }

    public void handleDeath() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Messenger.hideBossBar(p, bossId);
        }
        if (mainTask != null) mainTask.cancel();
        
        for (BlockDisplay bd : orbitalShields) {
            if (bd.isValid()) bd.remove();
        }
        orbitalShields.clear();
        
        for (UUID uuid : attackers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                giveRewards(p);
            }
        }
    }

    private void giveOrDropItem(Player p, ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        Map<Integer, ItemStack> leftover = p.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) p.getWorld().dropItem(p.getLocation(), drop);
        }
    }
    
    private void giveOrDropItems(Player p, Material material, int amount) {
        int maxStack = Math.min(64, Math.max(1, material.getMaxStackSize()));
        while (amount > 0) {
            int currentAmount = Math.min(amount, maxStack);
            giveOrDropItem(p, new ItemStack(material, currentAmount));
            amount -= currentAmount;
        }
    }

    private void giveRewards(Player p) {
        int scrapCount = 8 + random.nextInt(24);
        giveOrDropItems(p, Material.NETHERITE_SCRAP, scrapCount);
        
        List<Runnable> options = new ArrayList<>();
        options.add(() -> giveOrDropItems(p, Material.GOLDEN_APPLE, 1 + random.nextInt(31)));
        options.add(() -> giveOrDropItems(p, Material.ENCHANTED_GOLDEN_APPLE, 4));
        options.add(() -> p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0)));
        options.add(() -> p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 12 * 60 * 60 * 20, 0)));
        options.add(() -> p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1)));
        options.add(() -> p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 4 * 60 * 60 * 20, 2)));
        options.add(() -> p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 8 * 60 * 60 * 20, 2)));
        options.add(() -> giveOrDropItems(p, Material.TOTEM_OF_UNDYING, 1 + random.nextInt(6)));
        options.add(() -> giveOrDropItems(p, Material.DIAMOND, 25 + random.nextInt(50)));
        options.add(() -> giveOrDropItems(p, Material.GOLDEN_CARROT, 128 + random.nextInt(385)));
        options.add(() -> p.giveExpLevels(50 + random.nextInt(99)));
        options.add(() -> giveOrDropItems(p, Material.RAW_GOLD, 48 + random.nextInt(151)));
        options.add(() -> {
            Material[] shulkers = {Material.SHULKER_BOX, Material.RED_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BLACK_SHULKER_BOX};
            giveOrDropItems(p, shulkers[random.nextInt(shulkers.length)], 1);
        });
        options.add(() -> {
            String[] customItems = {"tactic_bow", "tezzlar_heart", "bee_totem", "chicken_totem", "ghast_totem", "golden_totem", "life_save", "memory_evoker", "sniffer_totem", "sulfur_totem", "turtle_totem"};
            String chosen = customItems[random.nextInt(customItems.length)];
            ItemStack item = CustomItemManager.getItem(chosen);
            if (item != null) giveOrDropItem(p, item);
        });
        options.add(() -> giveOrDropItems(p, Material.EMERALD, 128 + random.nextInt(385)));
        
        Collections.shuffle(options);
        options.get(0).run();
        options.get(1).run();
        options.get(2).run();
        
        Messenger.prefixedSend(p, "<#00FFFF>¡Has recibido recompensas glaciales por derrotar al Quebrantahuesos!</#00FFFF>");
        SoundUtils.play(p, "entity.player.levelup", 1, 2);
    }
}

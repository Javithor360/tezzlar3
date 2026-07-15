package com.panita.tezzlar3.difficulty.mechanics;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
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
import com.panita.tezzlar3.core.util.MobGearUtils;
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
    private boolean blizzardActive = false;
    private boolean shield1000 = false;
    private boolean shield500 = false;
    private boolean shield30 = false;
    private boolean isTeleporting = false;
    private final Map<BlockDisplay, double[]> orbitalShields = new HashMap<>();
    
    public GlacialBonebreakerBoss(Stray boss, JavaPlugin plugin, boolean isNewSpawn) {
        this.boss = boss;
        this.plugin = plugin;
        this.bossId = boss.getUniqueId().toString();
        
        EntityUtils.setCustomName(boss, "<#00FFFF><b>Quebrantahuesos Glacial</b></#00FFFF>", true);
        boss.leaveVehicle();
        boss.getPassengers().forEach(Entity::remove);
        EntityUtils.setColoredGlowing(boss, NamedTextColor.AQUA);

        // Attributes
        boss.setRemoveWhenFarAway(false);
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
                
                double currentHp = boss.getHealth();
                float progress = (float) (currentHp / (boss.getAttribute(Attribute.MAX_HEALTH) != null ? boss.getAttribute(Attribute.MAX_HEALTH).getValue() : 1500.0));
                progress = Math.max(0.0f, Math.min(1.0f, progress));
                
                if (currentHp <= 1000 && !shield1000) {
                    shield1000 = true;
                    executeOrbitalShields();
                }
                if (currentHp <= 500 && !shield500) {
                    shield500 = true;
                    executeOrbitalShields();
                }
                if (currentHp <= 30 && !shield30) {
                    shield30 = true;
                    executeOrbitalShields();
                }
                
                Block bossBlock = boss.getLocation().getBlock();
                if (bossBlock.getType() == Material.COBWEB) {
                    bossBlock.setType(Material.AIR);
                    boss.getWorld().playSound(bossBlock.getLocation(), Sound.BLOCK_COBWEB_BREAK, 1.0f, 1.0f);
                }
                if (!isTeleporting && (bossBlock.getType() == Material.LAVA || bossBlock.getRelative(BlockFace.DOWN).getType() == Material.LAVA)) {
                    List<Player> tpNearby = getNearbyPlayers(100);
                    if (!tpNearby.isEmpty()) {
                        Player tpTarget = tpNearby.get(random.nextInt(tpNearby.size()));
                        isTeleporting = true;
                        delayedTeleport(tpTarget.getLocation().add(0, 1, 0), () -> isTeleporting = false);
                    }
                }
                
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
                    orbitalShields.keySet().removeIf(shield -> shield.isDead() || !shield.isValid());
                    if (orbitalShields.isEmpty()) {
                        invulnerable = false;
                        boss.getWorld().playSound(boss.getLocation(), Sound.BLOCK_GLASS_BREAK, 2.0f, 0.5f);
                    } else {
                        for (Map.Entry<BlockDisplay, double[]> entry : orbitalShields.entrySet()) {
                            BlockDisplay shield = entry.getKey();
                            double[] data = entry.getValue();
                            double radius = data[0];
                            double height = data[1];
                            double speed = data[2];
                            double startAngle = data[3];
                            
                            double angle = startAngle + (ticks * speed);
                            double x = Math.cos(angle) * radius;
                            double z = Math.sin(angle) * radius;
                            
                            Location dest = boss.getLocation().add(x, height, z);
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
    
    public boolean isMeleePhase() { return meleePhase; }
    
    public boolean isBlizzardActive() { return blizzardActive; }

    public Location getSpawnLocation() { return boss.getLocation(); }
    
    private void scheduleAttackTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) return;
                
                List<Player> targets = getNearbyPlayers(50);
                if (!targets.isEmpty() && !invulnerable && !blizzardActive) {
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

    private void delayedTeleport(Location dest, Runnable afterTp) {
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 10, false, false));
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1.5f, 0.5f);
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    this.cancel();
                    return;
                }
                if (ticks >= 40) { // 2 seconds reaction time
                    boss.teleport(dest);
                    boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
                    boss.getWorld().spawnParticle(Particle.EXPLOSION, boss.getLocation(), 1);
                    if (afterTp != null) afterTp.run();
                    this.cancel();
                } else {
                    dest.getWorld().spawnParticle(Particle.REVERSE_PORTAL, dest.clone().add(0, 1, 0), 30, 0.5, 1.0, 0.5, 0.1);
                    dest.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, dest.clone().add(0, 1, 0), 10, 0.5, 1.0, 0.5, 0.05);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
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
        
        List<LivingEntity> targets = new ArrayList<>(getNearbyPlayers(40));
        
        if (boss.getTarget() != null && !(boss.getTarget() instanceof Player) && !targets.contains(boss.getTarget())) {
            targets.add(boss.getTarget());
        }
        
        for (Entity e : boss.getNearbyEntities(40, 40, 40)) {
            if (e instanceof Mob mob) {
                if (mob.getTarget() != null && mob.getTarget().equals(boss) && !targets.contains(mob)) {
                    targets.add(mob);
                }
            }
        }
        
        for (LivingEntity target : targets) {
            Snowball projectile = boss.launchProjectile(Snowball.class);
            if (projectile == null) continue;
            projectile.getPersistentDataContainer().set(GlacialBonebreakerMechanic.PROJECTILE_KEY, PersistentDataType.BYTE, (byte) 1);
            
            ItemDisplay id = (ItemDisplay) boss.getWorld().spawnEntity(projectile.getLocation(), EntityType.ITEM_DISPLAY);
            id.setItemStack(new ItemStack(Material.SNOWBALL));
            id.setBillboard(Display.Billboard.CENTER);
            Transformation t = id.getTransformation();
            t.getScale().set(2.2f, 2.2f, 2.2f);
            id.setTransformation(t);
            projectile.addPassenger(id);
            projectile.setItem(new ItemStack(Material.AIR));
            
            Slime hitbox = (Slime) boss.getWorld().spawnEntity(projectile.getLocation(), EntityType.SLIME);
            hitbox.setSize(2);
            hitbox.setInvisible(true);
            hitbox.setAI(false);
            hitbox.setGravity(false);
            hitbox.setSilent(true);
            hitbox.getPersistentDataContainer().set(GlacialBonebreakerMechanic.PROJECTILE_KEY, PersistentDataType.BYTE, (byte) 1);
            projectile.addPassenger(hitbox);
            
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (projectile.isDead() || !projectile.isValid() || ticks > 150 || !target.isValid() || target.isDead()) {
                        projectile.remove();
                        id.remove();
                        hitbox.remove();
                        this.cancel();
                        return;
                    }
                    
                    if (projectile.getShooter() == boss) {
                        if (projectile.getLocation().distanceSquared(target.getLocation()) > 25.0) { // Stops tracking at 5 blocks to allow dodging
                            Vector dir = target.getLocation().add(0, target.getHeight() / 2, 0).toVector().subtract(projectile.getLocation().toVector()).normalize();
                            projectile.setVelocity(dir.multiply(0.867)); // another 15% slower
                        }
                    }
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
                if (attackId < 0 || attackId >= 5) attackId = random.nextInt(5);
                executeSpecificMeleeAttack(targets, attackId);
            } else {
                if (attackId < 0 || attackId >= 11) attackId = random.nextInt(11);
                executeSpecificRangedAttack(targets, attackId);
            }
        }
    }

    private void executeRandomAttack(List<Player> players) {
        if (meleePhase) {
            executeSpecificMeleeAttack(players, random.nextInt(5));
        } else {
            executeSpecificRangedAttack(players, random.nextInt(11));
        }
    }

    private void executeSpecificMeleeAttack(List<Player> players, int r) {
        switch (r) {
            case 0: executeRadialShockwave(players); break;
            case 1: executeInertialCharge(players); break;
            case 2: executeFreezingSlash(players); break;
            case 3: executeWhiteShadowAssault(players); break;
            case 4: executeSnowballFight(players); break;
        }
    }

    private void executeSpecificRangedAttack(List<Player> players, int r) {
        switch (r) {
            case 0: executeImpalerStalagmite(players); break;
            case 1: executeOrbitalShields(); break;
            case 2: executeMirageClones(); break;
            case 3: executeBlizzardWall(); break;
            case 4: executeZeroFrictionFloor(players); break;
            case 5: executeFragileFloor(players); break;
            case 6: executeBlizzardGhosts(players); break;
            case 7: executeSnowMines(players); break;
            case 8: executeVortexCavalry(players); break;
            case 9: executeTundraAmbush(players); break;
            case 10: executeStatusDebuffs(players); break;
        }
    }

    private void alert(String msg) {
        for (Player p : getNearbyPlayers(100)) {
            if (msg.startsWith("¡Cambio")) {
                Messenger.prefixedSend(p, "<#FF5252>❄ <#00FFFF>" + msg + "</#00FFFF></#FF5252>");
            } else {
                Messenger.prefixedSend(p, "<#FF5252>❄ <#00FFFF>El Quebrantahuesos Glacial está lanzando " + msg + "</#00FFFF></#FF5252>");
            }
            SoundUtils.play(p, "entity.wither.ambient", 1, 0.5f);
        }
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }
    
    private void executeRadialShockwave(List<Player> players) {
        alert("una Onda de Choque Radial");
        if (players.isEmpty()) return;
        
        int totalTimes = 2 + random.nextInt(4); // 2 to 5
        int[] count = {0};
        
        Runnable[] taskRef = new Runnable[1];
        taskRef[0] = new Runnable() {
            @Override
            public void run() {
                if (count[0] >= totalTimes || boss.isDead() || !boss.isValid()) return;
                
                Player target = players.get(random.nextInt(players.size()));
                isTeleporting = true;
                
                delayedTeleport(target.getLocation(), () -> {
                    isTeleporting = false;
                    if (boss.isDead() || !boss.isValid()) return;
                    
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
                    
                    count[0]++;
                    if (count[0] < totalTimes) {
                        Bukkit.getScheduler().runTaskLater(plugin, taskRef[0], 20L + random.nextInt(41)); // 1 to 3 seconds
                    }
                });
            }
        };
        
        Bukkit.getScheduler().runTaskLater(plugin, taskRef[0], 20L + random.nextInt(41));
    }

    private void executeInertialCharge(List<Player> players) {
        if (players.isEmpty()) return;
        alert("una Carga Inercial");
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    this.cancel();
                    return;
                }
                
                if (ticks >= 160) { // 8 seconds
                    this.cancel();
                    
                    Player target = players.get(random.nextInt(players.size()));
                    Vector dir = target.getLocation().toVector().subtract(boss.getLocation().toVector());
                    dir.setY(0).normalize();
                    
                    new BukkitRunnable() {
                        int dashTicks = 0;
                        @Override
                        public void run() {
                            if (dashTicks > 20 || boss.isDead() || !boss.isValid()) {
                                this.cancel();
                                return;
                            }
                            boss.setVelocity(dir.clone().multiply(2.5));
                            boss.getWorld().spawnParticle(Particle.SNOWFLAKE, boss.getLocation(), 20, 1, 1, 1, 0.1);
                            
                            for (Player p : getNearbyPlayers(3)) {
                                p.damage(25.0, boss);
                                Vector pushDir = p.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize().multiply(3.0).setY(1.0);
                                p.setVelocity(pushDir);
                            }
                            
                            if (dashTicks > 2 && (boss.getVelocity().lengthSquared() < 0.1 || boss.getLocation().add(boss.getVelocity().normalize().multiply(1.5)).getBlock().getType().isSolid())) {
                                boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 5));
                                boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 2.0f, 0.5f);
                                this.cancel();
                                return;
                            }
                            dashTicks++;
                        }
                    }.runTaskTimer(plugin, 0L, 2L);
                    
                    return;
                }
                
                for (Player p : getNearbyPlayers(30)) {
                    Vector pull = boss.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.12).setY(0.05);
                    p.setVelocity(p.getVelocity().add(pull));
                }
                if (ticks % 20 == 0) {
                    boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeFreezingSlash(List<Player> players) {
        alert("un Tajo Congelante");
        if (players.isEmpty()) return;
        
        int[] count = {0};
        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (count[0] >= 3 || boss.isDead() || !boss.isValid()) {
                    return;
                }
                
                Player target = players.get(random.nextInt(players.size()));
                Location center = target.getLocation().clone();
                center.setY(Math.floor(center.getY()));
                boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.5f, 1.0f);
                
                new BukkitRunnable() {
                    int dist = 12;
                    @Override
                    public void run() {
                        if (dist < 0 || boss.isDead() || !boss.isValid()) {
                            this.cancel();
                            return;
                        }
                        
                        for (int i = 0; i < 8; i++) {
                            double angle = i * (Math.PI / 4);
                            double x = Math.cos(angle) * dist;
                            double z = Math.sin(angle) * dist;
                            Location loc = center.clone().add(x, 0, z);
                            loc.setY(loc.getWorld().getHighestBlockYAt(loc));
                            
                            loc.getWorld().spawnEntity(loc, EntityType.EVOKER_FANGS);
                            loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 10, 0.5, 0.5, 0.5, 0.1);
                        }
                        
                        dist -= 1;
                    }
                }.runTaskTimer(plugin, 0L, 2L);
                
                count[0]++;
                if (count[0] < 3) {
                    Bukkit.getScheduler().runTaskLater(plugin, this, 20L + random.nextInt(41)); // 1 to 3 sec delay
                }
            }
        };
        
        Bukkit.getScheduler().runTaskLater(plugin, task, 20L + random.nextInt(41));
    }

    private void executeWhiteShadowAssault(List<Player> players) {
        if (players.isEmpty()) return;
        alert("un Asalto de Sombra Blanca");
        boss.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 160, 0, false, false));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 2, false, false));
        
        int[] count = {0};
        Runnable[] taskRef = new Runnable[1];
        taskRef[0] = new Runnable() {
            @Override
            public void run() {
                if (count[0] >= 3 || boss.isDead() || players.isEmpty()) {
                    boss.removePotionEffect(PotionEffectType.INVISIBILITY);
                    return;
                }
                
                Player p = players.get(random.nextInt(players.size()));
                if (p.isOnline()) {
                    Vector look = p.getLocation().getDirection().setY(0).normalize();
                    Location behind = p.getLocation().subtract(look.multiply(2));
                    
                    isTeleporting = true;
                    delayedTeleport(behind, () -> {
                        isTeleporting = false;
                        if (boss.isDead() || !boss.isValid()) return;
                        boss.getWorld().spawnParticle(Particle.SNOWFLAKE, boss.getLocation(), 50, 1, 1, 1, 0.1);
                        p.damage(20.0, boss);
                        
                        count[0]++;
                        if (count[0] < 3) {
                            Bukkit.getScheduler().runTaskLater(plugin, taskRef[0], 20L + random.nextInt(41));
                        } else {
                            boss.removePotionEffect(PotionEffectType.INVISIBILITY);
                        }
                    });
                } else {
                    count[0]++;
                    if (count[0] < 3) Bukkit.getScheduler().runTaskLater(plugin, taskRef[0], 20L);
                }
            }
        };
        
        Bukkit.getScheduler().runTaskLater(plugin, taskRef[0], 20L + random.nextInt(41));
    }
    
    private void executeSnowballFight(List<Player> players) {
        alert("una Guerra de Nieve");
        if (players.isEmpty()) return;
        
        int totalTimes = 15 + random.nextInt(11); // 15 to 25 giant snowballs
        int[] count = {0};
        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (count[0] >= totalTimes || boss.isDead() || !boss.isValid()) return;
                
                Player target = players.get(random.nextInt(players.size()));
                
                double offsetX = (random.nextDouble() - 0.5) * 10.0; // -5 to 5
                double offsetZ = (random.nextDouble() - 0.5) * 10.0; // -5 to 5
                Location spawn = target.getLocation().add(offsetX, 15 + random.nextDouble() * 5, offsetZ);
                Vector dir = new Vector(0, -1.5, 0);
                
                Snowball snowball = (Snowball) spawn.getWorld().spawnEntity(spawn, EntityType.SNOWBALL);
                snowball.setVelocity(dir);
                snowball.setShooter(boss);
                snowball.getPersistentDataContainer().set(GlacialBonebreakerMechanic.GIANT_SNOWBALL_KEY, PersistentDataType.BYTE, (byte) 1);
                
                ItemDisplay display = (ItemDisplay) spawn.getWorld().spawnEntity(spawn, EntityType.ITEM_DISPLAY);
                display.setItemStack(new ItemStack(Material.SNOWBALL));
                Transformation t = display.getTransformation();
                t.getScale().set(4.0f, 4.0f, 4.0f);
                display.setTransformation(t);
                
                snowball.addPassenger(display);
                
                boss.getWorld().playSound(target.getLocation(), Sound.ENTITY_SNOW_GOLEM_SHOOT, 1.5f, 0.5f);
                
                count[0]++;
                if (count[0] < totalTimes) {
                    Bukkit.getScheduler().runTaskLater(plugin, this, 5L); // every 5 ticks
                }
            }
        };
        
        Bukkit.getScheduler().runTaskLater(plugin, task, 10L);
    }

    private void executeImpalerStalagmite(List<Player> players) {
        if (players.isEmpty()) return;
        alert("un Bosque de Estalagmitas Empaladoras");
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
        if (invulnerable) {
            for (BlockDisplay bd : orbitalShields.keySet()) {
                if (bd.isValid()) bd.remove();
                for (Entity passenger : bd.getPassengers()) {
                    if (passenger.isValid()) passenger.remove();
                }
            }
            orbitalShields.clear();
        }
        alert("un Escudo de Fractales Orbitales");
        invulnerable = true;
        int count = 3 + random.nextInt(7); // 3 to 9
        for (int i = 0; i < count; i++) {
            BlockDisplay bd = (BlockDisplay) boss.getWorld().spawnEntity(boss.getLocation(), EntityType.BLOCK_DISPLAY);
            bd.setBlock(Bukkit.createBlockData(Material.BLUE_ICE));
            bd.setTeleportDuration(3);
            EntityUtils.setColoredGlowing(bd, NamedTextColor.AQUA);
            
            float size = 1.0f + random.nextFloat() * 1.5f; // 1.0 to 2.5 scale
            
            Transformation t = bd.getTransformation();
            t.getScale().set(size, size, size);
            bd.setTransformation(t);
            
            Slime slime = (Slime) boss.getWorld().spawnEntity(boss.getLocation(), EntityType.SLIME);
            slime.setSize(Math.max(4, (int)(size * 3)));
            slime.setInvisible(true);
            slime.setAI(false);
            slime.setGravity(false);
            slime.setSilent(true);
            slime.getPersistentDataContainer().set(GlacialBonebreakerMechanic.SHIELD_KEY, PersistentDataType.BYTE, (byte) 1);
            bd.addPassenger(slime);
            
            double radius = 10.0 + random.nextDouble() * 10.0; // 10 to 20 blocks
            double height = 2.0 + random.nextDouble() * 6.0; // 2 to 8 blocks height
            // Speed between 0.0075 and 0.015 (75% slower)
            double speed = (0.03 + (random.nextDouble() * 0.03)) * 0.25; 
            if (random.nextBoolean()) speed *= -1; // some orbit in reverse
            double startAngle = random.nextDouble() * 2 * Math.PI;
            
            orbitalShields.put(bd, new double[]{radius, height, speed, startAngle});
        }
    }

    private void executeMirageClones() {
        alert("unos Clones Espejismo");
        int count = 3 + random.nextInt(8); // 3 to 10
        List<Stray> clones = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Stray clone = (Stray) EntityUtils.spawnNatural(boss.getLocation(), EntityType.STRAY);
            if (clone != null) {
                clone.getPersistentDataContainer().set(GlacialBonebreakerMechanic.FAKE_CLONE_KEY, PersistentDataType.BYTE, (byte) 1);
                EntityUtils.setCustomName(clone, boss.getCustomName());
                EntityUtils.trySetAttribute(clone, Attribute.SCALE, 8.0);
                
                if (clone.getEquipment() != null) {
                    clone.getEquipment().setArmorContents(boss.getEquipment().getArmorContents());
                    clone.getEquipment().setItemInMainHand(boss.getEquipment().getItemInMainHand());
                }
                
                clone.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20, 0, false, false));
                clone.setGlowing(false);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!clone.isDead() && clone.isValid()) {
                        EntityUtils.setColoredGlowing(clone, NamedTextColor.AQUA);
                    }
                }, 20L);
                
                Vector randDir = new Vector(random.nextDouble() - 0.5, 0.5, random.nextDouble() - 0.5).normalize().multiply(1.2);
                clone.setVelocity(randDir);
                clones.add(clone);
            }
        }
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 2.0f, 0.5f);
        boss.getWorld().spawnParticle(Particle.SNOWFLAKE, boss.getLocation(), 200, 2, 2, 2, 0.1);
        
        boss.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20, 0, false, false));
        boss.setGlowing(false); // Remove glow to sell the illusion
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!boss.isDead() && boss.isValid()) {
                EntityUtils.setColoredGlowing(boss, NamedTextColor.AQUA);
            }
        }, 20L);
        
        boss.setVelocity(new Vector(random.nextDouble() - 0.5, 0.5, random.nextDouble() - 0.5).normalize().multiply(1.5));
        
        // Position Swap Task (Shell Game)
        new BukkitRunnable() {
            int swaps = 0;
            @Override
            public void run() {
                clones.removeIf(c -> c.isDead() || !c.isValid());
                if (clones.isEmpty() || boss.isDead() || !boss.isValid() || swaps >= 5) {
                    this.cancel();
                    for (Stray c : clones) {
                        if (c.isValid()) {
                            c.getWorld().spawnParticle(Particle.SNOWFLAKE, c.getLocation(), 50, 1, 1, 1, 0.1);
                            c.remove(); // Auto-cleanup remaining clones after phase ends
                        }
                    }
                    return;
                }
                
                boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
                
                List<Location> locs = new ArrayList<>();
                locs.add(boss.getLocation().clone());
                boss.getWorld().spawnParticle(Particle.REVERSE_PORTAL, boss.getLocation(), 100, 1, 2, 1, 0.1);
                
                for (Stray c : clones) {
                    locs.add(c.getLocation().clone());
                    c.getWorld().spawnParticle(Particle.REVERSE_PORTAL, c.getLocation(), 100, 1, 2, 1, 0.1);
                }
                
                Collections.shuffle(locs);
                
                boss.teleport(locs.get(0));
                for (int i = 0; i < clones.size(); i++) {
                    clones.get(i).teleport(locs.get(i + 1));
                }
                
                swaps++;
            }
        }.runTaskTimer(plugin, 80L, 80L); // Swap every 5 seconds, 5 times total
    }

    private void executeBlizzardWall() {
        alert("un Muro de Ventisca Rotatorio");
        Location center = boss.getLocation().clone();
        blizzardActive = true;
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 400 || boss.isDead()) {
                    blizzardActive = false;
                    this.cancel();
                    return;
                }
                double angle = ticks * 0.015; // Even slower rotation
                
                // 1. Draw the massive vertical wall
                for (int d = 0; d < 2; d++) {
                    double offset = d * Math.PI;
                    for (double r = 1; r < 50; r += 1.0) { // Step by 1 to halve particle count per tick
                        double x = Math.cos(angle + offset) * r;
                        double z = Math.sin(angle + offset) * r;
                        // Reduced height to save particles, deltaY = 2.0
                        Location pLoc = center.clone().add(x, 2, z);
                        pLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, pLoc, 5, 0.5, 2.0, 0.5, 0);
                        pLoc.getWorld().spawnParticle(Particle.CLOUD, pLoc, 1, 0.5, 2.0, 0.5, 0);
                    }
                }
                
                // 2. Y-agnostic collision (Infinite Y hitbox)
                for (Player p : getNearbyPlayers(55)) {
                    Vector diff = p.getLocation().toVector().subtract(center.toVector());
                    diff.setY(0);
                    if (diff.length() < 50.0) {
                        double distToLine = Math.abs(Math.sin(angle) * diff.getX() - Math.cos(angle) * diff.getZ());
                        if (distToLine < 1.5) { // 1.5 block thick hitbox
                            if (p.getNoDamageTicks() < 10) { // Avoid instant death from tick-spam
                                p.damage(25.0, boss);
                                p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                                p.setFreezeTicks(Math.min(p.getMaxFreezeTicks(), p.getFreezeTicks() + 100));
                            }
                        }
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void executeZeroFrictionFloor(List<Player> targets) {
        alert("un Suelo de Fricción Cero");
        Map<Location, BlockData> old = replaceFloor(Material.BLUE_ICE, 0.2, targets);
        Bukkit.getScheduler().runTaskLater(plugin, () -> restoreFloor(old), 15 * 20L); // 15 seconds
    }

    private void executeFragileFloor(List<Player> targets) {
        alert("un Suelo Quebradizo");
        Map<Location, BlockData> old = replaceFloor(Material.POWDER_SNOW, 0.5, targets);
        Bukkit.getScheduler().runTaskLater(plugin, () -> restoreFloor(old), 15 * 20L); // 15 seconds
    }

    private Map<Location, BlockData> replaceFloor(Material newMat, double chance, List<Player> targets) {
        return replaceFloor(newMat, 25, 10, chance, targets);
    }
    
    public void spawnAssistantHorde(Player target) {
        int count = 2 + random.nextInt(3); // 2 to 4
        for (int i = 0; i < count; i++) {
            Location loc = boss.getLocation().add(random.nextDouble() * 2 - 1, 1, random.nextDouble() * 2 - 1);
            Skeleton skel = (Skeleton) EntityUtils.spawnNatural(loc, EntityType.SKELETON);
            if (skel != null) {
                skel.getPersistentDataContainer().set(GlacialBonebreakerMechanic.MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                EntityUtils.setCustomName(skel, "&bAsistente Óseo");
                EntityUtils.trySetAttribute(skel, Attribute.MAX_HEALTH, 1.0);
                skel.setHealth(1.0);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (skel.isValid()) {
                        MobGearUtils.equipRandomGear(skel);
                        if (skel.getEquipment() != null) {
                            ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                            sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
                            skel.getEquipment().setItemInMainHand(sword);
                            skel.getEquipment().setItemInMainHandDropChance(0.0f);
                        }
                    }
                }, 2L);
                skel.setTarget(target);
            }
        }
    }

    private Map<Location, BlockData> replaceFloor(Material newMat, int bossRadius, int playerRadius, double chance, List<Player> targets) {
        Map<Location, BlockData> old = new HashMap<>();
        
        // Boss area
        Location bossCenter = boss.getLocation();
        for (int x = -bossRadius; x <= bossRadius; x++) {
            for (int z = -bossRadius; z <= bossRadius; z++) {
                if (x * x + z * z > bossRadius * bossRadius) continue;
                if (random.nextDouble() > chance) continue;
                processFloorBlock(bossCenter.clone().add(x, 0, z), old, newMat);
            }
        }
        
        // Player areas
        for (Player p : targets) {
            Location center = p.getLocation();
            for (int x = -playerRadius; x <= playerRadius; x++) {
                for (int z = -playerRadius; z <= playerRadius; z++) {
                    if (x * x + z * z > playerRadius * playerRadius) continue;
                    if (random.nextDouble() > chance) continue;
                    processFloorBlock(center.clone().add(x, 0, z), old, newMat);
                }
            }
        }
        return old;
    }

    private void processFloorBlock(Location loc, Map<Location, BlockData> old, Material newMat) {
        boolean found = false;
        for (int y = 4; y >= -10; y--) { // Search downwards from above head
            Block check = loc.clone().add(0, y, 0).getBlock();
            if (check.getType().isSolid() && check.getType() != Material.BARRIER) {
                loc.add(0, y, 0);
                found = true;
                break;
            }
        }
        if (!found) return;
        
        Block b = loc.getBlock();
        if (b.getType() != Material.BEDROCK && b.getType() != Material.OBSIDIAN) {
            if (!old.containsKey(loc)) { // Prevent overriding overlapping radii
                old.put(loc, b.getBlockData().clone());
                b.setType(newMat);
            }
        }
    }

    private void restoreFloor(Map<Location, BlockData> old) {
        for (Map.Entry<Location, BlockData> entry : old.entrySet()) {
            entry.getKey().getBlock().setBlockData(entry.getValue());
        }
    }

    private void executeBlizzardGhosts(List<Player> players) {
        alert("unos Fantasmas de la Ventisca");
        for (Player p : players) {
            int count = 3 + random.nextInt(6); // 3 to 8
            for (int i = 0; i < count; i++) {
                Location spawn = p.getLocation().add(random.nextInt(10) - 5, 2, random.nextInt(10) - 5);
                Vex vex = (Vex) EntityUtils.spawnNatural(spawn, EntityType.VEX);
                if (vex != null) {
                    vex.getPersistentDataContainer().set(GlacialBonebreakerMechanic.MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                    EntityUtils.setCustomName(vex, "&bFantasma de la Ventisca");
                    if (vex.getEquipment() != null) {
                        vex.getEquipment().setHelmet(new ItemStack(Material.BLUE_ICE));
                        
                        ItemStack sword = new ItemStack(Material.IRON_SWORD);
                        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
                        sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 8);
                        vex.getEquipment().setItemInMainHand(sword);
                        
                        vex.getEquipment().setItemInMainHandDropChance(0.0f);
                        vex.getEquipment().setHelmetDropChance(0.0f);
                    }
                    vex.setTarget(p);
                }
            }
        }
    }

    private void executeSnowMines(List<Player> players) {
        alert("unas Minas de Nieve");
        for (Player p : players) {
            Location loc = p.getLocation().add(random.nextInt(10) - 5, 0, random.nextInt(10) - 5);
            Snowman sm = (Snowman) EntityUtils.spawnNatural(loc, EntityType.SNOW_GOLEM);
            if (sm != null) {
                sm.setAI(false); // Static
                sm.getPersistentDataContainer().set(GlacialBonebreakerMechanic.MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                EntityUtils.setCustomName(sm, "&bMina de Nieve");
                Creeper creeper = (Creeper) EntityUtils.spawnNatural(loc, EntityType.CREEPER);
                if (creeper != null) {
                    creeper.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false));
                    creeper.getPersistentDataContainer().set(GlacialBonebreakerMechanic.MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                    EntityUtils.setCustomName(creeper, "&bMina de Nieve");
                    sm.addPassenger(creeper);
                }
            }
        }
    }

    private void executeVortexCavalry(List<Player> players) {
        alert("una Caballería del Vórtice");
        for (Player p : players) {
            for (int i = 0; i < 2; i++) { // Spawn 2 per player
                Location loc = p.getLocation().add(random.nextInt(10) - 5, 10, random.nextInt(10) - 5);
                Phantom phantom = (Phantom) EntityUtils.spawnNatural(loc, EntityType.PHANTOM);
                if (phantom != null) {
                    phantom.getPersistentDataContainer().set(GlacialBonebreakerMechanic.MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                    EntityUtils.setCustomName(phantom, "&bCaballero del Vórtice");
                    
                    Stray stray = (Stray) EntityUtils.spawnNatural(loc, EntityType.STRAY);
                    if (stray != null) {
                        stray.getPersistentDataContainer().set(GlacialBonebreakerMechanic.MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                        EntityUtils.setCustomName(stray, "&bJinete del Vórtice");
                        phantom.addPassenger(stray);
                        
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (phantom.isValid()) phantom.setSize(8);
                            if (stray.isValid()) {
                                EntityUtils.trySetAttribute(stray, Attribute.SCALE, 2.5);
                                MobGearUtils.equipRandomGear(stray);
                            }
                        }, 2L); // 2 ticks delay to override any global 1-tick scaling mechanics
                    }
                }
            }
        }
    }

    private void executeTundraAmbush(List<Player> players) {
        alert("una Emboscada de Leñadores");
        for (Player p : players) {
            int toSpawn = 1 + random.nextInt(3);
            for (int i = 0; i < toSpawn; i++) {
                double offsetX = (random.nextBoolean() ? 1 : -1) * (5 + random.nextInt(6));
                double offsetZ = (random.nextBoolean() ? 1 : -1) * (5 + random.nextInt(6));
                Location spawnLoc = p.getLocation().add(offsetX, 0, offsetZ);
                spawnLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);
                
                Vindicator vindicator = (Vindicator) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.VINDICATOR);
                if (vindicator != null) {
                    vindicator.getPersistentDataContainer().set(GlacialBonebreakerMechanic.MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                    EntityUtils.setCustomName(vindicator, "&bLeñador Glacial");
                    if (vindicator.getEquipment() != null) {
                        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
                        axe.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
                        vindicator.getEquipment().setItemInMainHand(axe);
                    }
                }
            }
        }
    }

    private void executeStatusDebuffs(List<Player> players) {
        if (random.nextBoolean()) {
            alert("un Apagón de la Tundra");
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
                alert("una Hipotermia");
                Player target = players.get(random.nextInt(players.size()));
                target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                target.setFreezeTicks(Math.min(target.getMaxFreezeTicks(), target.getFreezeTicks() + 100));
            } else {
                alert("unas Cuerdas Congeladas");
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
        
        for (BlockDisplay bd : orbitalShields.keySet()) {
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

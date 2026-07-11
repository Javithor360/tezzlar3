package com.panita.tezzlar3.difficulty.mechanics;

import net.kyori.adventure.key.Key;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.NamespacedKey;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import com.panita.tezzlar3.core.util.PlayerUtils;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class GigaMagmaCubeBoss {
    private final MagmaCube boss;
    private final JavaPlugin plugin;
    private final BukkitRunnable task;
    private final Set<UUID> attackers = new HashSet<>();
    private final Random random = new Random();
    
    private int attackCooldown = 0;
    private final String bossId;

    public GigaMagmaCubeBoss(MagmaCube boss, JavaPlugin plugin, boolean isNewSpawn) {
        this.boss = boss;
        this.plugin = plugin;
        this.bossId = boss.getUniqueId().toString();
        
        // Setup Attributes
        boss.setSize(20);
        boss.setRemoveWhenFarAway(false);
        EntityUtils.setCustomName(boss, "<#E88331><b>Giga Magma Cube</b></#E88331>", true);
        
        // Ensure it is not riding anything and nothing is riding it
        boss.leaveVehicle();
        boss.getPassengers().forEach(Entity::remove);
        
        // Add yellow glowing
        EntityUtils.setColoredGlowing(boss, NamedTextColor.YELLOW);

        EntityUtils.trySetAttribute(boss, Attribute.MAX_HEALTH, 2500.0);
        EntityUtils.trySetAttribute(boss, Attribute.GRAVITY, 0.45);
        EntityUtils.trySetAttribute(boss, Attribute.ARMOR, 120.0);
        EntityUtils.trySetAttribute(boss, Attribute.ATTACK_DAMAGE, 25.0);
        EntityUtils.trySetAttribute(boss, Attribute.FOLLOW_RANGE, 100.0);
        
        double maxHealth = 2500.0;
        if (boss.getAttribute(Attribute.MAX_HEALTH) != null) {
            maxHealth = boss.getAttribute(Attribute.MAX_HEALTH).getValue();
        }
        boss.setHealth(maxHealth);
        
        // Broadcast
        if (isNewSpawn) {
            Messenger.prefixedBroadcast("<#E88331>¡Un Giga Magma Cube ha despertado en el Nether! ("
                + boss.getLocation().getBlockX() + ", " 
                + boss.getLocation().getBlockY() + ", " 
                + boss.getLocation().getBlockZ() + ")</#E88331>");
            for (Player p : Bukkit.getOnlinePlayers()) {
                SoundUtils.play(p, "entity.ender_dragon.growl", 1, 0.5f);
            }
        }
        
        // Task
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    hideBossBarGlobal();
                    GigaMagmaCubeMechanic.removeActiveBoss(boss.getUniqueId());
                    this.cancel();
                    return;
                }
                
                // Update Bossbar
                float progress = (float) (boss.getHealth() / 1000.0);
                progress = Math.max(0.0f, Math.min(1.0f, progress));
                
                List<Player> nearbyPlayers = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(boss.getWorld()) && player.getLocation().distance(boss.getLocation()) <= 100) {
                        if (PlayerUtils.isSurvival(player)) {
                            Messenger.showBossBar(player, bossId, "<#FF5252><b>Giga Magma Cube</b></#FF5252>", BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_20, progress);
                            if (player.getLocation().distance(boss.getLocation()) <= 50) {
                                nearbyPlayers.add(player);
                            }
                        } else {
                            Messenger.hideBossBar(player, bossId);
                        }
                    } else {
                        Messenger.hideBossBar(player, bossId);
                    }
                }
                
                if (attackCooldown > 0) {
                    attackCooldown--;
                } else if (!nearbyPlayers.isEmpty()) {
                    executeRandomAttack(nearbyPlayers);
                    attackCooldown = 10 + random.nextInt(51); // 10 to 60 seconds
                }
            }
        };
        this.task.runTaskTimer(plugin, 20L, 20L); // Every 1 second
    }
    
    private void hideBossBarGlobal() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Messenger.hideBossBar(p, bossId);
        }
    }
    
    public void addAttacker(UUID uuid) {
        attackers.add(uuid);
    }
    
    public void handleDeath() {
        hideBossBarGlobal();
        if (task != null) task.cancel();
        
        for (UUID uuid : attackers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                giveRewards(p);
            }
        }
    }
    
    private void giveRewards(Player p) {
        // Constant 8-24 Netherite Scrap
        int scrapCount = 8 + random.nextInt(24);
        giveOrDropItems(p, Material.NETHERITE_SCRAP, scrapCount);
        
        // Select 2 random choices
        List<Runnable> options = new ArrayList<>();
        
        options.add(() -> giveOrDropItems(p, Material.GOLDEN_APPLE, 1 + random.nextInt(31)));
        options.add(() -> giveOrDropItems(p, Material.ENCHANTED_GOLDEN_APPLE, 4));
        options.add(() -> p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0)));
        options.add(() -> p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 12 * 60 * 60 * 20, 0)));
        options.add(() -> p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1)));
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
        
        // Execute 3 choices
        options.get(0).run();
        options.get(1).run();
        options.get(2).run();

        Messenger.prefixedSend(p, "<#E88331>¡Has recibido grandiosas recompensas por la derrota del Giga Magma Cube!</#E88331>");
        SoundUtils.play(p, "entity.player.levelup", 1, 2);
    }
    
    private void executeRandomAttack(List<Player> players) {
        int attackType = random.nextInt(8);
        switch (attackType) {
            case 0:
                executeChargedBeam(players);
                break;
            case 1:
                executeMinions(players);
                break;
            case 2:
                executeFury(players);
                break;
            case 3:
                executeSwap(players);
                break;
            case 4:
                executeTraps(players);
                break;
            case 5:
                executeGhasts(players);
                break;
            case 6:
                executeBlazes(players);
                break;
            case 7:
                executePyromaniacPiglins(players);
                break;
        }
    }
    
    private void alertPlayers(List<Player> players, String attackName) {
        for (Player p : players) {
            Messenger.prefixedSend(p, "<#FF5252>¡El Giga Magma Cube se prepara para <#FFF200>" + attackName + "</#FFF200>!</#FF5252>");
            SoundUtils.play(p, "entity.wither.ambient", 1, 0.5f);
        }
    }
    
    private void executeChargedBeam(List<Player> players) {
        alertPlayers(players, "Lanzar Rayo Cargado");
        
        // 8 seconds = 160 ticks
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (boss.isDead() || ticks >= 160) {
                    if (!boss.isDead()) {
                        for (Player p : players) {
                            if (p.isOnline() && p.getLocation().distance(boss.getLocation()) <= 50) {
                                double dmg = 30.0 + random.nextInt(21); // 30 to 50
                                p.damage(dmg);
                                p.getWorld().strikeLightningEffect(p.getLocation());
                            }
                        }
                    }
                    this.cancel();
                    return;
                }
                
                // Draw particles towards all players
                for (Player p : players) {
                    if (p.isOnline()) {
                        Location start = boss.getLocation().add(0, boss.getHeight() / 2, 0);
                        Location end = p.getLocation().add(0, p.getHeight() / 2, 0);
                        double distance = start.distance(end);
                        Vector dir = end.toVector().subtract(start.toVector()).normalize().multiply(0.5);
                        
                        Location current = start.clone();
                        // 160 ticks total for fade
                        int g = Math.max(0, 255 - (int) (((double) ticks / 160) * 255)); // Green fades out
                        int r = Math.min(255, (int) (((double) ticks / 160) * 255));    // Red fades in
                        
                        for (double i = 0; i < distance; i += 0.5) {
                            current.getWorld().spawnParticle(Particle.DUST, current, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(r, g, 0), 2.0f));
                            current.add(dir);
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void executeMinions(List<Player> players) {
        alertPlayers(players, "Engendrar Secuaces");
        for (Player p : players) {
            int amount = 1 + random.nextInt(3); // 1 to 3
            for (int i = 0; i < amount; i++) {
                Location loc = p.getLocation().add(random.nextInt(5) - 2, 1, random.nextInt(5) - 2);
                MagmaCube minion = (MagmaCube) EntityUtils.spawnNatural(loc, EntityType.MAGMA_CUBE);
                EntityUtils.setCustomName(minion, "<#FFCA28>Magma Cube Secuaz</#FFCA28>");
                minion.getPersistentDataContainer().set(GigaMagmaCubeMechanic.MINION_KEY, PersistentDataType.BYTE, (byte) 1);
                try {
                    if (minion.getAttribute(Attribute.FOLLOW_RANGE) != null) minion.getAttribute(Attribute.FOLLOW_RANGE).setBaseValue(100.0);
                } catch (Exception ignored) {}
            }
        }
    }
    
    private void executeFury(List<Player> players) {
        alertPlayers(players, "Inyectar su Furia");
        for (Player p : players) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30 * 20, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 30 * 20, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 5 * 20, 0));
        }
    }
    
    private void executeSwap(List<Player> players) {
        if (players.size() < 2) return;
        alertPlayers(players, "un Intercambio Espacial");
        
        List<Location> locs = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (Player p : players) {
            locs.add(p.getLocation());
            names.add(p.getName());
        }
        
        Collections.shuffle(locs);
        Collections.shuffle(names);
        
        List<Player> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);
        
        Location firstLoc = shuffledPlayers.get(0).getLocation();
        for (int i = 0; i < shuffledPlayers.size(); i++) {
            Player p1 = shuffledPlayers.get(i);
            Player p2 = (i + 1 < shuffledPlayers.size()) ? shuffledPlayers.get(i + 1) : shuffledPlayers.get(0);
            
            Location targetLoc = (i + 1 < shuffledPlayers.size()) ? p2.getLocation() : firstLoc;
            p1.teleport(targetLoc);
            
            Messenger.prefixedSend(p1, "<#FFCA28>Has intercambiado posición con <b>" + p2.getName() + "</b>.</#FFCA28>");
        }
    }
    
    private void executeTraps(List<Player> players) {
        alertPlayers(players, "Poner Trampas de Piso");
        Material[] traps = {Material.LAVA, Material.COBWEB, Material.MAGMA_BLOCK};
        for (Player p : players) {
            Material trap = traps[random.nextInt(traps.length)];
            p.getLocation().getBlock().setType(trap);
        }
    }
    
    private void executeGhasts(List<Player> players) {
        alertPlayers(players, "Pedir Refuerzos Aéreos");
        int amount = 3 + random.nextInt(8); // 3 to 10
        for (int i = 0; i < amount; i++) {
            Location loc = boss.getLocation().add(random.nextInt(20) - 10, 15 + random.nextInt(10), random.nextInt(20) - 10);
            Ghast ghast = (Ghast) EntityUtils.spawnNatural(loc, EntityType.GHAST);
            EntityUtils.setCustomName(ghast, "<#FFCA28>Ghast Secuaz</#FFCA28>");
            try {
                if (ghast.getAttribute(Attribute.FOLLOW_RANGE) != null) ghast.getAttribute(Attribute.FOLLOW_RANGE).setBaseValue(100.0);
            } catch (Exception ignored) {}
        }
    }
    
    private void executeBlazes(List<Player> players) {
        alertPlayers(players, "una Invocación Tormentosa");
        for (Player p : players) {
            Location loc = p.getLocation().add(random.nextInt(10) - 5, 5, random.nextInt(10) - 5);
            Blaze blaze = (Blaze) EntityUtils.spawnNatural(loc, EntityType.BLAZE);
            EntityUtils.setCustomName(blaze, "<#FFCA28>Blaze Secuaz</#FFCA28>");
            blaze.getPersistentDataContainer().set(GigaMagmaCubeMechanic.BLAZE_KEY, PersistentDataType.BYTE, (byte) 1);
            try {
                if (blaze.getAttribute(Attribute.FOLLOW_RANGE) != null) blaze.getAttribute(Attribute.FOLLOW_RANGE).setBaseValue(100.0);
            } catch (Exception ignored) {}
        }
    }
    
    private void giveOrDropItem(Player p, ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        
        java.util.Map<Integer, ItemStack> leftover = p.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                p.getWorld().dropItem(p.getLocation(), drop);
            }
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

    private void executePyromaniacPiglins(List<Player> players) {
        alertPlayers(players, "Invocar Horda Pirómana");
        boss.getWorld().playSound(boss.getLocation(), org.bukkit.Sound.ENTITY_PIGLIN_ANGRY, 3.0f, 0.5f);
        
        for (Player p : players) {
            int piglinCount = 2 + random.nextInt(2); // 2 to 3 piglins per player
            for (int i = 0; i < piglinCount; i++) {
                Location spawnLoc = p.getLocation().add(random.nextInt(11) - 5, 2, random.nextInt(11) - 5);
                Piglin piglin = (Piglin) EntityUtils.spawnNatural(spawnLoc, EntityType.PIGLIN);
                if (piglin != null) {
                    EntityUtils.setCustomName(piglin, "&6Piglin Pirómano Demente");
                    piglin.setImmuneToZombification(true);
                    
                    if (piglin.getAttribute(Attribute.MAX_HEALTH) != null) {
                        piglin.getAttribute(Attribute.MAX_HEALTH).setBaseValue(60.0);
                        piglin.setHealth(60.0);
                    }

                    org.bukkit.inventory.EntityEquipment eq = piglin.getEquipment();
                    if (eq != null) {
                        Key modelKey = Key.key("panita", "fallen_hero");
                        
                        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
                        Equippable eqCompH = helmet.getData(DataComponentTypes.EQUIPPABLE);
                        if (eqCompH != null) helmet.setData(DataComponentTypes.EQUIPPABLE, eqCompH.toBuilder().assetId(modelKey).build());
                        eq.setHelmet(helmet);
                        eq.setHelmetDropChance(0.0f);
                        
                        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
                        Equippable eqCompC = chestplate.getData(DataComponentTypes.EQUIPPABLE);
                        if (eqCompC != null) chestplate.setData(DataComponentTypes.EQUIPPABLE, eqCompC.toBuilder().assetId(modelKey).build());
                        eq.setChestplate(chestplate);
                        eq.setChestplateDropChance(0.0f);
                        
                        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
                        Equippable eqCompL = leggings.getData(DataComponentTypes.EQUIPPABLE);
                        if (eqCompL != null) leggings.setData(DataComponentTypes.EQUIPPABLE, eqCompL.toBuilder().assetId(modelKey).build());
                        eq.setLeggings(leggings);
                        eq.setLeggingsDropChance(0.0f);
                        
                        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
                        Equippable eqCompB = boots.getData(DataComponentTypes.EQUIPPABLE);
                        if (eqCompB != null) boots.setData(DataComponentTypes.EQUIPPABLE, eqCompB.toBuilder().assetId(modelKey).build());
                        eq.setBoots(boots);
                        eq.setBootsDropChance(0.0f);
                        
                        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
                        eq.setItemInMainHand(crossbow);
                        eq.setItemInMainHandDropChance(0.0f);
                    }

                    piglin.getPersistentDataContainer().set(GigaMagmaCubeMechanic.PIGLIN_PYROMANIAC_KEY, PersistentDataType.BYTE, (byte) 1);
                }
            }
        }
    }
}

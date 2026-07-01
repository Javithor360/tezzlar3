package com.panita.tezzlar3.missions.refuge;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.difficulty.mechanics.AcidRainMechanic;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RefugeManager implements Listener {
    
    private final JavaPlugin plugin;
    private final NamespacedKey TNT_KEY;
    private final NamespacedKey DOOMED_KEY;
    private final NamespacedKey SURVIVOR_KEY;
    
    public enum Stage { INACTIVE, STAGE_1, STAGE_2 }
    private Stage currentStage = Stage.INACTIVE;
    
    private BukkitTask timerTask;
    private int timeLeft;
    private int survivalDuration;
    
    private Location refugeLocation;
    private double safeWidth;
    private double safeLength;
    
    private final Map<UUID, Integer> abandonCounters = new HashMap<>();
    private final Random random = new Random();
    
    public RefugeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.TNT_KEY = new NamespacedKey(plugin, "refuge_tnt");
        this.DOOMED_KEY = new NamespacedKey(plugin, "refuge_doomed");
        this.SURVIVOR_KEY = new NamespacedKey(plugin, "refuge_survivor");
    }
    
    public boolean isActive() {
        return currentStage != Stage.INACTIVE;
    }
    
    @EventHandler
    public void onTntExplode(EntityExplodeEvent event) {
        if (event.getEntity() != null && event.getEntity().getPersistentDataContainer().has(TNT_KEY, PersistentDataType.BYTE)) {
            event.blockList().clear();
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (currentStage == Stage.INACTIVE) {
            player.getPersistentDataContainer().remove(SURVIVOR_KEY);
        }
        
        if (player.getPersistentDataContainer().has(DOOMED_KEY, PersistentDataType.BYTE)) {
            player.getPersistentDataContainer().remove(DOOMED_KEY);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && !player.isDead()) {
                    player.setHealth(0.0); // Kills the player for evading the refuge death
                }
            }, 20L); // Wait 1 second after connecting
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (currentStage == Stage.STAGE_2 && player.getPersistentDataContainer().has(SURVIVOR_KEY, PersistentDataType.BYTE)) {
            doomPlayer(player);
        }
    }
    
    public void startEvent() {
        if (currentStage != Stage.INACTIVE) return;
        
        World overworld = Bukkit.getWorld(plugin.getConfig().getString("worldName", "world"));
        int x = plugin.getConfig().getInt("missions.refuge.x", 1000);
        int y = plugin.getConfig().getInt("missions.refuge.y", 64);
        int z = plugin.getConfig().getInt("missions.refuge.z", 1000);
        this.safeWidth = plugin.getConfig().getDouble("missions.refuge.width", 102.0);
        this.safeLength = plugin.getConfig().getDouble("missions.refuge.length", 55.0);
        this.timeLeft = plugin.getConfig().getInt("missions.refuge.duration", 300);
        this.survivalDuration = plugin.getConfig().getInt("missions.refuge.survival_duration", 300);
        
        this.refugeLocation = new Location(overworld, x, y, z);
        this.abandonCounters.clear();
        
        String rawTitle = "<b><gradient:#5FE2C5:#C6DEF1:#5FE2C5><shadow:#0D1E40:1>TEZZLAR</shadow></gradient></b>";
        String rawSub = "<#DB7EF7>¡Un mini-evento ha dado inicio!</#DB7EF7>";
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            Messenger.showTitle(p, rawTitle, rawSub, Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofMillis(1000));
            p.getPersistentDataContainer().remove(SURVIVOR_KEY);
        }

        SoundUtils.playGlobal("event.raid.horn", 100, 1.0f);
        Messenger.prefixedBroadcast("&c¡Un evento de Refugio ha comenzado!");
        Messenger.prefixedBroadcast("&7Debes llegar a las coordenadas &c" + x + " " + y + " " + z + " &7(Overworld) en el tiempo requerido y mantenerte bajo techo.");

        currentStage = Stage.STAGE_1;
        
        AcidRainMechanic.getInstance().forceAcidRain();
        
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (currentStage == Stage.STAGE_1) {
                    if (timeLeft <= 0) {
                        endStage1();
                        return;
                    }
                    
                    String timeStr = formatTime(timeLeft);
                    String title = "<red><b>Alerta:</b> Tienes " + timeStr + " para llegar al refugio.</red>";
                    
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!PlayerUtils.isSurvival(player)) continue;
                        Messenger.sendActionBar(player, title);
                    }
                    
                    timeLeft--;
                } else if (currentStage == Stage.STAGE_2) {
                    if (timeLeft <= 0) {
                        endStage2();
                        cancel();
                        return;
                    }
                    
                    String timeStr = formatTime(timeLeft);
                    String title = "<red><b>Alerta:</b> Sobrevive en el refugio " + timeStr + "</red>";
                    
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!PlayerUtils.isSurvival(player)) continue;
                        if (player.getPersistentDataContainer().has(SURVIVOR_KEY, PersistentDataType.BYTE)) {
                            boolean isSafe = isPlayerSafe(player);
                            
                            if (!isSafe) {
                                int outTime = abandonCounters.getOrDefault(player.getUniqueId(), 0) + 1;
                                abandonCounters.put(player.getUniqueId(), outTime);
                                
                                Messenger.sendActionBar(player, "<dark_red><b>¡PELIGRO!</b> Vuelve a ponerte a salvo (" + outTime + "/10s)</dark_red>");
                                SoundUtils.play(player, "block.note_block.pling", 1, 0.5f);
                                
                                if (outTime >= 10) {
                                    doomPlayer(player);
                                }
                            } else {
                                abandonCounters.put(player.getUniqueId(), 0);
                                Messenger.sendActionBar(player, title);
                            }
                            
                            // TNT rain over survivors every 3 seconds
                            if (timeLeft % 3 == 0 && player.getWorld().getEnvironment() == World.Environment.NORMAL) {
                                int tntCount = 1 + random.nextInt(3);
                                for (int i = 0; i < tntCount; i++) {
                                    Location spawnLoc = player.getLocation().clone().add(
                                            (random.nextDouble() - 0.5) * 20,
                                            35 + random.nextInt(10),
                                            (random.nextDouble() - 0.5) * 20
                                    );
                                    
                                    TNTPrimed tnt = player.getWorld().spawn(spawnLoc, TNTPrimed.class);
                                    tnt.setFuseTicks(100); // 5 seconds to fall from +35
                                    tnt.setYield(4.0f);
                                    tnt.getPersistentDataContainer().set(TNT_KEY, PersistentDataType.BYTE, (byte) 1);
                                }
                            }
                        }
                    }
                    
                    timeLeft--;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    public void cancelEvent() {
        if (currentStage == Stage.INACTIVE) return;
        currentStage = Stage.INACTIVE;
        if (timerTask != null) timerTask.cancel();
        
        AcidRainMechanic.getInstance().stopForcedAcidRain();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getPersistentDataContainer().remove(SURVIVOR_KEY);
        }
        
        abandonCounters.clear();
        Messenger.prefixedBroadcast("&aEl evento de Refugio ha sido cancelado manualmente.");
    }
    
    private void endStage1() {
        currentStage = Stage.STAGE_2;
        timeLeft = survivalDuration;
        
        Messenger.prefixedBroadcast("&c¡El tiempo de llegada ha finalizado! Inicia la fase de asedio y supervivencia.");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!PlayerUtils.isSurvival(player)) continue;
            if (player.getWorld().getEnvironment() == World.Environment.NORMAL && !player.isDead()) {
                if (isPlayerSafe(player)) {
                    player.getPersistentDataContainer().set(SURVIVOR_KEY, PersistentDataType.BYTE, (byte) 1);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    Messenger.prefixedSend(player, "&aHas llegado a tiempo. ¡Ahora sobrevive el asedio sin salir del refugio!");
                } else {
                    doomPlayer(player);
                }
            }
        }
    }
    
    private void endStage2() {
        currentStage = Stage.INACTIVE;
        AcidRainMechanic.getInstance().stopForcedAcidRain();
        abandonCounters.clear();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!PlayerUtils.isSurvival(player)) continue;
            if (player.getPersistentDataContainer().has(SURVIVOR_KEY, PersistentDataType.BYTE)) {
                player.getPersistentDataContainer().remove(SURVIVOR_KEY);
                
                double rewardHealth = plugin.getConfig().getDouble("missions.refuge.rewards.health", 0.0);
                double rewardDamage = plugin.getConfig().getDouble("missions.refuge.rewards.damage", 0.0);
                double rewardArmor = plugin.getConfig().getDouble("missions.refuge.rewards.armor", 0.0);

                StringBuilder rewardsMsg = new StringBuilder();

                if (rewardHealth > 0) {
                    AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                    if (maxHealth != null) {
                        EntityUtils.trySetAttribute(player, Attribute.MAX_HEALTH, maxHealth.getBaseValue() + rewardHealth);
                        rewardsMsg.append("<red>+").append(rewardHealth).append(" Salud Máxima</red> ");
                    }
                }
                
                if (rewardDamage > 0) {
                    AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);
                    if (attackDamage != null) {
                        EntityUtils.trySetAttribute(player, Attribute.ATTACK_DAMAGE, attackDamage.getBaseValue() + rewardDamage);
                        rewardsMsg.append("<dark_red>+").append(rewardDamage).append(" Daño de Ataque</dark_red> ");
                    }
                }

                if (rewardArmor > 0) {
                    AttributeInstance armor = player.getAttribute(Attribute.ARMOR);
                    if (armor != null) {
                        EntityUtils.trySetAttribute(player, Attribute.ARMOR, armor.getBaseValue() + rewardArmor);
                        rewardsMsg.append("<gray>+").append(rewardArmor).append(" Armadura</gray> ");
                    }
                }
                
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
                
                if (rewardsMsg.length() > 0) {
                    Messenger.prefixedSend(player, "&a¡Has sobrevivido al asedio del refugio! Recompensas obtenidas: " + rewardsMsg.toString().trim());
                } else {
                    Messenger.prefixedSend(player, "&a¡Has sobrevivido al asedio del refugio y estás a salvo!");
                }
            }
        }
    }
    
    private boolean isPlayerSafe(Player player) {
        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) return false;
        double dx = Math.abs(player.getLocation().getX() - refugeLocation.getX());
        double dz = Math.abs(player.getLocation().getZ() - refugeLocation.getZ());
        if (dx <= (safeWidth / 2.0) && dz <= (safeLength / 2.0)) {
            int highestY = player.getWorld().getHighestBlockYAt(player.getLocation());
            if (highestY > player.getLocation().getY()) {
                return true;
            }
        }
        return false;
    }
    
    private void doomPlayer(Player player) {
        player.getPersistentDataContainer().remove(SURVIVOR_KEY);
        Messenger.prefixedBroadcast("&c" + player.getName() + " &7no pudo mantenerse a salvo y sufrirá las consecuencias.");
        
        player.getPersistentDataContainer().set(DOOMED_KEY, PersistentDataType.BYTE, (byte) 1);
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 999999, 255, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 999999, 200, false, false));
        EntityUtils.setColoredGlowing(player, NamedTextColor.AQUA);
        
        int deathDelaySecs = 10 + random.nextInt(11);
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    if (player.isDead()) {
                        player.getPersistentDataContainer().remove(DOOMED_KEY);
                    }
                    EntityUtils.removeColoredGlowing(player);
                    cancel();
                    return;
                }
                
                if (ticks >= deathDelaySecs * 20) {
                    player.getPersistentDataContainer().remove(DOOMED_KEY);
                    player.setHealth(0.0);
                    EntityUtils.removeColoredGlowing(player);
                    cancel();
                    return;
                }
                
                if (ticks % 10 == 0) {
                    Location tntLoc = player.getLocation().clone().add(
                            (random.nextDouble() - 0.5) * 40,
                            15 + random.nextInt(10),
                            (random.nextDouble() - 0.5) * 40
                    );
                    
                    TNTPrimed tnt = player.getWorld().spawn(tntLoc, TNTPrimed.class);
                    tnt.setFuseTicks(40);
                    tnt.setYield(5.0f);
                    tnt.getPersistentDataContainer().set(TNT_KEY, PersistentDataType.BYTE, (byte) 1);
                }
                
                ticks += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
    
    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}

package com.panita.tezzlar3.missions.refuge;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.Random;

public class RefugeManager implements Listener {
    
    private final JavaPlugin plugin;
    private final NamespacedKey TNT_KEY;
    private final NamespacedKey DOOMED_KEY;
    private BukkitTask timerTask;
    private int timeLeft;
    private boolean active = false;
    
    private Location refugeLocation;
    private double safeWidth;
    private double safeLength;
    
    public RefugeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.TNT_KEY = new NamespacedKey(plugin, "refuge_tnt");
        this.DOOMED_KEY = new NamespacedKey(plugin, "refuge_doomed");
    }
    
    public boolean isActive() {
        return active;
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
        if (player.getPersistentDataContainer().has(DOOMED_KEY, PersistentDataType.BYTE)) {
            player.getPersistentDataContainer().remove(DOOMED_KEY);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && !player.isDead()) {
                    player.setHealth(0.0); // Kills the player for evading the refuge death
                }
            }, 20L); // Wait 1 second after connecting
        }
    }
    
    public void startEvent() {
        if (active) return;
        
        World overworld = Bukkit.getWorld(plugin.getConfig().getString("worldName", "world"));
        int x = plugin.getConfig().getInt("missions.refuge.x", 1000);
        int y = plugin.getConfig().getInt("missions.refuge.y", 64);
        int z = plugin.getConfig().getInt("missions.refuge.z", 1000);
        this.safeWidth = plugin.getConfig().getDouble("missions.refuge.width", 102.0);
        this.safeLength = plugin.getConfig().getDouble("missions.refuge.length", 55.0);
        this.timeLeft = plugin.getConfig().getInt("missions.refuge.duration", 300);
        
        this.refugeLocation = new Location(overworld, x, y, z);
        
        String rawTitle = "<b><gradient:#5FE2C5:#C6DEF1:#5FE2C5><shadow:#0D1E40:1>TEZZLAR</shadow></gradient></b>";
        String rawSub = "<#DB7EF7>¡Un mini-evento ha dado inicio!</#DB7EF7>";
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            Messenger.showTitle(p, rawTitle, rawSub, Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofMillis(1000));
        }

        SoundUtils.playGlobal("event.raid.horn", 100, 1.0f);
        Messenger.prefixedBroadcast("&c¡Un evento de Refugio ha comenzado!");
        Messenger.prefixedBroadcast("&7Debes llegar a las coordenadas &c" + x + " " + y + " " + z + " &7(Overworld) en el tiempo requerido y mantenerte bajo techo.");

        active = true;
        
        AcidRainMechanic.getInstance().forceAcidRain();
        
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    endEvent();
                    cancel();
                    return;
                }
                
                String timeStr = formatTime(timeLeft);
                String title = "<red><b>Alerta:</b> Tienes " + timeStr + " para llegar al refugio.</red>";
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Messenger.sendActionBar(player, title);
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    public void cancelEvent() {
        if (!active) return;
        active = false;
        if (timerTask != null) timerTask.cancel();
        
        AcidRainMechanic.getInstance().stopForcedAcidRain();
        
        Messenger.prefixedBroadcast("&aEl evento de Refugio ha sido cancelado manualmente.");
    }
    
    private void endEvent() {
        active = false;
        
        AcidRainMechanic.getInstance().stopForcedAcidRain();
        
        Random random = new Random();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean isSafe = false;
            
            if (player.getWorld().getEnvironment() == World.Environment.NORMAL) {
                double dx = Math.abs(player.getLocation().getX() - refugeLocation.getX());
                double dz = Math.abs(player.getLocation().getZ() - refugeLocation.getZ());
                if (dx <= (safeWidth / 2.0) && dz <= (safeLength / 2.0)) {
                    int highestY = player.getWorld().getHighestBlockYAt(player.getLocation());
                    if (highestY > player.getLocation().getY()) {
                        isSafe = true;
                    }
                }
            }
            
            if (isSafe) {
                AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth != null) {
                    EntityUtils.trySetAttribute(player, Attribute.MAX_HEALTH, maxHealth.getBaseValue() + 2.0);
                }
                
                AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);
                if (attackDamage != null) {
                    EntityUtils.trySetAttribute(player, Attribute.ATTACK_DAMAGE, attackDamage.getBaseValue() + 4.0);
                }
                
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
                Messenger.prefixedSend(player, "&a¡Has llegado al refugio a tiempo! Tu salud máxima y fuerza han aumentado permanentemente.");
            } else {
                Messenger.prefixedBroadcast("&c" + player.getName() + " &7no llegó al refugio y sufrirá las consecuencias.");
                
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
        }
    }
    
    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}

package com.panita.tezzlar3.missions.refuge;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.util.SoundUtils;
import net.kyori.adventure.bossbar.BossBar;
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
    private BukkitTask timerTask;
    private int timeLeft;
    private boolean active = false;
    
    private Location refugeLocation;
    private double safeRadius;
    
    public RefugeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.TNT_KEY = new NamespacedKey(plugin, "refuge_tnt");
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
    
    public void startEvent() {
        if (active) return;
        
        World overworld = Bukkit.getWorld(plugin.getConfig().getString("worldName", "world"));
        int x = plugin.getConfig().getInt("missions.refuge.x", 1000);
        int y = plugin.getConfig().getInt("missions.refuge.y", 64);
        int z = plugin.getConfig().getInt("missions.refuge.z", 1000);
        this.safeRadius = plugin.getConfig().getDouble("missions.refuge.radius", 30.0);
        this.timeLeft = plugin.getConfig().getInt("missions.refuge.duration", 300);
        
        this.refugeLocation = new Location(overworld, x, y, z);
        
        String rawTitle = "<b><gradient:#5FE2C5:#C6DEF1:#5FE2C5><shadow:#0D1E40:1>TEZZLAR</shadow></gradient></b>";
        String rawSub = "<#DB7EF7>¡Un mini-evento ha dado inicio!</#DB7EF7>";
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            Messenger.showTitle(p, rawTitle, rawSub, Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofMillis(1000));
        }

        SoundUtils.playGlobal("event.raid.horn", 100, 1.0f);
        Messenger.prefixedBroadcast("&c¡Un evento de Refugio ha comenzado!");
        Messenger.prefixedBroadcast("&7Debes llegar a las coordenadas &c" + x + " " + y + " " + z + " &7(Overworld) en el tiempo requerido.");

        active = true;
        
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
                
                float progress = (float) timeLeft / plugin.getConfig().getInt("missions.refuge.duration", 300);
                if (progress < 0.0f) progress = 0.0f;
                if (progress > 1.0f) progress = 1.0f;
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Messenger.showBossBar(player, "refuge_event", title, BossBar.Color.RED, BossBar.Overlay.NOTCHED_10, progress);
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    public void cancelEvent() {
        if (!active) return;
        active = false;
        if (timerTask != null) timerTask.cancel();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            Messenger.hideBossBar(player, "refuge_event");
        }
        
        Messenger.prefixedBroadcast("&aEl evento de Refugio ha sido cancelado manualmente.");
    }
    
    private void endEvent() {
        active = false;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            Messenger.hideBossBar(player, "refuge_event");
        }
        
        Random random = new Random();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean isSafe = false;
            
            if (player.getWorld().getEnvironment() == World.Environment.NORMAL) {
                if (player.getLocation().distanceSquared(refugeLocation) <= (safeRadius * safeRadius)) {
                    isSafe = true;
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
                
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 999999, 255, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 999999, 200, false, false));
                EntityUtils.setColoredGlowing(player, NamedTextColor.AQUA);
                
                int deathDelaySecs = 10 + random.nextInt(11);
                
                new BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (!player.isOnline() || player.isDead()) {
                            EntityUtils.removeColoredGlowing(player);
                            cancel();
                            return;
                        }
                        
                        if (ticks >= deathDelaySecs * 20) {
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

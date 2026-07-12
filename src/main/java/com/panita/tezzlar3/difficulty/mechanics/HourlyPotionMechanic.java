package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.minievents.impl.RandomPotionEvent;
import com.panita.tezzlar3.timeline.events.HourChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import java.time.Duration;

public class HourlyPotionMechanic extends DifficultyMechanic {

    public HourlyPotionMechanic(JavaPlugin plugin) {
        super(plugin, 27);
    }

    @EventHandler
    public void onHourChange(HourChangeEvent event) {
        if (!isActive()) return;

        // Perform roulette effect
        new BukkitRunnable() {
            int ticks = 0;
            int[] delays = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 5, 6, 8, 10, 14, 20};
            int currentIndex = 0;
            int nextTickTarget = 0;

            @Override
            public void run() {
                if (currentIndex >= delays.length) {
                    // Finished
                    this.cancel();
                    applyHourlyEffect();
                    return;
                }
                
                if (ticks >= nextTickTarget) {
                    PotionEffect dummy = RandomPotionEvent.generateRandomEffect(20);
                    String name = formatPotionName(dummy.getType());
                    String title = "<#9F5EFF>Ruleta de Poción</#9F5EFF>";
                    String subtitle = "&f>> &b" + name + " &f<<";
                    
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (PlayerUtils.isSurvival(player)) {
                            Messenger.showTitle(player, title, subtitle, Duration.ZERO, Duration.ofMillis(500), Duration.ZERO);
                            SoundUtils.playInRadius(player.getLocation(), "ui.button.click", 1.0f, 1.5f);
                        }
                    }
                    nextTickTarget = ticks + delays[currentIndex];
                    currentIndex++;
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void applyHourlyEffect() {
        // Duration: 1 hour (3600 seconds = 72000 ticks)
        PotionEffect effect = RandomPotionEvent.generateRandomEffect(72000);
        long expiration = System.currentTimeMillis() + (3600 * 1000);
        
        Tezzlar.getConfigManager().updateString("hourly_potion.type", effect.getType().getName(), null);
        Tezzlar.getConfigManager().updateInt("hourly_potion.amplifier", effect.getAmplifier(), null);
        Tezzlar.getConfigManager().updateLong("hourly_potion.expiration", expiration, null);
        
        String name = formatPotionName(effect.getType());
        String roman = effect.getAmplifier() == 0 ? "I" : (effect.getAmplifier() == 1 ? "II" : "III");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (PlayerUtils.isSurvival(player)) {
                Messenger.showTitle(player, "<#9F5EFF>¡Poción Activa!</#9F5EFF>", "&b" + name + " " + roman, Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(1));
                SoundUtils.playInRadius(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
                player.addPotionEffect(effect);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isActive()) return;
        Player player = event.getPlayer();
        if (!PlayerUtils.isSurvival(player)) return;

        long expiration = Tezzlar.getConfigManager().getLong("hourly_potion.expiration", 0L);
        if (expiration > System.currentTimeMillis()) {
            String typeName = Tezzlar.getConfigManager().getString("hourly_potion.type", "");
            int amplifier = Tezzlar.getConfigManager().getInt("hourly_potion.amplifier", 0);
            
            if (!typeName.isEmpty()) {
                PotionEffectType type = PotionEffectType.getByName(typeName);
                if (type != null) {
                    long remainingMs = expiration - System.currentTimeMillis();
                    int remainingTicks = (int) (remainingMs / 50);
                    
                    if (remainingTicks > 0) {
                        PotionEffect effect = new PotionEffect(type, remainingTicks, amplifier);
                        player.addPotionEffect(effect);
                    }
                }
            }
        }
    }

    private String formatPotionName(PotionEffectType type) {
        String name = type.getName().replace("_", " ").toLowerCase();
        StringBuilder capitalized = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                capitalized.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return capitalized.toString().trim();
    }
}

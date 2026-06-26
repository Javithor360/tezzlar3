package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class AcidRainMechanic extends DifficultyMechanic {
    private final Random random = new Random();
    private boolean isAcidRain = false;

    public AcidRainMechanic(JavaPlugin plugin) {
        super(plugin, 8); // Day 8
        
        // Task runs every 3 seconds (60 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive() || !isAcidRain) return;
            
            for (World world : Bukkit.getWorlds()) {
                if (world.getEnvironment() != World.Environment.NORMAL) continue;
                if (!world.hasStorm()) continue;
                
                for (Player player : world.getPlayers()) {
                    if (player.isDead() || !player.isValid()) continue;
                    
                    if (player.isInRain()) {
                        double newHealth = Math.max(0, player.getHealth() - 1.0);
                        player.setHealth(newHealth);
                        player.playHurtAnimation(0);
                        SoundUtils.play(player, "entity.player.hurt", 1, 0.7F);
                    }
                }
            }
        }, 60L, 60L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (!isActive()) return;
        
        World world = event.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) return;

        if (event.toWeatherState()) {
            isAcidRain = random.nextInt(100) < 40;
            if (isAcidRain) {
                Messenger.prefixedBroadcast("<#64B5F6>Se informa a todos los supervivientes que una <#81C784>lluvia ácida</#81C784> está a punto de comenzar, se recomienda buscar refugio.</#64B5F6>");
            }
        } else {
            isAcidRain = false;
        }
    }
}

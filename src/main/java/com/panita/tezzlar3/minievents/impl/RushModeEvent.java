package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.minievents.MiniEvent;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class RushModeEvent implements MiniEvent, Listener {

    private int activeMode = -1; // 0 = Double Health, 1 = No Hunger, 2 = Speed III
    private long startTimestamp = 0;
    private final Random random = new Random();

    @Override
    public void start(JavaPlugin plugin) {
        activeMode = random.nextInt(3);
        startTimestamp = System.currentTimeMillis();
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        for (Player player : Bukkit.getOnlinePlayers()) {
            applyRushEffect(player);
        }
    }

    @Override
    public void stop(JavaPlugin plugin) {
        HandlerList.unregisterAll(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            removeRushEffect(player);
        }
        
        activeMode = -1;
    }

    private void applyRushEffect(Player player) {
        if (activeMode == 0) {
            // Double Health
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(maxHealth.getBaseValue() * 2.0);
            }
        } else if (activeMode == 2) {
            // Speed III (infinite duration for the event, we remove it on stop)
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2, false, false, true));
        }
    }

    private void removeRushEffect(Player player) {
        if (activeMode == 0) {
            // Revert Double Health
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(maxHealth.getBaseValue() / 2.0);
            }
        } else if (activeMode == 2) {
            // Revert Speed III
            player.removePotionEffect(PotionEffectType.SPEED);
        }
    }

    @Override
    public String getId() {
        return "rush_mode";
    }

    @Override
    public String getDisplayName() {
        return "<#FFC019>Modo Rush</#FFC019>";
    }

    @Override
    public String getDescription() {
        return "\n&7Durante las próximas &b2 horas&7, el modo rush estará activo.\n\n&3- &7Los jugadores recibirán un efecto potenciador (Doble Vida, Sin Hambre, o Velocidad III) al azar.";
    }

    @Override
    public long getDurationTicks() {
        return 2 * 60 * 60 * 20L; // 2 hours
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        applyRushEffect(event.getPlayer());
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (activeMode == 1) {
            // No hunger for 1h max
            long elapsedMillis = System.currentTimeMillis() - startTimestamp;
            if (elapsedMillis <= 3600000L) { // 1 hour in ms
                if (event.getEntity() instanceof Player player) {
                    event.setFoodLevel(20);
                    player.setSaturation(20.0f);
                    event.setCancelled(true);
                }
            }
        }
    }
}

package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.minievents.MiniEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class RushModeEvent implements MiniEvent, Listener {

    private int activeMode = -1; // 0 = Double Health, 1 = No Hunger, 2 = Speed III
    private long startTimestamp = 0;
    private final Random random = new Random();

    private final NamespacedKey appliedKey = new NamespacedKey(Tezzlar.getInstance(), "rush_mode_applied");

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
        // We do NOT unregister the listener so that it can clean up offline players when they join
        activeMode = -1;

        for (Player player : Bukkit.getOnlinePlayers()) {
            removeRushEffect(player);
        }
    }

    private void applyRushEffect(Player player) {
        if (!player.getPersistentDataContainer().has(appliedKey, PersistentDataType.BYTE)) {
            player.getPersistentDataContainer().set(appliedKey, PersistentDataType.BYTE, (byte) 1);
            
            if (activeMode == 0) {
                // Double Health
                AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth != null) {
                    maxHealth.setBaseValue(maxHealth.getBaseValue() * 2.0);
                }
            } else if (activeMode == 1) {
                // No Hunger - set food and saturation to max at the start
                player.setFoodLevel(20);
                player.setSaturation(20.0f);
            } else if (activeMode == 2) {
                // Speed III (infinite duration for the event, we remove it on stop)
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 2, false, false, true));
            }
        }
    }

    private void removeRushEffect(Player player) {
        if (player.getPersistentDataContainer().has(appliedKey, PersistentDataType.BYTE)) {
            player.getPersistentDataContainer().remove(appliedKey);
            
            // Revert Double Health safely
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null && maxHealth.getBaseValue() > 20.0) {
                maxHealth.setBaseValue(maxHealth.getBaseValue() / 2.0);
            }
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
        if (activeMode == 0) {
            return "\n&7Durante las próximas &b2 horas&7, el modo rush estará activo.\n\n&3- &7Los jugadores recibirán el efecto &bDoble Vida&7.";
        } else if (activeMode == 1) {
            return "\n&7Durante la próxima &b1 hora&7, el modo rush estará activo.\n\n&3- &7Los jugadores recibirán el efecto &bSin Hambre&7.";
        } else {
            return "\n&7Durante las próximas &b2 horas&7, el modo rush estará activo.\n\n&3- &7Los jugadores recibirán el efecto &bVelocidad III&7.";
        }
    }

    @Override
    public long getDurationTicks() {
        if (activeMode == 1) {
            return 60 * 60 * 20L; // 1 hour
        }
        return 2 * 60 * 60 * 20L; // 2 hours
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (activeMode != -1) {
            applyRushEffect(event.getPlayer());
        } else {
            removeRushEffect(event.getPlayer());
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (activeMode == 1) {
            if (event.getEntity() instanceof Player player) {
                // Prevent food level from decreasing but let increase
                if (event.getFoodLevel() < player.getFoodLevel()) {
                    event.setCancelled(true);
                }
            }
        }
    }
}

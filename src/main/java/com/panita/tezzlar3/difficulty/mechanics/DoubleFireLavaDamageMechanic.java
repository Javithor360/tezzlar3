package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class DoubleFireLavaDamageMechanic extends DifficultyMechanic {

    private final NamespacedKey FIRE_TIME_KEY;

    public DoubleFireLavaDamageMechanic(JavaPlugin plugin) {
        super(plugin, 23);
        FIRE_TIME_KEY = new NamespacedKey(plugin, "fire_exposure_time");

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive() || TimeManager.getCurrentDay() < 24) return;
            
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!player.isValid() || player.isDead()) continue;
                
                boolean inLava = player.getLocation().getBlock().getType() == Material.LAVA || 
                                 player.getEyeLocation().getBlock().getType() == Material.LAVA;
                                 
                if (player.getFireTicks() > 0 || inLava) {
                    int time = player.getPersistentDataContainer().getOrDefault(FIRE_TIME_KEY, PersistentDataType.INTEGER, 0);
                    player.getPersistentDataContainer().set(FIRE_TIME_KEY, PersistentDataType.INTEGER, time + 1);
                } else {
                    player.getPersistentDataContainer().remove(FIRE_TIME_KEY);
                }
            }
        }, 20L, 20L); // 1 second intervals
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireLavaDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Player player) {
            DamageCause cause = event.getCause();
            if (cause == DamageCause.LAVA || cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK) {
                if (TimeManager.getCurrentDay() >= 24) {
                    int exposure = player.getPersistentDataContainer().getOrDefault(FIRE_TIME_KEY, PersistentDataType.INTEGER, 0);
                    // Starts at 1.5 multiplier, scales exponentially over time
                    double damage = event.getDamage() * 1.5 * Math.pow(1.15, exposure);
                    event.setDamage(Math.min(damage, 100.0)); // Cap to avoid unhandled errors
                } else {
                    // Day 23 logic
                    event.setDamage(event.getDamage() * 2.0);
                }
            }
        }
    }
}

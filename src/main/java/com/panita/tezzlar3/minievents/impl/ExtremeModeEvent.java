package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.minievents.MiniEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ExtremeModeEvent implements MiniEvent, Listener {

    private int taskId = -1;
    private boolean isActive = false;
    private final NamespacedKey originalHealthKey;

    public ExtremeModeEvent() {
        this.originalHealthKey = new NamespacedKey(Tezzlar.getInstance(), "extreme_mode_original_health");
        // Register listener permanently to handle players logging in after event stops
        Bukkit.getPluginManager().registerEvents(this, Tezzlar.getInstance());
    }

    @Override
    public void start(JavaPlugin plugin) {
        isActive = true;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyExtremeMode(player);
        }

        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getPersistentDataContainer().has(originalHealthKey, PersistentDataType.DOUBLE)) {
                    double targetHealth = player.getPersistentDataContainer().get(originalHealthKey, PersistentDataType.DOUBLE);
                    AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                    
                    if (maxHealth != null) {
                        double currentMax = maxHealth.getBaseValue();
                        if (currentMax < targetHealth) {
                            maxHealth.setBaseValue(Math.min(currentMax + 2.0, targetHealth));
                        }
                    }
                }
            }
        }, 1200L, 1200L).getTaskId(); // Every 60 seconds
    }

    @Override
    public void stop(JavaPlugin plugin) {
        isActive = false;
        
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            restoreHealth(player);
        }
    }

    private void applyExtremeMode(Player player) {
        if (!player.getPersistentDataContainer().has(originalHealthKey, PersistentDataType.DOUBLE)) {
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                player.getPersistentDataContainer().set(originalHealthKey, PersistentDataType.DOUBLE, maxHealth.getBaseValue());
                maxHealth.setBaseValue(2.0); // 1 heart
                if (player.getHealth() > 2.0) {
                    player.setHealth(2.0);
                }
            }
        }
    }

    private void restoreHealth(Player player) {
        if (player.getPersistentDataContainer().has(originalHealthKey, PersistentDataType.DOUBLE)) {
            double original = player.getPersistentDataContainer().get(originalHealthKey, PersistentDataType.DOUBLE);
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(original);
            }
            player.getPersistentDataContainer().remove(originalHealthKey);
        }
    }

    @Override
    public String getId() {
        return "extreme_mode";
    }

    @Override
    public String getDisplayName() {
        return "<#8B0000>Modo Extremo</#8B0000>";
    }

    @Override
    public String getDescription() {
        return "\n&7Durante la próxima &b1 hora&7, la supervivencia será extrema.\n\n&3- &7Todos los jugadores han sido reducidos a 1 corazón.\n&3- &7Recuperarás 1 corazón de vida máxima cada minuto.";
    }

    @Override
    public long getDurationTicks() {
        return 60 * 60 * 20L; // 1 hour
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (isActive) {
            applyExtremeMode(player);
        } else {
            // Clean up if they log in after event ended
            restoreHealth(player);
        }
    }
}

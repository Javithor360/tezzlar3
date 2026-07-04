package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.minievents.MiniEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import com.panita.tezzlar3.core.chat.Messenger;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.util.PlayerUtils;

public class HyperactivityEvent implements MiniEvent, Listener {

    private int taskId = -1;
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Integer> stillSeconds = new HashMap<>();

    @Override
    public void start(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Initialize online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            lastLocations.put(player.getUniqueId(), player.getLocation());
            stillSeconds.put(player.getUniqueId(), 0);
        }

        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                
                if (!PlayerUtils.isSurvival(player)) {
                    stillSeconds.put(uuid, 0);
                    continue;
                }
                
                Location current = player.getLocation();
                Location last = lastLocations.get(uuid);

                if (last != null) {
                    // Check if player has moved (ignoring rotation)
                    if (current.getWorld().equals(last.getWorld()) &&
                        current.getX() == last.getX() &&
                        current.getY() == last.getY() &&
                        current.getZ() == last.getZ()) {
                        
                        int seconds = stillSeconds.getOrDefault(uuid, 0) + 1;
                        stillSeconds.put(uuid, seconds);

                        int secondsLeft = 30 - seconds;
                        if (secondsLeft > 0 && secondsLeft <= 3) {
                            Messenger.prefixedSend(player, "<red>¡Muévete! Te quedan " + secondsLeft + " segundos antes de morir.</red>");
                        }

                        if (seconds >= 30) {
                            player.setHealth(0.0);
                            stillSeconds.put(uuid, 0); // Reset after death
                        }
                    } else {
                        // Player moved, reset counter
                        stillSeconds.put(uuid, 0);
                    }
                }

                // Update last location
                lastLocations.put(uuid, current);
            }
        }, 20L, 20L).getTaskId();
    }

    @Override
    public void stop(JavaPlugin plugin) {
        HandlerList.unregisterAll(this);

        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }

        lastLocations.clear();
        stillSeconds.clear();
    }

    @Override
    public String getId() {
        return "hyperactivity";
    }

    @Override
    public String getDisplayName() {
        return "<#FF4500>Hiperactividad</#FF4500>";
    }

    @Override
    public String getDescription() {
        return "\n&7Durante los próximos &b10 minutos&7, la hiperactividad te dominará.\n\n&3- &7Si te quedas completamente quieto por 30 segundos, morirás instantáneamente.";
    }

    @Override
    public long getDurationTicks() {
        return 10 * 60 * 20L; // 10 minutes
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (PlayerUtils.isSurvival(player)) {
            lastLocations.put(player.getUniqueId(), player.getLocation());
            stillSeconds.put(player.getUniqueId(), 0);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        lastLocations.remove(player.getUniqueId());
        stillSeconds.remove(player.getUniqueId());
        
        if (taskId != -1 && PlayerUtils.isSurvival(player)) {
            NamespacedKey key = new NamespacedKey(Tezzlar.getInstance(), "hyperactivity_penalty");
            player.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        }
    }
}

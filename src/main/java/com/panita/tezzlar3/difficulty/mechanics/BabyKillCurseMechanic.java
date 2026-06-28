package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BabyKillCurseMechanic extends DifficultyMechanic {

    private final Map<UUID, Long> cursedPlayers = new HashMap<>();

    public BabyKillCurseMechanic(JavaPlugin plugin) {
        super(plugin, 18);
        
        // Timer to restore scale
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            long now = System.currentTimeMillis();
            cursedPlayers.entrySet().removeIf(entry -> {
                if (now >= entry.getValue()) {
                    Player player = plugin.getServer().getPlayer(entry.getKey());
                    if (player != null && player.isOnline()) {
                        AttributeInstance scale = player.getAttribute(Attribute.SCALE);
                        if (scale != null) {
                            scale.setBaseValue(1.0);
                        }
                    }
                    return true;
                }
                return false;
            });
        }, 20L, 20L); // check every second
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBabyKill(EntityDeathEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity().getKiller() != null && event.getEntity() instanceof Ageable ageable && !ageable.isAdult()) {
            Player killer = event.getEntity().getKiller();
            
            AttributeInstance scale = killer.getAttribute(Attribute.SCALE);
            if (scale != null) {
                scale.setBaseValue(0.5);
                
                // 3 minutes = 180 seconds = 180000 ms
                cursedPlayers.put(killer.getUniqueId(), System.currentTimeMillis() + 180_000L);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isActive()) return;
        
        Player player = event.getPlayer();
        if (cursedPlayers.containsKey(player.getUniqueId())) {
            long expiry = cursedPlayers.get(player.getUniqueId());
            if (System.currentTimeMillis() < expiry) {
                // Still cursed
                AttributeInstance scale = player.getAttribute(Attribute.SCALE);
                if (scale != null) {
                    scale.setBaseValue(0.5);
                }
            } else {
                // Curse expired while offline
                cursedPlayers.remove(player.getUniqueId());
                AttributeInstance scale = player.getAttribute(Attribute.SCALE);
                if (scale != null) {
                    scale.setBaseValue(1.0);
                }
            }
        }
    }
}

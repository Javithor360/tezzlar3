package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.missions.MissionsModule;
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
import com.panita.tezzlar3.core.chat.actionbar.ActionBarManager;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarProvider;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.PlayerUtils;

public class BabyKillCurseMechanic extends DifficultyMechanic implements ActionBarProvider {

    private final Map<UUID, Long> cursedPlayers = new HashMap<>();
    private static BabyKillCurseMechanic instance;

    public BabyKillCurseMechanic(JavaPlugin plugin) {
        super(plugin, 18);
        instance = this;
        
        if (ActionBarManager.getInstance() != null) {
            ActionBarManager.getInstance().registerProvider(this);
        }
        
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

    public static boolean isCursed(Player player) {
        if (instance == null || !instance.isActive()) return false;
        if (!PlayerUtils.isSurvival(player)) return false;
        return instance.cursedPlayers.containsKey(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBabyKill(EntityDeathEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity().getKiller() != null && event.getEntity() instanceof Ageable ageable && !ageable.isAdult()) {
            Player killer = event.getEntity().getKiller();
            
            if (!PlayerUtils.isSurvival(killer)) return;
            
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
    
    @Override
    public String getId() {
        return "baby_kill_curse";
    }

    @Override
    public java.util.List<String> getTexts(Player player) {
        if (!isActive()) return null;
        if (!PlayerUtils.isSurvival(player)) return null;
        if (!cursedPlayers.containsKey(player.getUniqueId())) return null;

        long expiry = cursedPlayers.get(player.getUniqueId());
        long now = System.currentTimeMillis();
        
        if (now >= expiry) return null;
        
        int remaining = (int) ((expiry - now) / 1000);
        String timeStr = String.format("%02d:%02d", remaining / 60, remaining % 60);
        return java.util.Collections.singletonList("<gray>Tamaño reducido (" + timeStr + ")</gray>");
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.chat.Messenger;

public class TeleportingSpawnsMechanic extends DifficultyMechanic {

    public TeleportingSpawnsMechanic(JavaPlugin plugin) {
        super(plugin, 17);
    }

    // Use HIGH priority to ensure it captures the mob after
    // other mechanics in NORMAL have decided if it's a custom mob or not.
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMonsterSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
        
        if (event.getEntity() instanceof Monster) {
            // 0.5% probability (0.005)
            if (Math.random() < 0.005) {
                
                // Execute teleport 1 tick later to guarantee
                // that all spawn processes and attribute modifications (riders, armor, tags) are completed.
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (event.getEntity().isDead()) return;
                    
                    // Find the nearest player in a 64 block radius
                    Player nearest = EntityUtils.getNearestPlayer(event.getLocation(), 64.0);
                    if (nearest != null) {
                        event.getEntity().teleport(nearest.getLocation());
                        // Subtle visual and sound effect to warn the player of the jump scare
                        nearest.getWorld().playSound(nearest.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
                        nearest.getWorld().spawnParticle(Particle.PORTAL, nearest.getLocation(), 30, 0.5, 1.0, 0.5, 0.1);
                        Messenger.prefixedSend(nearest, "<red>Un mob se ha teletransportado a tu ubicación</red>");
                        
                        if (event.getEntity() instanceof org.bukkit.entity.Creeper creeper) {
                            creeper.setMaxFuseTicks(creeper.getMaxFuseTicks() * 2);
                            creeper.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "teleporting_creeper"), PersistentDataType.BYTE, (byte) 1);
                        }
                    }
                });
            }
        }
    }
}

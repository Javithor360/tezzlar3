package com.panita.tezzlar3.qol.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class MemoryEvokerListener implements Listener {

    private final NamespacedKey waveKey;
    private final NamespacedKey targetKey;
    private final NamespacedKey keyholderKey;

    public MemoryEvokerListener() {
        this.waveKey = new NamespacedKey(Tezzlar.getInstance(), "is_memory_mob");
        this.targetKey = new NamespacedKey(Tezzlar.getInstance(), "memory_target");
        this.keyholderKey = new NamespacedKey(Tezzlar.getInstance(), "is_memory_keyholder");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEvokerMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        if (!entity.getPersistentDataContainer().has(waveKey, PersistentDataType.STRING)) return;
        
        String waveId = entity.getPersistentDataContainer().get(waveKey, PersistentDataType.STRING);
        String targetUuidStr = entity.getPersistentDataContainer().get(targetKey, PersistentDataType.STRING);
        
        if (waveId == null || targetUuidStr == null) return;
        
        event.getDrops().clear(); // No loot from these mobs
        event.setDroppedExp(0);
        
        // Is it the keyholder?
        if (entity.getPersistentDataContainer().has(keyholderKey, PersistentDataType.BYTE)) {
            UUID targetUuid = UUID.fromString(targetUuidStr);
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetUuid);
            
            // Unban player
            HardcoreDataManager.setBanExpiration(targetUuid, target.getName(), 0L);
            
            // Visuals
            entity.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, entity.getLocation().add(0, 1, 0), 100, 1.0, 1.0, 1.0, 0.5);
            SoundUtils.playGlobal("ui.toast.challenge_complete", 1.0f, 1.0f);
            
            // Broadcast
            Messenger.prefixedBroadcast("<gold>¡La condena de <yellow>" + target.getName() + "</yellow> ha sido revocada! El jugador ya puede volver a entrar.</gold>");
            
            // Kill all other mobs in the same wave
            for (Entity e : entity.getWorld().getEntities()) {
                if (e.getUniqueId().equals(entity.getUniqueId())) continue;
                
                if (e.getPersistentDataContainer().has(waveKey, PersistentDataType.STRING)) {
                    String otherWaveId = e.getPersistentDataContainer().get(waveKey, PersistentDataType.STRING);
                    if (waveId.equals(otherWaveId)) {
                        if (e instanceof LivingEntity living) {
                            living.getWorld().spawnParticle(Particle.POOF, living.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.0);
                            living.remove();
                        }
                    }
                }
            }
        } else {
            // It was just a decoy.
            entity.getWorld().spawnParticle(Particle.POOF, entity.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.0);
        }
    }
}

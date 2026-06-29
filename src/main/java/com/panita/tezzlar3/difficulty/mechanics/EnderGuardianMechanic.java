package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;

import java.util.ArrayList;
import java.util.List;

public class EnderGuardianMechanic extends DifficultyMechanic {

    private final NamespacedKey ENDER_GUARDIAN_KEY;

    public EnderGuardianMechanic(JavaPlugin plugin) {
        super(plugin, 20);
        this.ENDER_GUARDIAN_KEY = new NamespacedKey(plugin, "is_enderguardian");
        
        CustomMobManager.register(CustomMobType.ENDER_GUARDIAN, this::spawnManual);
    }
    
    public void spawnManual(Location loc) {
        Enderman enderman = EntityUtils.spawnNatural(loc, EntityType.ENDERMAN);
        transform(enderman);
    }
    
    private void transform(Enderman enderman) {
        enderman.getPersistentDataContainer().set(ENDER_GUARDIAN_KEY, PersistentDataType.BYTE, (byte) 1);
        EntityUtils.setCustomName(enderman, CustomMobType.ENDER_GUARDIAN.getCustomName());
        
        Entity guardian = EntityUtils.spawnNatural(enderman.getLocation(), EntityType.GUARDIAN);
        guardian.setSilent(true);
        enderman.addPassenger(guardian);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEndermanSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getEntityType() == EntityType.ENDERMAN) {
            if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
            
            // 5% chance
            if (Math.random() < 0.05) {
                transform((Enderman) event.getEntity());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEndermanTeleport(EntityTeleportEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Enderman enderman) {
            if (enderman.getPersistentDataContainer().has(ENDER_GUARDIAN_KEY, PersistentDataType.BYTE)) {
                
                List<Entity> passengers = new ArrayList<>(enderman.getPassengers());
                if (!passengers.isEmpty()) {
                    enderman.eject();
                    
                    // Remount passengers next tick so they teleport along with the Enderman
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        if (enderman.isValid()) {
                            for (Entity p : passengers) {
                                if (p.isValid()) {
                                    enderman.addPassenger(p);
                                }
                            }
                        }
                    });
                }
            }
        }
    }
}

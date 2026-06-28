package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.util.EntityUtils;

public class BabyMobRiderMechanic extends DifficultyMechanic {

    public BabyMobRiderMechanic(JavaPlugin plugin) {
        super(plugin, 17);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBabySpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;

        if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
        
        if (event.getEntity() instanceof Ageable ageable && !ageable.isAdult()) {
            if (event.getEntity() instanceof WaterMob || event.getEntityType() == EntityType.TURTLE) return;
            
            // Spawn zombie rider
            Zombie zombie = (Zombie) event.getLocation().getWorld().spawnEntity(event.getLocation(), EntityType.ZOMBIE);
            ageable.addPassenger(zombie);
        }
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;

public class GuardianSquidMechanic extends DifficultyMechanic {

    public GuardianSquidMechanic(JavaPlugin plugin) {
        super(plugin, 16);
    }

    @EventHandler
    public void onSquidSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getEntityType() == EntityType.SQUID) {
            if (Math.random() < 0.50) {
                event.setCancelled(true);
                Entity guardian = event.getLocation().getWorld().spawnEntity(event.getLocation(), EntityType.GUARDIAN);
                NamespacedKey key = new NamespacedKey(plugin, "is_guardian_squid");
                guardian.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            }
        }
    }
}

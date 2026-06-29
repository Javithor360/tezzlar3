package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ravager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class NaturalRavagerMechanic extends DifficultyMechanic {
    private final Random random = new Random();
    private final NamespacedKey RAVAGER_KEY;

    public NaturalRavagerMechanic(JavaPlugin plugin) {
        super(plugin, 9); // Day 9
        RAVAGER_KEY = new NamespacedKey(plugin, "is_natural_ravager");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        // Ensure it's a natural spawn
        if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
        
        // Ensure it's a Creature (animals, monsters, etc.)
        if (!(event.getEntity() instanceof Creature)) return;
        
        // Avoid infinite recursion or replacing a ravager with a ravager
        if (event.getEntityType() == EntityType.RAVAGER) return;
        
        // Avoid replacing custom mobs
        if (EntityUtils.isCustomMob(event.getEntity())) return;
        
        // 0.5% chance (1 in 200)
        if (random.nextInt(1000) >= 5) return;
        
        event.setCancelled(true);
        
        Ravager ravager = (Ravager) EntityUtils.spawnNatural(event.getLocation(), EntityType.RAVAGER);
        ravager.getPersistentDataContainer().set(RAVAGER_KEY, PersistentDataType.BYTE, (byte) 1);
    }
}

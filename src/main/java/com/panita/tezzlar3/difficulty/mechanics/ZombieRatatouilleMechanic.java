package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import org.bukkit.Location;

import java.util.Random;

public class ZombieRatatouilleMechanic extends DifficultyMechanic {

    private final Random random = new Random();
    private final NamespacedKey RATATOUILLE_KEY;

    public ZombieRatatouilleMechanic(JavaPlugin plugin) {
        super(plugin, 15);
        this.RATATOUILLE_KEY = new NamespacedKey(plugin, "is_ratatouille");
        CustomMobManager.register(CustomMobType.ZOMBIE_RATATOUILLE, this::spawnManual);
    }
    
    public void spawnManual(Location loc) {
        Zombie zombie = loc.getWorld().spawn(loc, Zombie.class);
        transform(zombie);
    }
    
    private void transform(Zombie zombie) {
        zombie.getPersistentDataContainer().set(RATATOUILLE_KEY, PersistentDataType.BYTE, (byte) 1);
        EntityUtils.setCustomName(zombie, "<#FFA35C>Zombie Ratatouille</#FFA35C>");
        
        Rabbit rabbit = (Rabbit) zombie.getWorld().spawnEntity(zombie.getLocation(), EntityType.RABBIT);
        rabbit.getPersistentDataContainer().set(RATATOUILLE_KEY, PersistentDataType.BYTE, (byte) 1);
        
        EntityUtils.trySetAttribute(rabbit, Attribute.MAX_HEALTH, 5.0);
        rabbit.setHealth(5.0);
        
        zombie.addPassenger(rabbit);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onZombieSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        LivingEntity entity = event.getEntity();
        if (entity.getType() != EntityType.ZOMBIE) return;
        if (EntityUtils.isCustomMob(entity)) return;
        if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
        
        // 2% chance to spawn
        if (random.nextDouble() < 0.02) {
            transform((Zombie) entity);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onZombieDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Zombie zombie) {
            if (zombie.getPersistentDataContainer().has(RATATOUILLE_KEY, PersistentDataType.BYTE)) {
                // If it still has the rabbit passenger, cancel damage
                if (zombie.getPassengers().size() > 0) {
                    for (Entity passenger : zombie.getPassengers()) {
                        if (passenger instanceof Rabbit rabbit && rabbit.getPersistentDataContainer().has(RATATOUILLE_KEY, PersistentDataType.BYTE)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

public class InvisibleChargedCreeperMechanic extends DifficultyMechanic {

    public InvisibleChargedCreeperMechanic(JavaPlugin plugin) {
        super(plugin, 12);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreeperSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Creeper creeper) {
            creeper.setPowered(true);
            creeper.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCloudSpawn(EntitySpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof AreaEffectCloud cloud) {
            if (cloud.hasCustomEffect(PotionEffectType.INVISIBILITY)) {
                // Cancel potion cloud to avoid dropping invisibility on explosion
                event.setCancelled(true);
            }
        }
    }
}

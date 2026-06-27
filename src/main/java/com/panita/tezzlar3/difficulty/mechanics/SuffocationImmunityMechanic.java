package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;

public class SuffocationImmunityMechanic extends DifficultyMechanic {

    public SuffocationImmunityMechanic(JavaPlugin plugin) {
        super(plugin, 3);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!isActive()) return;

        if (event.getCause() == DamageCause.SUFFOCATION) {
            if (event.getEntity() instanceof LivingEntity entity) {
                // Immunity to suffocation for custom mobs and any mob that is riding a vehicle
                if (EntityUtils.isCustomMob(entity) || entity.getVehicle() != null) {
                    event.setCancelled(true);
                }
            }
        }
    }
}

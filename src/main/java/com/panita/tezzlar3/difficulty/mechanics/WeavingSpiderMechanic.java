package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WeavingSpiderMechanic extends DifficultyMechanic {

    public WeavingSpiderMechanic(JavaPlugin plugin) {
        super(plugin, 13);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpiderDamage(EntityDamageByEntityEvent event) {
        if (!isActive()) return;

        if (event.getEntity() instanceof Player player) {
            if (event.getDamager() instanceof Spider || event.getDamager() instanceof CaveSpider) {
                // Apply Weaving for 1200 ticks (60 seconds)
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAVING, 1200, 0, false, true));
            }
        }
    }
}

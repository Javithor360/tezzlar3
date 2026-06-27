package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public class WeavingSpiderMechanic extends DifficultyMechanic {

    public WeavingSpiderMechanic(JavaPlugin plugin) {
        super(plugin, 13);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpiderDamage(EntityDamageByEntityEvent event) {
        if (!isActive()) return;

        if (event.getEntity() instanceof Player player) {
            if (event.getDamager() instanceof Spider || event.getDamager() instanceof CaveSpider) {
                if (ThreadLocalRandom.current().nextDouble() <= 0.45) {
                    Block block = player.getLocation().getBlock();
                    if (block.isReplaceable()) {
                        block.setType(Material.COBWEB);
                    }
                }
            }
        }
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AngryWolfMechanic extends DifficultyMechanic {

    public AngryWolfMechanic(JavaPlugin plugin) {
        super(plugin, 7);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWolfSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;

        if (event.getEntity() instanceof Wolf wolf) {
            // Set the wolf to be angry immediately
            wolf.setAngry(true);

            // Increase its follow range to 64 blocks
            AttributeInstance followRange = wolf.getAttribute(Attribute.FOLLOW_RANGE);
            if (followRange != null) {
                followRange.setBaseValue(64.0);
            }

            // Delay by 1 tick to ensure it has spawned completely before assigning a target
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!wolf.isValid() || wolf.isDead()) return;

                Player nearest = EntityUtils.getNearestPlayer(wolf.getLocation(), 64.0);
                if (nearest != null) {
                    wolf.setTarget(nearest);
                }
            });
        }
    }
}

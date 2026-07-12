package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public class CopperAppleDropMechanic extends DifficultyMechanic {

    public CopperAppleDropMechanic(JavaPlugin plugin) {
        super(plugin, 28);
    }

    @EventHandler
    public void onMonsterDeath(EntityDeathEvent event) {
        if (!isActive()) return;

        if (event.getEntity() instanceof Monster && event.getEntity().getKiller() != null) {
            // 1% chance
            if (ThreadLocalRandom.current().nextDouble() < 0.01) {
                ItemStack copperApple = CustomItemManager.getItem("copper_apple");
                if (copperApple != null) {
                    event.getDrops().add(copperApple);
                }
            }
        }
    }
}

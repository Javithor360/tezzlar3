package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;

public class ToxicMeatMechanic extends DifficultyMechanic {

    private final EnumSet<Material> toxicItems = EnumSet.of(
            Material.GOLDEN_CARROT,
            Material.BEEF, Material.COOKED_BEEF,
            Material.PORKCHOP, Material.COOKED_PORKCHOP,
            Material.MUTTON, Material.COOKED_MUTTON,
            Material.CHICKEN, Material.COOKED_CHICKEN,
            Material.RABBIT, Material.COOKED_RABBIT
    );

    public ToxicMeatMechanic(JavaPlugin plugin) {
        super(plugin, 15);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!isActive()) return;
        
        if (toxicItems.contains(event.getItem().getType())) {
            Player player = event.getPlayer();
            // Delay by 1 tick so the vanilla eating effect doesn't overwrite it immediately
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.setFoodLevel(0);
                player.setSaturation(0.0f);
            });
        }
    }
}

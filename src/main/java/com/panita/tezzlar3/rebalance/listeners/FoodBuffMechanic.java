package com.panita.tezzlar3.rebalance.listeners;

import com.panita.tezzlar3.qol.util.CustomItemManager;

import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.timeline.util.TimeManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FoodBuffMechanic implements Listener {

    private final JavaPlugin plugin;

    public FoodBuffMechanic(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        
        Material type = event.getItem().getType();
        Player player = event.getPlayer();
        
        int currentFood = player.getFoodLevel();
        float currentSat = player.getSaturation();
        
        int addedFood = 0;
        float addedSat = 0;
        
        switch (type) {
            case BAKED_POTATO -> { addedFood = 7; addedSat = 9f; }
            case BEETROOT_SOUP -> { addedFood = 12; addedSat = 16f; }
            case BEETROOT -> { addedFood = 5; addedSat = 7f; }
            case BREAD -> { addedFood = 7; addedSat = 12f; }
            case CARROT -> { addedFood = 6; addedSat = 12f; }
            case RABBIT_STEW -> { addedFood = 12; addedSat = 25f; }
            case PUMPKIN_PIE -> { addedFood = 10; addedSat = 14.5f; }
            case COOKIE -> { addedFood = 7; addedSat = 9f; }
            case GLOW_BERRIES -> { addedFood = 7; addedSat = 9f; }
            case SWEET_BERRIES -> { addedFood = 7; addedSat = 9f; }
            case DRIED_KELP -> { addedFood = 4; addedSat = 4f; }
            default -> { return; }
        }
        
        final int finalAddedFood = addedFood;
        final float finalAddedSat = addedSat;

        // Schedule the overwrite 1 tick later to erase vanilla healing
        Bukkit.getScheduler().runTask(plugin, () -> {
            int day = TimeManager.getCurrentDay();
            
            // Food and saturation changes apply from day 15 onwards
            if (day >= 15 && finalAddedFood > 0) {
                player.setFoodLevel(Math.min(20, currentFood + finalAddedFood));
                player.setSaturation(Math.min(player.getFoodLevel(), currentSat + finalAddedSat));
            }
            
            // Extra effects apply from day 2 onwards
            if (day >= 2) {
                if (type == Material.CARROT) {
                    if (CustomItemManager.isCustomItem(event.getItem(), "copper_carrot")) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60, 0));
                    }
                } else if (type == Material.RABBIT_STEW) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 60, 1));
                } else if (type == Material.PUMPKIN_PIE) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60, 0));
                } else if (type == Material.COOKIE) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 1));
                } else if (type == Material.GLOW_BERRIES) {
                    EntityUtils.setColoredGlowing(player, NamedTextColor.GREEN);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline()) {
                            EntityUtils.removeColoredGlowing(player);
                        }
                    }, 20 * 60);
                } else if (type == Material.SWEET_BERRIES) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0));
                } else if (type == Material.DRIED_KELP) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 30, 0));
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCakeInteract(PlayerInteractEvent event) {
        if (TimeManager.getCurrentDay() < 15) return;
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.CAKE) {
                Player player = event.getPlayer();
                
                // Only if they actually have hunger
                if (player.getFoodLevel() < 20) {
                    int preFood = player.getFoodLevel();
                    float preSat = player.getSaturation();
                    
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        // If vanilla food level increased, it means they successfully took a bite
                        if (player.getFoodLevel() > preFood) {
                            player.setFoodLevel(Math.min(20, preFood + 10));
                            player.setSaturation(Math.min(player.getFoodLevel(), preSat + 10f));
                        }
                    });
                }
            }
        }
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.core.util.SoundUtils;

public class SpecialBlocksMechanic extends DifficultyMechanic {

    public SpecialBlocksMechanic(JavaPlugin plugin) {
        super(plugin, 14); // Active from day 14

        // Runs every 10 ticks (0.5 seconds)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;

            // Process players
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead() || player.isFlying() || !PlayerUtils.isSurvival(player)) continue;

                Block blockBelow = player.getLocation().clone().subtract(0, 0.1, 0).getBlock();
                Material type = blockBelow.getType();

                switch (type) {
                    case BEDROCK:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0));
                        break;
                    case DIAMOND_BLOCK:
                        if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
                            double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                            if (player.getHealth() < maxHealth) {
                                player.setHealth(maxHealth);
                                SoundUtils.playInRadius(player.getLocation(), "entity.player.levelup", 1.0f, 2.0f);
                            }
                        }
                        break;
                    case NETHERITE_BLOCK:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1));
                        break;
                    case EMERALD_BLOCK:
                        if (player.getFoodLevel() < 20 && Math.random() < 0.2) {
                            player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
                        }
                        break;
                    case PRISMARINE:
                    case PRISMARINE_BRICKS:
                    case DARK_PRISMARINE:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 60, 0));
                        break;
                    case HAY_BLOCK:
                        if (!player.getActivePotionEffects().isEmpty()) {
                            for (PotionEffect effect : player.getActivePotionEffects()) {
                                player.removePotionEffect(effect.getType());
                            }
                        }
                        break;
                    case SCULK_CATALYST:
                        if (player.getLevel() < 30) {
                            if (Math.random() < 0.3) {
                                player.giveExp(1);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            // Process monsters for Emerald Ore
            for (World world : Bukkit.getWorlds()) {
                for (Monster monster : world.getEntitiesByClass(Monster.class)) {
                    if (monster.isDead()) continue;
                    
                    Block blockBelow = monster.getLocation().clone().subtract(0, 0.1, 0).getBlock();
                    Material type = blockBelow.getType();
                    
                    if (type == Material.EMERALD_ORE || type == Material.DEEPSLATE_EMERALD_ORE) {
                        double currentHealth = monster.getHealth();
                        if (currentHealth > 0) {
                            if (currentHealth <= 2.0) {
                                monster.setHealth(0.1);
                                monster.damage(10.0);
                            } else {
                                monster.setHealth(currentHealth - 2.0);
                                monster.damage(0.00001);
                            }
                            SoundUtils.playInRadius(monster.getLocation(), "entity.generic.burn", 1.0f, 1.0f);
                        }
                    }
                }
            }

        }, 10L, 10L);
    }
}

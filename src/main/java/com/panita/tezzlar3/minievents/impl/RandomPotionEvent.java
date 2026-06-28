package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.minievents.MiniEvent;
import com.panita.tezzlar3.core.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class RandomPotionEvent implements MiniEvent {

    private final Random random = new Random();

    @Override
    public void start(JavaPlugin plugin) {
        PotionEffectType[] randomEffects = {
                PotionEffectType.SPEED, PotionEffectType.RESISTANCE, PotionEffectType.FIRE_RESISTANCE,
                PotionEffectType.HASTE, PotionEffectType.STRENGTH, PotionEffectType.JUMP_BOOST,
                PotionEffectType.INVISIBILITY, PotionEffectType.REGENERATION, PotionEffectType.ABSORPTION
        };
        PotionEffectType chosen = randomEffects[random.nextInt(randomEffects.length)];
        int level = random.nextInt(3); // 0, 1, 2
        
        // 10 minutes
        PotionEffect effect = new PotionEffect(chosen, 10 * 60 * 20, level);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (PlayerUtils.isSurvival(player)) {
                player.addPotionEffect(effect);
            }
        }
    }

    @Override
    public void stop(JavaPlugin plugin) {
        // Effect expires on its own
    }

    @Override
    public String getId() {
        return "random_potion";
    }

    @Override
    public String getDisplayName() {
        return "<light_purple><b>Poción Sorpresa</b></light_purple>";
    }

    @Override
    public String getDescription() {
        return "<gray>Todos los jugadores han recibido un efecto de poción al azar durante 10 minutos.</gray>";
    }

    @Override
    public long getDurationTicks() {
        return 0L; // Instant event
    }
}

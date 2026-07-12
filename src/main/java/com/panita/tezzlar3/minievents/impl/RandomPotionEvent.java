package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.minievents.MiniEvent;
import com.panita.tezzlar3.core.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class  RandomPotionEvent implements MiniEvent {

    private final Random random = new Random();

    public static PotionEffect generateRandomEffect(int durationTicks) {
        PotionEffectType[] randomEffects = {
                PotionEffectType.ABSORPTION, PotionEffectType.UNLUCK, PotionEffectType.BAD_OMEN, PotionEffectType.BLINDNESS,
                PotionEffectType.BREATH_OF_THE_NAUTILUS, PotionEffectType.CONDUIT_POWER, PotionEffectType.DARKNESS, PotionEffectType.DOLPHINS_GRACE,
                PotionEffectType.FIRE_RESISTANCE, PotionEffectType.GLOWING, PotionEffectType.HASTE, PotionEffectType.HEALTH_BOOST,
                PotionEffectType.HERO_OF_THE_VILLAGE, PotionEffectType.HUNGER, PotionEffectType.INFESTED, PotionEffectType.INSTANT_DAMAGE,
                PotionEffectType.INSTANT_HEALTH, PotionEffectType.INVISIBILITY, PotionEffectType.JUMP_BOOST, PotionEffectType.MINING_FATIGUE,
                PotionEffectType.LEVITATION, PotionEffectType.LUCK, PotionEffectType.NAUSEA, PotionEffectType.NIGHT_VISION,
                PotionEffectType.OOZING, PotionEffectType.POISON, PotionEffectType.RAID_OMEN, PotionEffectType.REGENERATION,
                PotionEffectType.RESISTANCE, PotionEffectType.SLOWNESS, PotionEffectType.SLOW_FALLING, PotionEffectType.SPEED,
                PotionEffectType.STRENGTH, PotionEffectType.TRIAL_OMEN, PotionEffectType.WATER_BREATHING, PotionEffectType.WEAKNESS,
                PotionEffectType.WEAVING, PotionEffectType.WIND_CHARGED, PotionEffectType.WITHER
        };
        Random r = new Random();
        PotionEffectType chosen = randomEffects[r.nextInt(randomEffects.length)];
        int level = r.nextInt(3); // 0, 1, 2
        return new PotionEffect(chosen, durationTicks, level);
    }

    @Override
    public void start(JavaPlugin plugin) {
        PotionEffect effect = generateRandomEffect(10 * 60 * 20);
        
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
    public boolean canExecute() {
        return com.panita.tezzlar3.timeline.util.TimeManager.getCurrentDay() < 27;
    }

    @Override
    public String getId() {
        return "random_potion";
    }

    @Override
    public String getDisplayName() {
        return "<#9F5EFF>Poción Sorpresa</#9F5EFF>";
    }

    @Override
    public String getDescription() {
        return "\n&7Todos los jugadores han recibido un efecto de poción al azar durante 10 minutos.";
    }

    @Override
    public long getDurationTicks() {
        return 0L; // Instant event
    }
}

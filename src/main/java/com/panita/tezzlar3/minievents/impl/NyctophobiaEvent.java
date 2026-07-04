package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.minievents.MiniEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class NyctophobiaEvent implements MiniEvent {

    private int taskId = -1;
    private final Random random = new Random();

    // List of negative effects and their amplifiers (0-indexed)
    private final List<NyctoEffect> negativeEffects = Arrays.asList(
            new NyctoEffect(PotionEffectType.MINING_FATIGUE, 0),
            new NyctoEffect(PotionEffectType.SLOWNESS, 3), // SLOWNESS IV
            new NyctoEffect(PotionEffectType.BLINDNESS, 0),
            new NyctoEffect(PotionEffectType.NAUSEA, 0),
            new NyctoEffect(PotionEffectType.POISON, 1) // POISON II
    );

    @Override
    public void start(JavaPlugin plugin) {
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getLocation().getBlock().getLightLevel() == 0) {
                    applyNyctophobiaEffect(player);
                }
            }
        }, 100L, 100L).getTaskId(); // Every 5 seconds
    }

    @Override
    public void stop(JavaPlugin plugin) {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void applyNyctophobiaEffect(Player player) {
        long activeCount = player.getActivePotionEffects().stream()
                .filter(effect -> isNyctoEffect(effect.getType()))
                .count();

        if (activeCount < 2) {
            NyctoEffect chosen = negativeEffects.get(random.nextInt(negativeEffects.size()));
            player.addPotionEffect(new PotionEffect(chosen.type, 600, chosen.amplifier)); // 30 seconds
        }
    }

    private boolean isNyctoEffect(PotionEffectType type) {
        for (NyctoEffect effect : negativeEffects) {
            if (effect.type.equals(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getId() {
        return "nyctophobia";
    }

    @Override
    public String getDisplayName() {
        return "<#4B0082>Nictofobia</#4B0082>";
    }

    @Override
    public String getDescription() {
        return "\n&7Durante las próximas &b4 horas&7, la oscuridad te aterrorizará.\n\n&3- &7Estar en completa oscuridad (nivel de luz 0) te causará efectos negativos aleatorios.";
    }

    @Override
    public long getDurationTicks() {
        return 4 * 60 * 60 * 20L; // 4 hours
    }

    private static class NyctoEffect {
        final PotionEffectType type;
        final int amplifier;

        NyctoEffect(PotionEffectType type, int amplifier) {
            this.type = type;
            this.amplifier = amplifier;
        }
    }
}

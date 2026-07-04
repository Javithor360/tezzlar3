package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.minievents.MiniEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;

public class MermaidModeEvent implements MiniEvent {

    private int taskId = -1;

    @Override
    public void start(JavaPlugin plugin) {
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isInWater()) {
                    player.setRemainingAir(player.getMaximumAir());
                } else {
                    int newAir = player.getRemainingAir() - 20; // 1 second worth of air
                    if (newAir <= 0) {
                        player.setRemainingAir(0);
                        player.damage(2.0); // 1 heart of drowning damage
                    } else {
                        player.setRemainingAir(newAir);
                    }
                }
            }
        }, 20L, 20L).getTaskId();
    }

    @Override
    public void stop(JavaPlugin plugin) {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setRemainingAir(player.getMaximumAir());
        }
    }

    @Override
    public String getId() {
        return "mermaid_mode";
    }

    @Override
    public String getDisplayName() {
        return "<#00BFFF>Modo Sirena</#00BFFF>";
    }

    @Override
    public String getDescription() {
        return "\n&7Durante las próximas &b2 horas&7, el modo sirena estará activo.\n\n&3- &7Solo podrás respirar bajo el agua.\n&3- &7Si sales a la superficie o te quedas en tierra, comenzarás a ahogarte.";
    }

    @Override
    public long getDurationTicks() {
        return 2 * 60 * 60 * 20L; // 2 hours
    }
}

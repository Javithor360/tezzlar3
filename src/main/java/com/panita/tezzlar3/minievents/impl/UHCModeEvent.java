package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.minievents.MiniEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class UHCModeEvent implements MiniEvent {

    @Override
    public void start(JavaPlugin plugin) {
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRules.NATURAL_HEALTH_REGENERATION, false);
        }
    }

    @Override
    public void stop(JavaPlugin plugin) {
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRules.NATURAL_HEALTH_REGENERATION, true);
        }
    }

    @Override
    public String getId() {
        return "uhc_mode";
    }

    @Override
    public String getDisplayName() {
        return "<#F0162E>Modo UHC</#F0162E>";
    }

    @Override
    public String getDescription() {
        return "\n&7Durante las próximas &b2 horas&7, el modo ultra hardcore estará activo.\n\n&3- &7No se podrá regenerar vida naturalmente.\n&3- &7Para curarte debes utilizar pociones o manzanas.\n\n&7Por lo tanto, se recomienda andar con extremo cuidado.";
    }

    @Override
    public long getDurationTicks() {
        return 2 * 60 * 60 * 20L; // 2 hours
    }
}

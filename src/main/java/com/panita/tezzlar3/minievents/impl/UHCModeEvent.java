package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.minievents.MiniEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class UHCModeEvent implements MiniEvent, Listener {

    @Override
    public void start(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop(JavaPlugin plugin) {
        HandlerList.unregisterAll(this);
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
        return "&7Durante las próximas &b2 horas&7, el modo ultra hardcore estará activo.<newline>&3- &7No se podrá regenerar vida naturalmente.<newline>&3- &7Para curarte debes utilizar pociones.<newline>&7Por lo tanto, se recomienda andar con extremo cuidado.";
    }

    @Override
    public long getDurationTicks() {
        return 2 * 60 * 60 * 20L; // 2 hours
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED ||
                event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
                event.setCancelled(true);
            }
        }
    }
}

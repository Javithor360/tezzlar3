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
        return "<dark_red><b>Modo UHC</b></dark_red>";
    }

    @Override
    public String getDescription() {
        return "<gray>La regeneración natural ha sido bloqueada. Sólo pociones o manzanas doradas curarán a los jugadores.</gray>";
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

package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.minievents.MiniEvent;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class ResizeModeEvent implements MiniEvent, Listener {

    private final Random random = new Random();

    @Override
    public void start(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        for (Player player : Bukkit.getOnlinePlayers()) {
            applyResize(player);
        }
    }

    @Override
    public void stop(JavaPlugin plugin) {
        HandlerList.unregisterAll(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            removeResize(player);
        }
    }

    private void applyResize(Player player) {
        AttributeInstance scale = player.getAttribute(Attribute.SCALE);
        if (scale != null) {
            // Random double between 0.75 and 1.25
            double newScale = 0.75 + (random.nextDouble() * 0.5);
            scale.setBaseValue(newScale);
        }
    }

    private void removeResize(Player player) {
        AttributeInstance scale = player.getAttribute(Attribute.SCALE);
        if (scale != null) {
            scale.setBaseValue(1.0);
        }
    }

    @Override
    public String getId() {
        return "resize_mode";
    }

    @Override
    public String getDisplayName() {
        return "<#FF69B4>Cambio de Tamaño</#FF69B4>";
    }

    @Override
    public String getDescription() {
        return "\n&7Durante las próximas &b2 horas&7, los tamaños estarán alterados.\n\n&3- &7Cada jugador tendrá un tamaño aleatorio entre 0.75x y 1.25x.";
    }

    @Override
    public long getDurationTicks() {
        return 2 * 60 * 60 * 20L; // 2 hours
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        applyResize(event.getPlayer());
    }
}

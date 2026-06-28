package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.minievents.MiniEvent;
import com.panita.tezzlar3.core.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PositionSwapEvent implements MiniEvent {

    @Override
    public void start(JavaPlugin plugin) {
        List<Player> players = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (PlayerUtils.isSurvival(p)) {
                players.add(p);
            }
        }
        
        if (players.size() < 2) return;
        
        Collections.shuffle(players);
        
        // Store the location of the first player
        Location firstLoc = players.get(0).getLocation();
        
        // Cycle teleport
        for (int i = 0; i < players.size() - 1; i++) {
            players.get(i).teleport(players.get(i + 1).getLocation());
        }
        
        // Last player goes to first location
        players.get(players.size() - 1).teleport(firstLoc);
    }

    @Override
    public void stop(JavaPlugin plugin) {
    }

    @Override
    public String getId() {
        return "position_swap";
    }

    @Override
    public String getDisplayName() {
        return "<aqua><b>¡Intercambio de Posiciones!</b></aqua>";
    }

    @Override
    public String getDescription() {
        return "<gray>Las posiciones de todos los jugadores conectados han sido revueltas.</gray>";
    }

    @Override
    public long getDurationTicks() {
        return 0; // Instant
    }
}

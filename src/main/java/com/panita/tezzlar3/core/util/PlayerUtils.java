package com.panita.tezzlar3.core.util;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PlayerUtils {
    
    /**
     * Checks if the player is in Survival or Adventure mode.
     * Useful for validating if the player is vulnerable to custom mechanics
     * (damage, weather, aggressive mobs, etc.) and ignoring those in Creative or Spectator.
     */
    public static boolean isSurvival(Player player) {
        if (player == null) return false;
        GameMode mode = player.getGameMode();
        return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
    }
}

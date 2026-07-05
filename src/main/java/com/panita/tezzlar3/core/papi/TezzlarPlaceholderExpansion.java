package com.panita.tezzlar3.core.papi;

import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class TezzlarPlaceholderExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "tezzlar";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Javithor360";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || player.getName() == null) {
            return "";
        }
        
        if (params.equalsIgnoreCase("maxlives")) {
            return String.valueOf(HardcoreDataManager.getMaxLives(player.getUniqueId(), player.getName()));
        }

        if (params.equalsIgnoreCase("lives")) {
            return String.valueOf(HardcoreDataManager.getLives(player.getUniqueId(), player.getName()));
        }
        
        if (params.equalsIgnoreCase("consumedlives")) {
            int maxLives = HardcoreDataManager.getMaxLives(player.getUniqueId(), player.getName());
            int lives = HardcoreDataManager.getLives(player.getUniqueId(), player.getName());
            return String.valueOf(Math.max(0, maxLives - lives));
        }
        
        if (params.equalsIgnoreCase("deaths")) {
            return String.valueOf(HardcoreDataManager.getDeaths(player.getUniqueId(), player.getName()));
        }
        
        if (params.equalsIgnoreCase("totaldeaths")) {
            int maxLives = HardcoreDataManager.getMaxLives(player.getUniqueId(), player.getName());
            int lives = HardcoreDataManager.getLives(player.getUniqueId(), player.getName());
            int consumed = Math.max(0, maxLives - lives);
            int deaths = HardcoreDataManager.getDeaths(player.getUniqueId(), player.getName());
            return String.valueOf(consumed + deaths);
        }
        
        if (params.equalsIgnoreCase("belowname")) {
            int deaths = HardcoreDataManager.getDeaths(player.getUniqueId(), player.getName());
            int lives = HardcoreDataManager.getLives(player.getUniqueId(), player.getName());
            if (deaths >= 1) {
                String plural = (deaths == 1) ? "Muerte" : "Muertes";
                return "💀 " + deaths + " " + plural;
            } else {
                String plural = (lives == 1) ? "Vida" : "Vidas";
                return "❤ " + lives + " " + plural;
            }
        }
        
        return null;
    }
}

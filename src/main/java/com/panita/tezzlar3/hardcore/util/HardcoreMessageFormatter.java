package com.panita.tezzlar3.hardcore.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;

public class HardcoreMessageFormatter {
    
    /**
     * Processes placeholders in a Hardcore module message.
     * 
     * @param message The raw message from config.
     * @param player The player's name.
     * @param formattedBanDuration The formatted string of the ban duration (e.g., "8 horas"), can be null if not applicable.
     * @return The message with all placeholders replaced.
     **/
    public static String processPlaceholders(String message, OfflinePlayer player, String formattedBanDuration) {
        if (message == null) return "";
        
        String result = message;
        if (player != null) {
            result = result.replace("%player_name%", player.getName() != null ? player.getName() : "Desconocido");
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                result = PlaceholderAPI.setPlaceholders(player, result);
            }
        } else {
            result = result.replace("%player_name%", "Desconocido");
        }
                               
        if (formattedBanDuration != null) {
            result = result.replace("%ban_duration%", formattedBanDuration);
        }
        
        return result;
    }
}

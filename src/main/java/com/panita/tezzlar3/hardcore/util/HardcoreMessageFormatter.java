package com.panita.tezzlar3.hardcore.util;

public class HardcoreMessageFormatter {
    
    /**
     * Processes placeholders in a Hardcore module message.
     * 
     * @param message The raw message from config.
     * @param playerName The player's name.
     * @param deaths The player's current deaths.
     * @param formattedBanDuration The formatted string of the ban duration (e.g., "8 horas"), can be null if not applicable.
     * @return The message with all placeholders replaced.
     */
    public static String processPlaceholders(String message, String playerName, int deaths, String formattedBanDuration) {
        if (message == null) return "";
        
        String result = message.replace("%player_name%", playerName != null ? playerName : "Desconocido")
                               .replace("%deaths%", String.valueOf(deaths));
                               
        if (formattedBanDuration != null) {
            result = result.replace("%ban_duration%", formattedBanDuration);
        }
        
        return result;
    }
}

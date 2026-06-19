package com.panita.tezzlar3.hardcore.listeners;

import com.panita.tezzlar3.core.chat.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;

import java.util.Date;

public class PlayerLoginListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        java.util.UUID playerUUID = event.getUniqueId();
        long banExpiration = HardcoreDataManager.getBanExpiration(playerUUID);
        
        // Check if the player has an active custom ban
        if (banExpiration > System.currentTimeMillis()) {
            int deaths = HardcoreDataManager.getDeaths(playerUUID);
            String reason = "<dark_red><bold>¡Has perdido una vida Hardcore!</bold></dark_red>\n<red>Muertes totales: <yellow>" + deaths + "</yellow></red>";
            
            long diffMillis = banExpiration - System.currentTimeMillis();
            long diffHours = diffMillis / (60 * 60 * 1000);
            long days = diffHours / 24;
            long hours = diffHours % 24;
            long diffMinutes = (diffMillis / (60 * 1000)) % 60;
            
            String timeRemaining;
            if (days > 0) {
                timeRemaining = "en " + days + " días y " + hours + " horas";
            } else if (hours > 0) {
                timeRemaining = "en " + hours + " horas y " + diffMinutes + " minutos";
            } else {
                timeRemaining = "en " + diffMinutes + " minutos";
            }

            // Construct the dynamic message
            String customMessage = reason + "\n<gray>Tu exilio terminará <red>" + timeRemaining + "</red>.</gray>";
            
            // Disallow the connection manually with KICK_OTHER to bypass vanilla formatting
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messenger.mini(customMessage));
        }
        // If banExpiration is in the past, we simply let them connect. 
        // We do not modify the YAML asynchronously here to prevent thread-safety file corruption.
    }
}

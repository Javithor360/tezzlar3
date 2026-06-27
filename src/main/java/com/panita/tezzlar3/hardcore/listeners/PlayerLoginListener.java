package com.panita.tezzlar3.hardcore.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.hardcore.util.HardcoreConfigDefaults;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.Bukkit;

import java.util.UUID;
import com.panita.tezzlar3.hardcore.util.HardcoreMessageFormatter;

public class PlayerLoginListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID playerUUID = event.getUniqueId();
        long banExpiration = HardcoreDataManager.getBanExpiration(playerUUID, event.getName());
        
        // Check if the player has an active custom ban
        if (banExpiration > System.currentTimeMillis()) {
            int deaths = HardcoreDataManager.getDeaths(playerUUID, event.getName());
            long diffMillis = banExpiration - System.currentTimeMillis();
            
            String formattedTime = Global.formatDuration(diffMillis);
            
            // Fetch message from config
            String rawBanReason = Tezzlar.getConfigManager().getString(
                    "hardcore.messages.banMessage", 
                    HardcoreConfigDefaults.HARDCORE_BANMESSAGE
            );
            
            String customMessage = HardcoreMessageFormatter.processPlaceholders(rawBanReason, Bukkit.getOfflinePlayer(playerUUID), formattedTime);
            
            // Disallow the connection manually with KICK_OTHER to bypass vanilla formatting
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messenger.mini(customMessage));
        }
        // If banExpiration is in the past, we simply let them connect.
    }
}

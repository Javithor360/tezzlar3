package com.panita.tezzlar3.hardcore.listeners;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.hardcore.HardcoreModule;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Always force survival mode to override hardcore spectator
        player.setGameMode(GameMode.SURVIVAL);
        
        // Check if there is a pending kick/ban message
        if (HardcoreModule.getPendingKicks().containsKey(player.getUniqueId())) {
            String kickMessage = HardcoreModule.getPendingKicks().remove(player.getUniqueId());
            // Kick using Adventure API component through Messenger
            player.kick(Messenger.mini(kickMessage));
        }
    }
}

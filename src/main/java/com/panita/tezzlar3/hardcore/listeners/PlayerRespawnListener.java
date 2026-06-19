package com.panita.tezzlar3.hardcore.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.hardcore.HardcoreModule;
import org.bukkit.Bukkit;
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
        
        // Check if there is a pending kick/ban message
        if (HardcoreModule.getPendingKicks().containsKey(player.getUniqueId())) {
            String kickMessage = HardcoreModule.getPendingKicks().remove(player.getUniqueId());
            
            // Delay kick for 2 seconds (40 ticks)
            Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                if (player.isOnline()) {
                    // Force survival mode just before kick to override hardcore spectator
                    // so their save file remains SURVIVAL while banned.
                    player.setGameMode(GameMode.SURVIVAL);
                    // Kick using Adventure API component through Messenger
                    player.kick(Messenger.mini(kickMessage));
                }
            }, 40L);
        } else {
            // Always force survival mode to override hardcore spectator for normal respawns
            player.setGameMode(GameMode.SURVIVAL);
        }
    }
}

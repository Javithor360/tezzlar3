package com.panita.tezzlar3.missions.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.missions.data.PlayerDataManager;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.missions.util.MissionsConfigDefaults;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataListener implements Listener {
    private final PlayerDataManager dataManager;

    public PlayerDataListener(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        dataManager.loadPlayerData(player);

        PlayerMissionData data = dataManager.getPlayerData(player);
        if (data != null && !data.getActivePunishments().isEmpty() && !data.hasPunishmentsAcknowledged()) {
            Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                String warning = Tezzlar.getConfigManager().getString("missions.messages.punishment_warning", MissionsConfigDefaults.MISSIONS_MESSAGES_PUNISHMENT_WARNING);
                Messenger.prefixedSend(player, warning);
                data.setPunishmentsAcknowledged(true);
            }, 100L); // 5 seconds delay
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        dataManager.unloadPlayerData(event.getPlayer());
    }
}

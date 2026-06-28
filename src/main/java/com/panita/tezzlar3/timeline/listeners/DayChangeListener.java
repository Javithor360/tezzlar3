package com.panita.tezzlar3.timeline.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.timeline.events.DayChangeEvent;
import com.panita.tezzlar3.timeline.util.TimelineConfigDefaults;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.core.util.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;

public class DayChangeListener implements Listener {

    @EventHandler
    public void onDayChange(DayChangeEvent event) {
        String titleRaw = Tezzlar.getConfigManager().getString("timeline.messages.day_change_title", TimelineConfigDefaults.TIMELINE_MESSAGES_DAY_CHANGE_TITLE);
        String subtitleRaw = Tezzlar.getConfigManager().getString("timeline.messages.day_change_subtitle", TimelineConfigDefaults.TIMELINE_MESSAGES_DAY_CHANGE_SUBTITLE);
        String chatRaw = Tezzlar.getConfigManager().getString("timeline.messages.day_change_chat", TimelineConfigDefaults.TIMELINE_MESSAGES_DAY_CHANGE_CHAT);

        String title = titleRaw.replace("%day%", String.valueOf(event.getNewDay()));
        String subtitle = subtitleRaw.replace("%day%", String.valueOf(event.getNewDay()));
        String chat = chatRaw.replace("%day%", String.valueOf(event.getNewDay()));
        
        String soundRaw = Tezzlar.getConfigManager().getString("timeline.sound", TimelineConfigDefaults.TIMELINE_SOUND);

        Messenger.broadcast(chat);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Messenger.showTitle(player, title, subtitle, Duration.ofSeconds(1), Duration.ofSeconds(4), Duration.ofSeconds(1));
            
            PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
            if (data != null) {
                data.setDayChangeAcknowledged(event.getNewDay());
            }
        }

        if (soundRaw != null && !soundRaw.isEmpty() && !soundRaw.equalsIgnoreCase("none")) {
            String[] parts = soundRaw.split(";");
            if (parts.length > 0) {
                String soundName = parts[0];
                float volume = 1.0f;
                float pitch = 0.8f;
                if (parts.length > 1) {
                    try { volume = Float.parseFloat(parts[1]); } catch (NumberFormatException ignored) {}
                }
                if (parts.length > 2) {
                    try { pitch = Float.parseFloat(parts[2]); } catch (NumberFormatException ignored) {}
                }
                SoundUtils.playGlobal(soundName, volume, pitch);
            }
        }
    }
}

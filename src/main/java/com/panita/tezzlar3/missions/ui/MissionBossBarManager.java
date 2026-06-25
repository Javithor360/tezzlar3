package com.panita.tezzlar3.missions.ui;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.Mission;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.timeline.util.TimeManager;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class MissionBossBarManager implements Listener {
    private final Map<UUID, Set<String>> playerActiveBars = new HashMap<>();

    public MissionBossBarManager(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int currentDay = TimeManager.getCurrentDay();
            
            List<Mission> activeMissions = new ArrayList<>();
            for (Mission mission : MissionsModule.getMissionManager().getLoadedMissions().values()) {
                if (currentDay >= mission.getStartDay() && currentDay <= mission.getEndDay()) {
                    activeMissions.add(mission);
                }
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
                Set<String> displayedThisTick = new HashSet<>();
                
                int incompleteMissions = 0;
                
                for (Mission mission : activeMissions) {
                    if (data != null && data.hasCompleted(mission.getId())) {
                        continue; // Do not show completed missions
                    }
                    
                    incompleteMissions++;
                    
                    int currentProgress = 0;
                    if (mission.getScope().equalsIgnoreCase("GROUP")) {
                        currentProgress = MissionsModule.getGlobalMissionManager().getProgress(mission.getId());
                    } else {
                        if (data != null) {
                            currentProgress = data.getProgress(mission.getId());
                        }
                    }
                    
                    // Calculate smooth time progress using UTC-6 real world time
                    long totalMillis = (mission.getEndDay() - mission.getStartDay() + 1) * 24L * 60L * 60L * 1000L;
                    
                    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Mexico_City"));
                    ZonedDateTime today2PM = now.withHour(14).withMinute(0).withSecond(0).withNano(0);
                    
                    long millisPassedInCurrentDay;
                    if (now.isBefore(today2PM)) {
                        ZonedDateTime yesterday2PM = today2PM.minusDays(1);
                        millisPassedInCurrentDay = Duration.between(yesterday2PM, now).toMillis();
                    } else {
                        millisPassedInCurrentDay = Duration.between(today2PM, now).toMillis();
                    }
                    
                    long passedMillis = (currentDay - mission.getStartDay()) * 24L * 60L * 60L * 1000L + millisPassedInCurrentDay;
                    float timeProgress = 1.0f - ((float) passedMillis / totalMillis);
                    timeProgress = Math.max(0.0f, Math.min(1.0f, timeProgress));
                    
                    // Format color based on urgency
                    BossBar.Color barColor = BossBar.Color.BLUE;
                    if (timeProgress < 0.25f) barColor = BossBar.Color.RED;
                    else if (timeProgress < 0.5f) barColor = BossBar.Color.YELLOW;
                    
                    // Build text
                    String text = "<b><yellow>Día " + currentDay + "</yellow></b> <dark_gray>-</dark_gray> " + mission.getName() + " <gray>(" + currentProgress + "/" + mission.getObjectiveAmount() + ")</gray>";
                    String barId = "mission_" + mission.getId();
                    
                    Messenger.showBossBar(player, barId, text, barColor, BossBar.Overlay.PROGRESS, timeProgress);
                    displayedThisTick.add(barId);
                }
                
                if (incompleteMissions == 0) {
                    // Show default timeline bossbar
                    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Mexico_City"));
                    ZonedDateTime today2PM = now.withHour(14).withMinute(0).withSecond(0).withNano(0);
                    
                    long millisPassedInCurrentDay;
                    if (now.isBefore(today2PM)) {
                        ZonedDateTime yesterday2PM = today2PM.minusDays(1);
                        millisPassedInCurrentDay = Duration.between(yesterday2PM, now).toMillis();
                    } else {
                        millisPassedInCurrentDay = Duration.between(today2PM, now).toMillis();
                    }
                    
                    long totalDayMillis = 24L * 60L * 60L * 1000L;
                    float timeProgress = 1.0f - ((float) millisPassedInCurrentDay / totalDayMillis);
                    timeProgress = Math.max(0.0f, Math.min(1.0f, timeProgress));
                    
                    BossBar.Color barColor = BossBar.Color.BLUE;
                    
                    String text = "<gradient:#5FE2C5:#C6DEF1:#5FE2C5><shadow:#0D1E40:1>TEZZLAR</shadow></gradient> <dark_gray>-</dark_gray> <#F2E76B>Día " + currentDay + "</#F2E76B>";
                    String barId = "timeline_day_bar";
                    
                    Messenger.showBossBar(player, barId, text, barColor, BossBar.Overlay.PROGRESS, timeProgress);
                    displayedThisTick.add(barId);
                }
                
                // Clear bars that should no longer be displayed (expired or completed)
                Set<String> previous = playerActiveBars.getOrDefault(player.getUniqueId(), new HashSet<>());
                for (String id : previous) {
                    if (!displayedThisTick.contains(id)) {
                        Messenger.hideBossBar(player, id);
                    }
                }
                playerActiveBars.put(player.getUniqueId(), displayedThisTick);
            }
        }, 20L, 20L); // Update every second
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Set<String> bars = playerActiveBars.remove(player.getUniqueId());
        if (bars != null) {
            for (String id : bars) {
                Messenger.hideBossBar(player, id);
            }
        }
    }
}

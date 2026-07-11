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


import java.util.List;
import java.util.ArrayList;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class MissionBossBarManager implements Listener {
    private long tickCounter = 0;
    
    private static final Map<UUID, String> forcedMissions = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> forceUntil = new ConcurrentHashMap<>();

    public static void forceShowMission(Player player, String missionId) {
        if (player == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                forcedMissions.put(p.getUniqueId(), missionId);
                forceUntil.put(p.getUniqueId(), System.currentTimeMillis() + 5000);
            }
        } else {
            forcedMissions.put(player.getUniqueId(), missionId);
            forceUntil.put(player.getUniqueId(), System.currentTimeMillis() + 5000);
        }
    }

    public MissionBossBarManager(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            tickCounter++;
            int currentDay = TimeManager.getCurrentDay();
            
            List<Mission> activeMissions = new ArrayList<>();
            for (Mission mission : MissionsModule.getMissionManager().getLoadedMissions().values()) {
                if (currentDay >= mission.getStartDay() && currentDay <= mission.getEndDay()) {
                    activeMissions.add(mission);
                }
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
                
                List<Mission> incompleteMissions = new ArrayList<>();
                for (Mission mission : activeMissions) {
                    if (data == null || !data.hasCompleted(mission.getId())) {
                        incompleteMissions.add(mission);
                    }
                }
                
                if (incompleteMissions.isEmpty()) {
                    Messenger.hideBossBar(player, "missions_bar");
                    
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
                    
                    Messenger.showBossBar(player, "timeline_day_bar", text, barColor, BossBar.Overlay.PROGRESS, timeProgress);
                } else {
                    Messenger.hideBossBar(player, "timeline_day_bar");
                    
                    int index = (int) ((tickCounter / 30) % incompleteMissions.size());
                    
                    String forcedId = forcedMissions.get(player.getUniqueId());
                    if (forcedId != null && forceUntil.getOrDefault(player.getUniqueId(), 0L) > System.currentTimeMillis()) {
                        for (int i = 0; i < incompleteMissions.size(); i++) {
                            if (incompleteMissions.get(i).getId().equals(forcedId)) {
                                index = i;
                                break;
                            }
                        }
                    }
                    
                    Mission mission = incompleteMissions.get(index);
                    
                    int currentProgress = 0;
                    int maxProgress = mission.getObjectiveAmount();
                    
                    if (mission.getObjectiveTargetsMap() != null && !mission.getObjectiveTargetsMap().isEmpty()) {
                        maxProgress = 0;
                        for (Map.Entry<String, Integer> entry : mission.getObjectiveTargetsMap().entrySet()) {
                            maxProgress += entry.getValue();
                            if (data != null) {
                                currentProgress += data.getProgress(mission.getId() + "_sub_" + entry.getKey());
                            }
                        }
                    } else if (mission.getScope().equalsIgnoreCase("GROUP")) {
                        currentProgress = MissionsModule.getGlobalMissionManager().getProgress(mission.getId());
                    } else if (data != null) {
                        currentProgress = data.getProgress(mission.getId());
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
                    
                    BossBar.Color barColor = BossBar.Color.BLUE;
                    if (timeProgress < 0.25f) barColor = BossBar.Color.RED;
                    else if (timeProgress < 0.5f) barColor = BossBar.Color.YELLOW;
                    
                    String pagination = incompleteMissions.size() > 1 ? " <shadow:#000000:1><dark_gray>[" + (index + 1) + "/" + incompleteMissions.size() + "]</dark_gray></shadow>" : "";
                    String text = "<b><yellow>Día " + currentDay + "</yellow></b> <dark_gray>-</dark_gray> " + mission.getName() + " <gray>(" + currentProgress + "/" + maxProgress + ")</gray>" + pagination;
                    
                    Messenger.showBossBar(player, "missions_bar", text, barColor, BossBar.Overlay.PROGRESS, timeProgress);
                }
            }
        }, 20L, 20L); // Update every second
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Messenger.hideBossBar(player, "missions_bar");
        Messenger.hideBossBar(player, "timeline_day_bar");
    }
}

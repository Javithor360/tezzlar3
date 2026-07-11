package com.panita.tezzlar3.missions.listeners;

import com.panita.tezzlar3.core.config.CustomConfig;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.Mission;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.timeline.events.DayChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MissionExpirationListener implements Listener {
    private final JavaPlugin plugin;

    public MissionExpirationListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDayChange(DayChangeEvent event) {
        if (event.getNewDay() < event.getOldDay()) {
            rollbackFutureMissions(event.getNewDay());
        }
        evaluateExpirations(event.getNewDay());
    }

    private void rollbackFutureMissions(int newDay) {
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) return;

        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml") && !name.equals("global_missions.yml"));
        if (files == null) return;

        for (Map.Entry<String, Mission> entry : MissionsModule.getMissionManager().getLoadedMissions().entrySet()) {
            Mission mission = entry.getValue();

            boolean revokePunishments = newDay <= mission.getEndDay();
            boolean revokeCompletion = newDay < mission.getStartDay();

            if (!revokePunishments && !revokeCompletion) continue;
            
            if (revokeCompletion && mission.getScope().equalsIgnoreCase("GROUP")) {
                MissionsModule.getGlobalMissionManager().resetProgress(mission.getId());
            }

            for (File file : files) {
                String playerName = file.getName().replace(".yml", "");
                Player p = Bukkit.getPlayerExact(playerName);

                if (p != null && p.isOnline()) {
                    PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(p);
                    if (data != null) {
                        if (revokePunishments) {
                            for (Map<?, ?> punishmentMap : mission.getPunishments()) {
                                String id = (String) punishmentMap.get("id");
                                data.removePunishment(id);
                            }
                        }
                        if (revokeCompletion) {
                            data.getCompletedMissions().remove(mission.getId());
                            data.removePendingReward(mission.getId());
                            data.getActiveProgress().remove(mission.getId());
                        }
                    }
                } else {
                    CustomConfig customConfig = new CustomConfig(plugin, "data", file.getName());
                    FileConfiguration config = customConfig.getConfig();
                    boolean changed = false;

                    if (revokePunishments) {
                        List<String> activePunishments = config.getStringList("active_punishments");
                        for (Map<?, ?> punishmentMap : mission.getPunishments()) {
                            String id = (String) punishmentMap.get("id");
                            if (activePunishments.remove(id)) {
                                changed = true;
                            }
                        }
                        if (changed) {
                            config.set("active_punishments", activePunishments);
                        }
                    }

                    if (revokeCompletion) {
                        List<String> completed = config.getStringList("completed_missions");
                        if (completed.remove(mission.getId())) {
                            config.set("completed_missions", completed);
                            changed = true;
                        }

                        List<String> pending = config.getStringList("pending_rewards");
                        if (pending.remove(mission.getId())) {
                            config.set("pending_rewards", pending);
                            changed = true;
                        }

                        if (config.contains("progress." + mission.getId())) {
                            config.set("progress." + mission.getId(), null);
                            changed = true;
                        }
                    }

                    if (changed) {
                        customConfig.save();
                    }
                }
            }
        }
    }

    public void evaluateExpirations(int currentDay) {
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) return;
        
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml") && !name.equals("global_missions.yml"));
        if (files == null) return;

        for (Map.Entry<String, Mission> entry : MissionsModule.getMissionManager().getLoadedMissions().entrySet()) {
            Mission mission = entry.getValue();
            
            // If the mission expired yesterday (or earlier)
            if (currentDay > mission.getEndDay()) {
                
                if (!mission.getPunishments().isEmpty()) {
                    
                    for (File file : files) {
                        String playerName = file.getName().replace(".yml", "");
                        Player p = Bukkit.getPlayerExact(playerName);
                        
                        if (p != null && p.isOnline()) {
                            PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(p);
                            if (data != null && data.getFirstJoinDay() <= mission.getEndDay() && !data.hasCompleted(mission.getId())) {
                                for (Map<?, ?> punishmentMap : mission.getPunishments()) {
                                    String id = (String) punishmentMap.get("id");
                                    if (!data.hasPunishment(id)) {
                                        data.addPunishment(id);
                                        data.setPunishmentsAcknowledged(false);
                                    }
                                }
                            }
                        } else {
                            CustomConfig customConfig = new CustomConfig(plugin, "data", file.getName());
                            FileConfiguration config = customConfig.getConfig();
                            
                            int firstJoinDay = config.getInt("first_join_day", 1);
                            if (firstJoinDay <= mission.getEndDay()) {
                                List<String> completed = config.getStringList("completed_missions");
                                if (!completed.contains(mission.getId())) {
                                    List<String> activePunishments = config.getStringList("active_punishments");
                                    boolean changed = false;
                                    for (Map<?, ?> punishmentMap : mission.getPunishments()) {
                                        String id = (String) punishmentMap.get("id");
                                        if (!activePunishments.contains(id)) {
                                            activePunishments.add(id);
                                            changed = true;
                                        }
                                    }
                                    if (changed) {
                                        config.set("active_punishments", activePunishments);
                                        config.set("punishments_acknowledged", false);
                                        customConfig.save();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

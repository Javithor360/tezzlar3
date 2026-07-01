package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.config.Config;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.minievents.MiniEventManager;
import com.panita.tezzlar3.minievents.MiniEventsModule;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DeathTrainMechanic extends DifficultyMechanic {

    private static DeathTrainMechanic instance;
    private int remainingSeconds = 0;
    private boolean stormActive = false;

    public DeathTrainMechanic(JavaPlugin plugin) {
        super(plugin, 2); // Active from day 2
        instance = this;
        
        this.remainingSeconds = Config.raw().getInt("difficulty.death_train_seconds", 0);

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            if (remainingSeconds > 0) {
                remainingSeconds--;
                
                World overworld = Bukkit.getWorld(plugin.getConfig().getString("worldName", "world"));
                if (overworld != null) {
                    if (!overworld.hasStorm() || overworld.getWeatherDuration() < 100) {
                        overworld.setStorm(true);
                        overworld.setWeatherDuration(200);
                    }
                    stormActive = true;
                }
                
                if (remainingSeconds % 60 == 0 || remainingSeconds == 0) {
                    Tezzlar.getConfigManager().updateInt("difficulty.death_train_seconds", remainingSeconds, null);
                }
                
                String timeStr = Global.formatTimeTicks(remainingSeconds * 20L);
                String actionBarMsg = "<gray>DeathTrain activo por " + timeStr + "</gray>";
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!PlayerUtils.isSurvival(player)) continue;
                    
                    if (MissionsModule.getRefugeManager() != null && MissionsModule.getRefugeManager().isActive()) continue;
                    if (MiniEventsModule.getManager() != null && MiniEventsModule.getManager().getActiveEvent() != null) continue;
                    if (OverworldToxicityMechanic.isToxic(player)) continue;
                    if (BabyKillCurseMechanic.isCursed(player)) continue;
                    
                    Messenger.sendActionBar(player, actionBarMsg);
                }
            } else if (stormActive) {
                World overworld = Bukkit.getWorld(plugin.getConfig().getString("worldName", "world"));
                if (overworld != null) {
                    overworld.setStorm(false);
                }
                stormActive = false;
            }
        }, 20L, 20L);
    }

    public static DeathTrainMechanic getInstance() {
        return instance;
    }

    public void addDeathTrainTime() {
        if (!isActive()) return;
        
        int day = TimeManager.getCurrentDay();
        int secondsToAdd = day * 3600;
        
        remainingSeconds += secondsToAdd;
        Tezzlar.getConfigManager().updateInt("difficulty.death_train_seconds", remainingSeconds, null);
    }
}

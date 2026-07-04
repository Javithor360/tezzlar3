package com.panita.tezzlar3.missions;

import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.missions.data.GlobalMissionManager;
import com.panita.tezzlar3.missions.data.PlayerDataManager;
import com.panita.tezzlar3.missions.listeners.MissionExpirationListener;
import com.panita.tezzlar3.missions.listeners.MissionTracker;
import com.panita.tezzlar3.missions.listeners.PlayerDataListener;
import com.panita.tezzlar3.missions.refuge.RefugeManager;
import com.panita.tezzlar3.missions.MissionManager;
import com.panita.tezzlar3.timeline.util.TimeManager;
import com.panita.tezzlar3.missions.ui.MissionBossBarManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class MissionsModule implements PluginModule {
    private boolean enabled;
    public static final String PACKAGE_NAME = "com.panita.tezzlar3.missions";

    @Override
    public String id() {
        return "missions";
    }

    @Override
    public String basePackage() {
        return PACKAGE_NAME;
    }

    private static PlayerDataManager dataManager;
    private static MissionManager missionManager;
    private static GlobalMissionManager globalMissionManager;
    private static RefugeManager refugeManager;

    @Override
    public void onEnable(JavaPlugin plugin) {
        refugeManager = new RefugeManager(plugin);
        plugin.getServer().getPluginManager().registerEvents(refugeManager, plugin);
        missionManager = new MissionManager(plugin);
        globalMissionManager = new GlobalMissionManager(plugin);
        
        dataManager = new PlayerDataManager(plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDataListener(dataManager), plugin);
        
        MissionExpirationListener expirationListener = new MissionExpirationListener(plugin);
        expirationListener.evaluateExpirations(TimeManager.getCurrentDay());
        Bukkit.getPluginManager().registerEvents(expirationListener, plugin);
        
        MissionBossBarManager bossBarManager = new MissionBossBarManager(plugin);
        plugin.getServer().getPluginManager().registerEvents(bossBarManager, plugin);
        
        enabled = true;
        
        // Load data for online players (useful on reloads)
        for (Player player : Bukkit.getOnlinePlayers()) {
            dataManager.loadPlayerData(player);
        }
        
        // Execute expirations if the server was offline
        expirationListener.evaluateExpirations(TimeManager.getCurrentDay());
    }

    @Override
    public void onDisable(JavaPlugin plugin) {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        if (refugeManager != null) {
            HandlerList.unregisterAll(refugeManager);
        }
    }

    public static PlayerDataManager getDataManager() {
        return dataManager;
    }

    public static MissionManager getMissionManager() {
        return missionManager;
    }

    public static GlobalMissionManager getGlobalMissionManager() {
        return globalMissionManager;
    }
    
    public static RefugeManager getRefugeManager() {
        return refugeManager;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean value) {
        this.enabled = value;
    }
}

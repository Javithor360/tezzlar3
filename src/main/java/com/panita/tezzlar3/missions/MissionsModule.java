package com.panita.tezzlar3.missions;

import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.missions.data.PlayerDataManager;
import com.panita.tezzlar3.missions.listeners.PlayerDataListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

    @Override
    public void onEnable(JavaPlugin plugin) {
        missionManager = new MissionManager(plugin);
        
        dataManager = new PlayerDataManager(plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDataListener(dataManager), plugin);
        
        // Load data for online players (useful on reloads)
        for (Player player : Bukkit.getOnlinePlayers()) {
            dataManager.loadPlayerData(player);
        }
    }

    @Override
    public void onDisable(JavaPlugin plugin) {
        if (dataManager != null) {
            dataManager.saveAll();
        }
    }

    public static PlayerDataManager getDataManager() {
        return dataManager;
    }

    public static MissionManager getMissionManager() {
        return missionManager;
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

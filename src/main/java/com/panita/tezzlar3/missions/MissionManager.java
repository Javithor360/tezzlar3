package com.panita.tezzlar3.missions;

import com.panita.tezzlar3.core.config.CustomConfig;
import com.panita.tezzlar3.missions.data.Mission;
import com.panita.tezzlar3.missions.handlers.PunishmentHandler;
import com.panita.tezzlar3.missions.handlers.RewardHandler;
import com.panita.tezzlar3.missions.handlers.punishments.*;
import com.panita.tezzlar3.missions.handlers.rewards.CustomItemRewardHandler;
import com.panita.tezzlar3.missions.handlers.rewards.ExpRewardHandler;
import com.panita.tezzlar3.missions.handlers.rewards.ItemRewardHandler;
import com.panita.tezzlar3.missions.handlers.rewards.RemovePunishmentRewardHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MissionManager {
    private final JavaPlugin plugin;
    private final CustomConfig missionsConfig;
    private final Map<String, Mission> loadedMissions = new HashMap<>();
    
    private final Map<String, RewardHandler> rewardHandlers = new HashMap<>();
    private final Map<String, PunishmentHandler> punishmentHandlers = new HashMap<>();

    public MissionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        
        // Ensure default file is copied from jar if it doesn't exist
        File file = new File(plugin.getDataFolder(), "missions.yml");
        if (!file.exists()) {
            plugin.saveResource("missions.yml", false);
        }
        
        this.missionsConfig = new CustomConfig(plugin, "", "missions.yml");
        
        registerBaseHandlers();
        
        loadMissions();
    }

    private void registerBaseHandlers() {
        registerRewardHandler(new CustomItemRewardHandler());
        registerRewardHandler(new ItemRewardHandler());
        registerRewardHandler(new ExpRewardHandler());
        registerRewardHandler(new RemovePunishmentRewardHandler());
        
        DiamondBanPunishmentHandler diamondBan = new DiamondBanPunishmentHandler();
        registerPunishmentHandler(diamondBan);
        plugin.getServer().getPluginManager().registerEvents(diamondBan, plugin);
        
        DamageMultiplierPunishmentHandler damageMultiplier = new DamageMultiplierPunishmentHandler();
        registerPunishmentHandler(damageMultiplier);
        plugin.getServer().getPluginManager().registerEvents(damageMultiplier, plugin);

        RegenerationToWitherPunishmentHandler regenWither = new RegenerationToWitherPunishmentHandler();
        registerPunishmentHandler(regenWither);
        plugin.getServer().getPluginManager().registerEvents(regenWither, plugin);
        
        WitherSkeletonSpawnPunishmentHandler witherSkeleton = new WitherSkeletonSpawnPunishmentHandler(0.10); // 10% chance
        registerPunishmentHandler(witherSkeleton);
        plugin.getServer().getPluginManager().registerEvents(witherSkeleton, plugin);
        
        HostileIronGolemsPunishmentHandler hostileGolems = new HostileIronGolemsPunishmentHandler(plugin);
        registerPunishmentHandler(hostileGolems);

        BuffedIronGolemsPunishmentHandler buffedGolems = new BuffedIronGolemsPunishmentHandler(plugin);
        registerPunishmentHandler(buffedGolems);
        plugin.getServer().getPluginManager().registerEvents(buffedGolems, plugin);

        WardenNightmarePunishmentHandler wardenNightmare = new WardenNightmarePunishmentHandler(plugin);
        registerPunishmentHandler(wardenNightmare);
        plugin.getServer().getPluginManager().registerEvents(wardenNightmare, plugin);

        DayBanPunishmentHandler dayBan = new DayBanPunishmentHandler(plugin);
        registerPunishmentHandler(dayBan);

        OffhandRestrictionPunishmentHandler offhandRestriction = new OffhandRestrictionPunishmentHandler(plugin);
        registerPunishmentHandler(offhandRestriction);
    }

    public void loadMissions() {
        loadedMissions.clear();
        ConfigurationSection root = missionsConfig.getConfig().getConfigurationSection("missions");
        if (root == null) return;
        
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section != null) {
                loadedMissions.put(key, new Mission(key, section));
            }
        }
        plugin.getLogger().info("Loaded " + loadedMissions.size() + " missions from config.");
    }

    public Mission getMission(String id) {
        return loadedMissions.get(id);
    }
    
    public Map<String, Mission> getLoadedMissions() {
        return loadedMissions;
    }

    public void registerRewardHandler(RewardHandler handler) {
        rewardHandlers.put(handler.getId(), handler);
    }

    public void registerPunishmentHandler(PunishmentHandler handler) {
        punishmentHandlers.put(handler.getId(), handler);
    }
    
    public RewardHandler getRewardHandler(String id) {
        return rewardHandlers.get(id);
    }
    
    public PunishmentHandler getPunishmentHandler(String id) {
        return punishmentHandlers.get(id);
    }
}

package com.panita.tezzlar3.hardcore;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HardcoreModule implements PluginModule {
    private boolean enabled;
    public static String packageName = "com.panita.tezzlar3.hardcore";
    
    // Map to keep track of players that need to be kicked immediately upon respawn
    private static final Map<UUID, String> pendingKicks = new ConcurrentHashMap<>();

    @Override
    public String id() {
        return "hardcore";
    }

    @Override
    public String basePackage() {
        return packageName;
    }

    @Override
    public void onEnable(JavaPlugin plugin) {
        // Initialize the YAML data manager for tracking player deaths
        HardcoreDataManager.init(plugin);
        
        // Ensure that doImmediateRespawn is activated on all worlds
        for (World world : Bukkit.getWorlds()) {
            if (!world.getGameRuleValue(GameRules.IMMEDIATE_RESPAWN)) {
                world.setGameRule(GameRules.IMMEDIATE_RESPAWN, true);
                plugin.getLogger().info("[Hardcore] Activated InmediateRespawn Gamerule for the world: " + world.getName());
            }
        }
        
        // Initialize a native scoreboard objective for deaths (for historical/aesthetic purposes)
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard mainScoreboard = manager.getMainScoreboard();
        if (mainScoreboard.getObjective("tezzlar_deaths") == null) {
            mainScoreboard.registerNewObjective("tezzlar_deaths", Criteria.DEATH_COUNT, Messenger.mini("<gray>\uD83D\uDC80</gray> <red>Muertes</red>"));
            plugin.getLogger().info("[Hardcore] Registered new native scoreboard objective: tezzlar_deaths");
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    public static Map<UUID, String> getPendingKicks() {
        return pendingKicks;
    }
}

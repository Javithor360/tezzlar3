package com.panita.tezzlar3.missions.handlers.punishments;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.config.CustomConfig;
import com.panita.tezzlar3.missions.handlers.PunishmentHandler;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;

import java.util.List;

public class DayBanPunishmentHandler implements PunishmentHandler, Listener {
    
    public DayBanPunishmentHandler(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getId() {
        return "DAY_22_BAN";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        // Punishments are continuous. The ID is saved directly to the player's data.
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        if (TimeManager.getCurrentDay() == 22) {
            CustomConfig dataConfig = new CustomConfig(Tezzlar.getInstance(), "data", event.getName() + ".yml");
            List<String> activePunishments = dataConfig.getConfig().getStringList("active_punishments");
            
            if (activePunishments.contains(getId())) {
                String kickMsg = "<red>Has fallado la misión de la armadura.\nNo puedes entrar el día 22.</red>";
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Messenger.mini(kickMsg));
            }
        }
    }
}

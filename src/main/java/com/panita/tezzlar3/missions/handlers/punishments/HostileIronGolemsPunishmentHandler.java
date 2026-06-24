package com.panita.tezzlar3.missions.handlers.punishments;

import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.missions.handlers.PunishmentHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.ConfigurationSection;

public class HostileIronGolemsPunishmentHandler implements PunishmentHandler {
    
    public HostileIronGolemsPunishmentHandler(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
                if (data != null && data.hasPunishment(getId())) {
                    for (Entity entity : player.getNearbyEntities(48, 48, 48)) {
                        if (entity instanceof IronGolem golem) {
                            if (golem.getTarget() == null || !golem.getTarget().equals(player)) {
                                golem.setTarget(player);
                            }
                        }
                    }
                }
            }
        }, 100L, 100L); // Check every 5 seconds
    }

    @Override
    public String getId() {
        return "HOSTILE_IRON_GOLEMS";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        // Passive effect
    }
}

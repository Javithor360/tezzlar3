package com.panita.tezzlar3.missions.handlers.punishments;

import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.missions.handlers.PunishmentHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageMultiplierPunishmentHandler implements PunishmentHandler, Listener {
    
    @Override
    public String getId() {
        return "DAMAGE_MULTIPLIER";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        double multiplier = 1.0;
        if (args != null) {
            multiplier = args.getDouble("multiplier", 1.0);
        }
        
        PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
        if (data != null) {
            // Save the punishment along with its multiplier
            data.addPunishment(getId() + ":" + multiplier);
        }
    }
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
        if (data != null) {
            double highestMultiplier = 1.0;
            
            for (String punishment : data.getActivePunishments()) {
                if (punishment.startsWith(getId() + ":")) {
                    try {
                        double mult = Double.parseDouble(punishment.split(":")[1]);
                        if (mult > highestMultiplier) {
                            highestMultiplier = mult;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            
            if (highestMultiplier > 1.0) {
                event.setDamage(event.getDamage() * highestMultiplier);
            }
        }
    }
}

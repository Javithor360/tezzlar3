package com.panita.tezzlar3.missions.handlers.punishments;

import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.missions.handlers.PunishmentHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.configuration.ConfigurationSection;

public class RegenerationToWitherPunishmentHandler implements PunishmentHandler, Listener {
    @Override
    public String getId() {
        return "REGENERATION_TO_WITHER";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        // Nothing to do here, passive effect managed by events
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player player) {
            PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
            if (data != null && data.hasPunishment(getId())) {
                if (event.getNewEffect() != null && event.getNewEffect().getType().equals(PotionEffectType.REGENERATION)) {
                    // Cancel the original regeneration effect
                    event.setCancelled(true);
                    
                    // Apply Wither II instead (amplifier 1 = Level II)
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, event.getNewEffect().getDuration(), 1));
                }
            }
        }
    }
}

package com.panita.tezzlar3.missions.handlers.rewards;

import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.missions.handlers.RewardHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class RemovePunishmentRewardHandler implements RewardHandler {
    @Override
    public String getId() {
        return "REMOVE_PUNISHMENT";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        if (args == null) return;
        String punishmentId = args.getString("punishment_id");
        
        if (punishmentId != null) {
            PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
            if (data != null && data.hasPunishment(punishmentId)) {
                data.removePunishment(punishmentId);
            }
        }
    }
}

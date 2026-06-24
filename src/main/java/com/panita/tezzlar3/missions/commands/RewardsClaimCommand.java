package com.panita.tezzlar3.missions.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.missions.MissionManager;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.Mission;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.missions.handlers.RewardHandler;
import com.panita.tezzlar3.missions.util.MissionsConfigDefaults;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.panita.tezzlar3.Tezzlar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SubCommandSpec(
        parent = "rewards",
        name = "claim",
        description = "Claim your completed mission rewards.",
        syntax = "/rewards claim",
        permission = "tezzlar.command.rewards.claim",
        playerOnly = true
)
public class RewardsClaimCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
        if (data == null || data.getPendingRewards().isEmpty()) {
            String emptyMsg = Tezzlar.getConfigManager().getString("missions.messages.rewards_empty", MissionsConfigDefaults.MISSIONS_MESSAGES_REWARDS_EMPTY);
            Messenger.prefixedSend(player, emptyMsg);
            return;
        }

        List<String> toClaim = new ArrayList<>(data.getPendingRewards());
        data.clearPendingRewards();

        for (String missionId : toClaim) {
            Mission mission = MissionsModule.getMissionManager().getMission(missionId);
            if (mission != null) {
                for (Map<?, ?> rewardMap : mission.getRewards()) {
                    String id = (String) rewardMap.get("id");
                    RewardHandler handler = MissionsModule.getMissionManager().getRewardHandler(id);
                    if (handler != null) {
                        Map<String, Object> argsMap = (Map<String, Object>) rewardMap.get("args");
                        ConfigurationSection configArgs = new MemoryConfiguration();
                        if (argsMap != null) {
                            for (Map.Entry<String, Object> entry : argsMap.entrySet()) {
                                configArgs.set(entry.getKey(), entry.getValue());
                            }
                        }
                        handler.apply(player, configArgs);
                    }
                }
                
                String claimMsg = Tezzlar.getConfigManager().getString("missions.messages.reward_claimed", MissionsConfigDefaults.MISSIONS_MESSAGES_REWARD_CLAIMED);
                claimMsg = claimMsg.replace("%mission%", mission.getName());
                Messenger.prefixedSend(player, claimMsg);
            }
        }
    }
}

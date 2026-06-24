package com.panita.tezzlar3.missions.handlers.rewards;

import com.panita.tezzlar3.missions.handlers.RewardHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ExpRewardHandler implements RewardHandler {
    @Override
    public String getId() {
        return "GIVE_EXP";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        if (args == null) return;
        int levels = args.getInt("levels", 0);
        if (levels > 0) {
            player.giveExpLevels(levels);
        }
    }
}

package com.panita.tezzlar3.missions.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public interface RewardHandler {
    String getId();
    void apply(Player player, ConfigurationSection args);
}

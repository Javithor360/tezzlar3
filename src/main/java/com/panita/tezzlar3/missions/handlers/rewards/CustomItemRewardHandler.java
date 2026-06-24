package com.panita.tezzlar3.missions.handlers.rewards;

import com.panita.tezzlar3.missions.handlers.RewardHandler;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class CustomItemRewardHandler implements RewardHandler {
    @Override
    public String getId() {
        return "GIVE_CUSTOM_ITEM";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        if (args == null) return;
        String customId = args.getString("custom_id");
        int amount = args.getInt("amount", 1);
        
        if (customId != null) {
            ItemStack item = CustomItemManager.getItem(customId);
            if (item != null) {
                item.setAmount(amount);
                
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                if (!leftover.isEmpty()) {
                    for (ItemStack left : leftover.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), left);
                    }
                }
            }
        }
    }
}

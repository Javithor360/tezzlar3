package com.panita.tezzlar3.missions.handlers.rewards;

import com.panita.tezzlar3.missions.handlers.RewardHandler;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ItemRewardHandler implements RewardHandler {
    @Override
    public String getId() {
        return "GIVE_ITEM";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        if (args == null) return;
        String materialName = args.getString("material");
        int amount = args.getInt("amount", 1);
        
        if (materialName != null) {
            Material material = Material.matchMaterial(materialName);
            if (material != null) {
                ItemStack item = new ItemStack(material, amount);
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

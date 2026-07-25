package com.panita.tezzlar3.missions.handlers.rewards;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.missions.handlers.RewardHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

public class RemoveOverworldContaminationRewardHandler implements RewardHandler {
    @Override
    public String getId() {
        return "REMOVE_OVERWORLD_CONTAMINATION";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        NamespacedKey immuneKey = new NamespacedKey(Tezzlar.getInstance(), "immune_overworld_contamination");
        player.getPersistentDataContainer().set(immuneKey, PersistentDataType.BYTE, (byte) 1);
        
        NamespacedKey toxicityKey = new NamespacedKey(Tezzlar.getInstance(), "overworld_toxicity");
        player.getPersistentDataContainer().set(toxicityKey, PersistentDataType.INTEGER, 0);
        
        player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.HUNGER);
        player.removePotionEffect(PotionEffectType.WITHER);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
    }
}

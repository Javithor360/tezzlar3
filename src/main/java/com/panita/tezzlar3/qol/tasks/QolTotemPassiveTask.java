package com.panita.tezzlar3.qol.tasks;

import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class QolTotemPassiveTask extends BukkitRunnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack offHand = player.getInventory().getItemInOffHand();
            
            if (offHand.getType().isAir()) continue;

            if (CustomItemManager.isCustomItem(offHand, "axolotl_totem")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, false, false, true));
            } else if (CustomItemManager.isCustomItem(offHand, "sulfur_totem")) {
                // Requires longer duration to avoid night vision blind flashing
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0, false, false, true));
            } else if (CustomItemManager.isCustomItem(offHand, "tadpole_totem")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 0, false, false, true));
            } else if (CustomItemManager.isCustomItem(offHand, "ghast_totem")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, false, false, true));
            } else if (CustomItemManager.isCustomItem(offHand, "copper_totem")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0, false, false, true));
            } else if (CustomItemManager.isCustomItem(offHand, "fish_totem")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 60, 0, false, false, true));
            } else if (CustomItemManager.isCustomItem(offHand, "sniffer_totem")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 60, 0, false, false, true));
            } else if (CustomItemManager.isCustomItem(offHand, "turtle_totem")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0, false, false, true));
            } else if (CustomItemManager.isCustomItem(offHand, "chicken_totem")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, false, false, true));
            }
        }
    }
}

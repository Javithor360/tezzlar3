package com.panita.tezzlar3.qol.tasks;

import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class QolTotemPassiveTask extends BukkitRunnable {

    private final Map<UUID, Long> lastGoldenEffect = new HashMap<>();
    private final Random random = new Random();

    @Override
    public void run() {
        long now = System.currentTimeMillis();

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
            } else if (CustomItemManager.isCustomItem(offHand, "golden_totem")) {
                long lastTime = lastGoldenEffect.getOrDefault(player.getUniqueId(), 0L);
                
                // 30 seconds minimum between effects
                if (now - lastTime >= 30000) {
                    // Enforce maximum 10 minutes (600,000 ms)
                    boolean force = (now - lastTime >= 600000);
                    
                    // Average check per second to trigger randomly between 30s and 10m
                    if (force || random.nextInt(300) == 0) {
                        lastGoldenEffect.put(player.getUniqueId(), now);
                        
                        // 1% chance to get Resistance 3 instead of the fart sound
                        if (random.nextInt(100) == 0) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 60 * 3, 2)); // Resistance 3 for 3 min
                        } else {
                            // Play the oxidized copper trumpet sound with low pitch
                            SoundUtils.play(player, "block.note_block.trumpet_oxidized", 1.0f, 0.5f);
                        }
                    }
                }
            }
        }
    }
}

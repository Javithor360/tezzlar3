package com.panita.tezzlar3.difficulty.mechanics;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.SoundUtils;
import java.util.Random;

public class ElytraNerfMechanic extends DifficultyMechanic {
    private final Random random = new Random();

    public ElytraNerfMechanic(JavaPlugin plugin) {
        super(plugin, 26);
    }

    @EventHandler
    public void onElytraDamage(PlayerItemDamageEvent event) {
        if (!isActive()) return;
        
        ItemStack item = event.getItem();
        if (item.getType() == Material.ELYTRA) {
            // Apply double damage
            event.setDamage(event.getDamage() * 2);
        }
    }

    @EventHandler
    public void onElytraBoost(PlayerElytraBoostEvent event) {
        if (!isActive()) return;
        
        if (random.nextDouble() <= 0.06) {
            event.setCancelled(true);
            
            Player player = event.getPlayer();
            
            // Consume the firework manually since we cancelled the event
            if (event.getFirework() != null) {
                event.getFirework().remove(); // Remove the spawned firework entity
            }
            
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (mainHand.getType() == Material.FIREWORK_ROCKET) {
                mainHand.setAmount(mainHand.getAmount() - 1);
            } else if (offHand.getType() == Material.FIREWORK_ROCKET) {
                offHand.setAmount(offHand.getAmount() - 1);
            }

            int failType = random.nextInt(3);
            SoundUtils.playInRadius(player.getLocation(), "entity.generic.explode", 1.0f, 0.5f);

            if (failType == 0) {
                // Leave Elytra with 1 durability
                ItemStack chestplate = player.getInventory().getChestplate();
                if (chestplate != null && chestplate.getType() == Material.ELYTRA) {
                    ItemMeta meta = chestplate.getItemMeta();
                    if (meta instanceof Damageable damageable) {
                        int maxDurability = chestplate.getType().getMaxDurability();
                        damageable.setDamage(maxDurability - 1);
                        chestplate.setItemMeta(damageable);
                        Messenger.prefixedSend(player, "&c¡Tu cohete explotó y destrozó tus élitros!");
                    }
                }
            } else if (failType == 1) {
                // Crash into the floor
                player.setVelocity(new Vector(0, -4.0, 0));
                Messenger.prefixedSend(player, "&c¡Tu cohete falló y te empuja en picada!");
            } else if (failType == 2) {
                // Move in opposite direction with greater speed
                Vector direction = player.getLocation().getDirection();
                Vector opposite = direction.multiply(-2.5); // Reverse and speed up
                player.setVelocity(opposite);
                Messenger.prefixedSend(player, "&c¡Tu cohete se atascó y te disparó hacia atrás!");
            }
        }
    }
}

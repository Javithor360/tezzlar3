package com.panita.tezzlar3.qol.listeners;

import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TotemCombatListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType().isAir()) return;

        if (CustomItemManager.isCustomItem(offHand, "bee_totem")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0, false, true, true));
        }
    }
}

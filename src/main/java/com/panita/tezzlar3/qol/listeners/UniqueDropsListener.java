package com.panita.tezzlar3.qol.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import com.panita.tezzlar3.timeline.util.TimeManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class UniqueDropsListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        // Only active during days 19, 20, and 22
        int currentDay = TimeManager.getCurrentDay();
        if (currentDay < 19 || currentDay > 22) return;
        
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        
        if (killer == null) return;

        // 1. Phantoms dropping stone_membrance (5% chance)
        if (entity instanceof Phantom) {
            if (Math.random() < 0.05) {
                ItemStack membrance = CustomItemManager.getItem("stone_membrance");
                if (membrance != null) {
                    event.getDrops().add(membrance);
                }
            }
        }
        
        // 2. Custom Mobs dropping stone_heart (5% chance)
        NamespacedKey mobKey = new NamespacedKey(Tezzlar.getInstance(), "custom_mob_id");
        if (entity.getPersistentDataContainer().has(mobKey, PersistentDataType.STRING)) {
            if (Math.random() < 0.1) {
                ItemStack heart = CustomItemManager.getItem("stone_heart");
                if (heart != null) {
                    event.getDrops().add(heart);
                }
            }
        }
    }
}

package com.panita.tezzlar3.qol.listeners;

import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CustomExpBottleListener implements Listener {

    private final Map<String, Integer> customXpAmounts = new HashMap<>();

    public CustomExpBottleListener() {
        customXpAmounts.put("experience_bottle_s", 50);
        customXpAmounts.put("experience_bottle_m", 200);
        customXpAmounts.put("experience_bottle_l", 500);
        customXpAmounts.put("experience_bottle_xl", 800);
        customXpAmounts.put("experience_bottle_2xl", 1200);
        customXpAmounts.put("experience_bottle_4xl", 2500);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExpBottleSplash(ExpBottleEvent event) {
        ThrownExpBottle bottle = event.getEntity();
        ItemStack item = bottle.getItem();
        
        if (!CustomItemManager.isCustomItem(item)) return;

        for (Map.Entry<String, Integer> entry : customXpAmounts.entrySet()) {
            if (CustomItemManager.isCustomItem(item, entry.getKey())) {
                event.setExperience(entry.getValue());
                break;
            }
        }
    }
}

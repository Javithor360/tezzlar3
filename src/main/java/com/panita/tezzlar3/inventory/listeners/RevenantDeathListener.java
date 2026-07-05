package com.panita.tezzlar3.inventory.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.inventory.util.InventorySerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class RevenantDeathListener implements Listener {

    private final NamespacedKey pdcKey;

    public RevenantDeathListener() {
        this.pdcKey = new NamespacedKey(Tezzlar.getInstance(), "revenant_inventory");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        
        if (entity.getPersistentDataContainer().has(pdcKey, PersistentDataType.STRING)) {
            String base64 = entity.getPersistentDataContainer().get(pdcKey, PersistentDataType.STRING);
            if (base64 != null && !base64.isEmpty()) {
                ItemStack[] items = InventorySerializer.fromBase64(base64);
                
                // Clear default drops
                event.getDrops().clear();
                
                for (ItemStack item : items) {
                    if (item != null) {
                        event.getDrops().add(item);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityTransform(EntityTransformEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(pdcKey, PersistentDataType.STRING)) {
            // Cancel the transformation so it doesn't lose its inventory PDC and become a drowned
            event.setCancelled(true);
        }
    }
}

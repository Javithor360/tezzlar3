package com.panita.tezzlar3.inventory.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.inventory.util.InventoryConfigDefaults;
import com.panita.tezzlar3.inventory.util.GravesDataManager;
import com.panita.tezzlar3.inventory.util.InventorySerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class GravesDeathListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!Tezzlar.getConfigManager().getBoolean("inventory.enabled", InventoryConfigDefaults.INVENTORY_ENABLED)) return;
        if (!Tezzlar.getConfigManager().getBoolean("inventory.revenantZombie", InventoryConfigDefaults.INVENTORY_REVENANTZOMBIE)) return;
        
        Player player = event.getEntity();
        
        // We capture the drops and save them, clearing the native drops
        if (!event.getDrops().isEmpty()) {
            ItemStack[] drops = event.getDrops().toArray(new ItemStack[0]);
            String base64 = InventorySerializer.toBase64(drops);
            
            event.getDrops().clear();
            
            Location deathLoc = player.getLocation().getBlock().getLocation();
            
            String deathCause = "Causa desconocida";
            if (player.getLastDamageCause() != null) {
                deathCause = player.getLastDamageCause().getCause().name();
            }
            
            GravesDataManager.addGrave(deathLoc, player.getUniqueId(), player.getName(), base64, deathCause);
        }
    }
}

package com.panita.tezzlar3.inventory.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.inventory.util.InventoryConfigDefaults;
import com.panita.tezzlar3.inventory.util.GravesDataManager;
import com.panita.tezzlar3.inventory.util.InventorySerializer;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.qol.util.CustomItemManager;

public class GravesDeathListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!Tezzlar.getConfigManager().getBoolean("inventory.enabled", InventoryConfigDefaults.INVENTORY_ENABLED)) return;
        if (!Tezzlar.getConfigManager().getBoolean("inventory.revenantZombie", InventoryConfigDefaults.INVENTORY_REVENANTZOMBIE)) return;
        
        Player player = event.getEntity();
        
        // Check for Soulbound Relic
        boolean isSoulbound = false;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && CustomItemManager.isCustomItem(item, "soulbound_relic")) {
                isSoulbound = true;
                // Consume one soulbound item
                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItem(i, item.getAmount() > 0 ? item : null);
                break;
            }
        }
        
        if (isSoulbound) {
            event.setKeepInventory(true);
            event.getDrops().clear();
            
            // Halve the XP level
            event.setKeepLevel(true);
            event.setDroppedExp(0);
            int currentLevel = player.getLevel();
            player.setLevel(currentLevel / 2);
            
            Messenger.prefixedSend(player, "<green>¡Tu Reliquia de Vinculación se ha consumido, pero ha protegido tus pertenencias!</green>");
        }
        
        // We capture the drops and save them (if soulbound, drops are empty, so we just save an empty string to mark the grave)
        if (!event.getDrops().isEmpty() || isSoulbound) {
            String base64 = "";
            if (!isSoulbound) {
                ItemStack[] drops = event.getDrops().toArray(new ItemStack[0]);
                base64 = InventorySerializer.toBase64(drops);
                event.getDrops().clear();
            }
            
            Location deathLoc = player.getLocation().getBlock().getLocation();
            
            String deathCause = "Causa desconocida";
            if (player.getLastDamageCause() != null) {
                deathCause = player.getLastDamageCause().getCause().name();
            }
            
            GravesDataManager.addGrave(deathLoc, player.getUniqueId(), player.getName(), base64, deathCause, isSoulbound);
        }
    }
}

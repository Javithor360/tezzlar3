package com.panita.tezzlar3.inventory.listeners;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.inventory.util.GravesDataManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.inventory.util.InventoryConfigDefaults;

public class RevenantJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if the player has an active grave
        ConfigurationSection grave = GravesDataManager.getGraveForPlayer(player.getUniqueId());
        if (grave != null) {
            int x = grave.getInt("x");
            int y = grave.getInt("y");
            int z = grave.getInt("z");
            String worldName = grave.getString("world", Global.WORLD_NAME);
            
            String dimension = "Overworld";
            if (worldName.endsWith("_nether")) {
                dimension = "Nether";
            } else if (worldName.endsWith("_the_end")) {
                dimension = "End";
            }
            
            String rawMessage = Tezzlar.getConfigManager().getString("inventory.deathCoordsReminder", InventoryConfigDefaults.DEATH_COORDS_REMINDER);
            
            String message = rawMessage
                    .replace("{x}", String.valueOf(x))
                    .replace("{y}", String.valueOf(y))
                    .replace("{z}", String.valueOf(z))
                    .replace("{dimension}", dimension);
            
            Messenger.prefixedSend(player, message);
        }
    }
}

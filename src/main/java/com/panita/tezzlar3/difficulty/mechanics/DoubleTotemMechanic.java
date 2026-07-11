package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.util.PlayerUtils;

public class DoubleTotemMechanic extends DifficultyMechanic {

    public DoubleTotemMechanic(JavaPlugin plugin) {
        super(plugin, 21);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent event) {
        if (!isActive()) return;
        
        if (!(event.getEntity() instanceof Player player)) return;
        
        // Count the total number of totems in the player's inventory
        PlayerInventory inv = player.getInventory();
        int totalTotems = 0;
        
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                totalTotems += item.getAmount();
            }
        }
        
        // If they don't have at least 2 totems, cancel the resurrection
        if (totalTotems < 2) {
            event.setCancelled(true);
            return;
        }
        
        // The game will automatically consume the totem the player holds in their hand.
        // We must find a second totem in the inventory and consume it.
        boolean removedExtra = false;
        
        // Check the inventory to remove an additional totem
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                
                // Avoid removing the one in the hand since vanilla will consume it
                // However, if they have a stack of totems in the hand slot, we can remove one from there
                if ((i == inv.getHeldItemSlot() || i == 40) && item.getAmount() == 1) {
                    continue; // It is the only totem in this hand, vanilla will consume it
                }
                
                item.setAmount(item.getAmount() - 1);
                removedExtra = true;
                break;
            }
        }
        
        // Just in case it couldn't be removed (should be impossible since we verified >= 2)
        if (!removedExtra) {
            event.setCancelled(true);
        }
    }
}

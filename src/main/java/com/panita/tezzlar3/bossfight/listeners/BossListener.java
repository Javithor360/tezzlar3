package com.panita.tezzlar3.bossfight.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.bossfight.gui.BossAttacksMenu;
import com.panita.tezzlar3.bossfight.gui.BossAttributesMenu;
import com.panita.tezzlar3.bossfight.gui.BossGeneralMenu;
import com.panita.tezzlar3.bossfight.util.BossItems;
import com.panita.tezzlar3.bossfight.util.BossManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BossListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBossDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        BossManager manager = BossManager.getInstance();
        if (!manager.isBoss(player)) return;
        
        // If fake death or phase 4, cancel damage
        if (manager.isFakeDeathState() || manager.getCurrentPhase() == 4) {
            event.setCancelled(true);
            return;
        }
        
        double finalDamage = event.getFinalDamage();
        if (player.getHealth() - finalDamage <= 0.0) {
            event.setCancelled(true);
            manager.triggerFakeDeath();
            return;
        }
        
        // Update BossBar after damage is applied (delay 1 tick to let health update, or update manually)
        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), manager::updateBossBar, 1L);
    }
    
    @EventHandler
    public void onBossRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        BossManager manager = BossManager.getInstance();
        if (manager.isBoss(player)) {
            Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), manager::updateBossBar, 1L);
        }
    }

    @EventHandler
    public void onBossDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        BossManager manager = BossManager.getInstance();
        
        if (manager.isBoss(player)) {
            // Remove boss items from drops
            event.getDrops().removeIf(BossItems::isBossItem);
        }
    }

    @EventHandler
    public void onBossInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        BossManager manager = BossManager.getInstance();
        
        if (!manager.isBoss(player)) return;
        
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && BossItems.isBossItem(item)) {
                event.setCancelled(true);
                String type = BossItems.getBossItemType(item);
                
                if (type == null) return;
                
                switch (type) {
                    case BossItems.GENERAL_MENU:
                        new BossGeneralMenu(player).open();
                        break;
                    case BossItems.ATTACKS_MENU:
                        new BossAttacksMenu(player).open();
                        break;
                    case BossItems.ATTRIBUTES_MENU:
                        new BossAttributesMenu(player).open();
                        break;
                }
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        BossManager manager = BossManager.getInstance();
        if (manager.isBoss(event.getPlayer()) && BossItems.isBossItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        BossManager manager = BossManager.getInstance();
        if (manager.isBoss(player)) {
            ItemStack clickedItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();
            
            if (BossItems.isBossItem(clickedItem) || BossItems.isBossItem(cursorItem)) {
                // Prevent moving boss items to other inventories or dropping them
                if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
                    event.setCancelled(true);
                }
                
                // Prevent dropping via hotkey (Q)
                if (event.getClick().name().contains("DROP")) {
                    event.setCancelled(true);
                }
            }
        }
    }
}

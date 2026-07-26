package com.panita.tezzlar3.bossfight.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.bossfight.gui.BossAttacksMenu;
import com.panita.tezzlar3.bossfight.gui.BossAttributesMenu;
import com.panita.tezzlar3.bossfight.gui.BossGeneralMenu;
import com.panita.tezzlar3.bossfight.util.BossItems;
import com.panita.tezzlar3.bossfight.util.BossManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BossListener implements Listener {
    
    private static final Set<UUID> bossMagmaTrailTargets = new HashSet<>();
    
    public static void addMagmaTrailTarget(UUID uuid) {
        bossMagmaTrailTargets.add(uuid);
        // Duration: 10 seconds (200 ticks)
        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
            bossMagmaTrailTargets.remove(uuid);
        }, 200L);
    }

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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BossManager manager = BossManager.getInstance();
        
        if (manager.getPendingBossUuid() != null && manager.getPendingBossUuid().equals(player.getUniqueId())) {
            manager.resumeFight(player);
        } else {
            if (manager.getGlobalBossBar() != null) {
                player.showBossBar(manager.getGlobalBossBar());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BossManager manager = BossManager.getInstance();
        
        if (manager.isBoss(player)) {
            manager.pauseFight();
            manager.saveState(Tezzlar.getConfigManager());
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player p = (Player) event.getTarget();
            if (BossManager.getInstance().isBoss(p)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!bossMagmaTrailTargets.contains(event.getPlayer().getUniqueId())) return;
        
        Location to = event.getTo();
        if (to == null) return;
        
        Block blockUnder = to.clone().subtract(0, 1, 0).getBlock();
        if (blockUnder.getType().isAir() || !blockUnder.getType().isSolid() || blockUnder.getType() == Material.BEDROCK || blockUnder.getType() == Material.MAGMA_BLOCK) return;
        if (blockUnder.getState() instanceof InventoryHolder) return;
        
        BlockData originalData = blockUnder.getBlockData().clone();
        blockUnder.setType(Material.MAGMA_BLOCK);
        
        // Revert after 5 seconds
        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
            blockUnder.setBlockData(originalData);
        }, 100L);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof LargeFireball fireball) {
            if (fireball.getShooter() instanceof Player p && BossManager.getInstance().isBoss(p)) {
                event.blockList().clear(); // Prevent block damage from boss fireballs
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball snowball) {
            if (snowball.getShooter() instanceof Player p && BossManager.getInstance().isBoss(p)) {
                if (event.getHitEntity() instanceof Player hit) {
                    hit.setVelocity(hit.getLocation().getDirection().multiply(-2.0).setY(1.5)); // Aggressive knockback
                    hit.damage(5.0, p);
                } else if (event.getHitBlock() != null) {
                    for (Entity e : event.getEntity().getNearbyEntities(2, 2, 2)) {
                        if (e instanceof Player hit && !hit.equals(p)) {
                            Vector push = hit.getLocation().toVector().subtract(snowball.getLocation().toVector()).normalize().multiply(1.5).setY(1.0);
                            hit.setVelocity(push);
                            hit.damage(5.0, p);
                        }
                    }
                }
            }
        }
    }
}

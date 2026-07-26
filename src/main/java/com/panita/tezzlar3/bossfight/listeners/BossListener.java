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
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
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
import org.bukkit.util.Vector;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.scheduler.BukkitRunnable;
import com.panita.tezzlar3.bossfight.util.BossAttacks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class BossListener implements Listener {
    
    private static final Set<UUID> bossMagmaTrailTargets = new HashSet<>();
    private static final Map<UUID, Long> bossSwordCooldown = new HashMap<>();
    
    public static void addMagmaTrailTarget(UUID uuid) {
        bossMagmaTrailTargets.add(uuid);
        // Duration: 10 seconds (200 ticks)
        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
            bossMagmaTrailTargets.remove(uuid);
        }, 200L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBossDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        BossManager manager = BossManager.getInstance();
        if (!manager.isBoss(player)) return;
        
        // If fake death, cancel damage
        if (manager.isFakeDeathState()) {
            event.setCancelled(true);
            return;
        }
        
        // Phase 4 Immunity Check
        if (manager.getCurrentPhase() == 4 && !manager.isVulnerable()) {
            event.setCancelled(true);
            return;
        }
        
        double finalDamage = event.getFinalDamage();
        if (player.getHealth() - finalDamage <= 0.0) {
            if (manager.getCurrentPhase() == 4) {
                // Check if damager is a player or player projectile
                boolean isPlayerDamage = false;
                if (event instanceof EntityDamageByEntityEvent entityEvent) {
                    Entity damager = entityEvent.getDamager();
                    if (damager instanceof Player) {
                        isPlayerDamage = true;
                    } else if (damager instanceof Projectile proj && proj.getShooter() instanceof Player) {
                        isPlayerDamage = true;
                    }
                }
                
                if (isPlayerDamage) {
                    // Do not cancel, let the boss die for real
                    return;
                } else {
                    // Environmental or non-player damage cannot kill the boss for real. Leave at 1 HP.
                    event.setCancelled(true);
                    player.setHealth(1.0);
                    return;
                }
            } else {
                event.setCancelled(true);
                manager.triggerFakeDeath();
                return;
            }
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
            
            if (manager.getCurrentPhase() == 4) {
                manager.stopFight();
                
                Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle("§6¡Felicidades!", "§eHan librado a Tezzlar de su maldición", 10, 100, 20);
                        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    }
                }, 100L); // 5 seconds delay to let the hardcore death message play first
            }
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
                
                // Clear passenger ItemDisplays
                for (Entity passenger : snowball.getPassengers()) {
                    passenger.remove();
                }
            }
        }
    }

    @EventHandler
    public void onBossShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player boss && BossManager.getInstance().isBoss(boss)) {
            List<Player> targets = BossAttacks.getTargets(boss);
            if (!targets.isEmpty()) {
                event.setCancelled(true); // Cancel original arrow to fire multiple
                
                int onlinePlayers = Bukkit.getOnlinePlayers().size();
                int arrowCount = 1 + new Random().nextInt(onlinePlayers);
                
                for (int i = 0; i < arrowCount; i++) {
                    Player target = targets.get(new Random().nextInt(targets.size()));
                    
                    // Spawn a new arrow
                    Entity projectile = boss.launchProjectile(Arrow.class);
                    // Add slight variance so they don't stack perfectly on spawn
                    projectile.setVelocity(boss.getLocation().getDirection().multiply(1.5).add(Vector.getRandom().subtract(new Vector(0.5,0.5,0.5)).multiply(0.5)));
                    
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!projectile.isValid() || !target.isOnline() || projectile.isOnGround()) {
                                this.cancel();
                                return;
                            }
                            Vector dir = target.getLocation().add(0, 1, 0).toVector().subtract(projectile.getLocation().toVector());
                            if (dir.lengthSquared() > 0) {
                                projectile.setVelocity(dir.normalize().multiply(1.5));
                            }
                        }
                    }.runTaskTimer(Tezzlar.getInstance(), 1L, 1L);
                }
            }
        }
    }

    @EventHandler
    public void onBossSwing(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        Player boss = event.getPlayer();
        if (!BossManager.getInstance().isBoss(boss)) return;
        
        if (boss.getInventory().getItemInMainHand().getType().name().endsWith("_SWORD")) {
            long now = System.currentTimeMillis();
            if (now - bossSwordCooldown.getOrDefault(boss.getUniqueId(), 0L) >= 500) { // 0.5 seconds cooldown
                bossSwordCooldown.put(boss.getUniqueId(), now);
                
                double damage = 10.0;
                AttributeInstance attr = boss.getAttribute(Attribute.ATTACK_DAMAGE);
                if (attr != null) damage = attr.getValue();
                
                Block targetBlock = boss.getTargetBlockExact(100);
                Location hitLoc = targetBlock != null ? targetBlock.getLocation().add(0.5, 1.0, 0.5) : boss.getLocation();
                
                boss.getWorld().spawnParticle(Particle.SWEEP_ATTACK, hitLoc, 20, 5, 0.1, 5, 0);
                boss.getWorld().playSound(hitLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 0.5f);
                
                for (LivingEntity ent : hitLoc.getWorld().getNearbyLivingEntities(hitLoc, 15, 5, 15)) {
                    if (ent.equals(boss)) continue;
                    
                    boolean shouldHit = false;
                    if (ent instanceof Player p) {
                        shouldHit = BossAttacks.isValidTarget(p, boss);
                    } else if (!(ent instanceof ArmorStand)) {
                        shouldHit = true;
                    }
                    
                    if (shouldHit) {
                        double distSq = Math.pow(ent.getLocation().getX() - hitLoc.getX(), 2) + Math.pow(ent.getLocation().getZ() - hitLoc.getZ(), 2);
                        
                        if (distSq <= 225.0) { // Radius 15 cylinder
                            ent.damage(damage, boss);
                            Vector knockback = ent.getLocation().toVector().subtract(hitLoc.toVector());
                            if (knockback.lengthSquared() > 0) {
                                ent.setVelocity(knockback.normalize().multiply(0.5).setY(0.3));
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        BossManager manager = BossManager.getInstance();
        if (manager.getCurrentPhase() == 4 && manager.getActiveStatues().contains(event.getEntity().getUniqueId())) {
            manager.getActiveStatues().remove(event.getEntity().getUniqueId());
            
            // Remove the netherite block underneath
            Location loc = event.getEntity().getLocation();
            for (int dy = 0; dy <= 2; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Block block = loc.clone().subtract(dx, dy, dz).getBlock();
                        if (block.getType() == Material.NETHERITE_BLOCK) {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
            
            // Clear drops just in case
            event.getDrops().clear();
            event.setDroppedExp(0);
            
            // Clear nearby ItemDisplays that represent the statue
            for (Entity e : event.getEntity().getNearbyEntities(0.5, 2.0, 0.5)) {
                if (e instanceof ItemDisplay) {
                    e.remove();
                }
            }
            
            if (manager.getActiveStatues().isEmpty()) {
                manager.triggerVulnerability();
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.NETHERITE_BLOCK) {
            if (BossManager.getInstance().getCurrentPhase() == 4) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (BossManager.getInstance().isBoss(player)) {
                event.setCancelled(true);
                player.setFoodLevel(20);
                player.setSaturation(0f);
            }
        }
    }
}

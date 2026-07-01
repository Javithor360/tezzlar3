package com.panita.tezzlar3.qol.listeners;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.panita.tezzlar3.Tezzlar;

public class QolItemsListener implements Listener {

    private final Map<UUID, Long> hornCooldowns = new HashMap<>();

    public QolItemsListener() {
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (CustomItemManager.isCustomItem(item, "copper_apple")) {
            int preFood = player.getFoodLevel();
            float preSat = player.getSaturation();
            
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 60, 0, false, true, true));
            
            // Revert food and saturation given by the vanilla apple behavior
            Bukkit.getScheduler().runTask(Tezzlar.getInstance(), () -> {
                player.setFoodLevel(preFood);
                player.setSaturation(preSat);
            });
        } else if (CustomItemManager.isCustomItem(item, "manzanium_apple")) {
            // Apply 20 food and 20 saturation 1 tick later
            Bukkit.getScheduler().runTask(Tezzlar.getInstance(), () -> {
                player.setFoodLevel(20);
                player.setSaturation(20f);
            });
        } else if (CustomItemManager.isCustomItem(item, "copper_carrot")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60, 0, false, true, true));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHornInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.GOAT_HORN) return;

        if (CustomItemManager.isCustomItem(item, "amethyst_horn")) {
            Player player = event.getPlayer();

            long now = System.currentTimeMillis();
            if (hornCooldowns.containsKey(player.getUniqueId())) {
                if (now - hornCooldowns.get(player.getUniqueId()) < 15000) {
                    return; // Prevent execution spam
                }
            }
            hornCooldowns.put(player.getUniqueId(), now);

            // Set vanilla visual cooldown to 15 seconds (300 ticks)
            player.setCooldown(Material.GOAT_HORN, 300);

            int count = 0;
            for (Entity entity : player.getNearbyEntities(30, 30, 30)) {
                if (entity instanceof Mob mob) {
                    double distance = player.getLocation().distance(mob.getLocation());

                    // Push entities within 5 blocks and spawn sonic boom
                    if (distance <= 5.0) {
                        Vector push = mob.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.5).setY(0.5);
                        mob.setVelocity(push);
                        mob.getWorld().spawnParticle(Particle.SONIC_BOOM, mob.getLocation().add(0, 1, 0), 1);
                        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);
                    }

                    mob.setAware(false);
                    EntityUtils.setColoredGlowing(mob, NamedTextColor.DARK_BLUE);

                    new BukkitRunnable() {
                        int passedTicks = 0;

                        @Override
                        public void run() {
                            if (!mob.isValid() || mob.isDead() || passedTicks >= 300) {
                                if (mob.isValid() && !mob.isDead()) {
                                    mob.setAware(true);
                                    EntityUtils.removeColoredGlowing(mob);
                                }
                                this.cancel();
                                return;
                            }
                            mob.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, mob.getLocation().add(0, 1, 0), 10, 0.4, 0.4, 0.4, 0.0);
                            passedTicks += 10;
                        }
                    }.runTaskTimer(Tezzlar.getInstance(), 0L, 10L); // 15 seconds duration (runs every 0.5s)

                    count++;
                }
            }

            if (count > 0) {
                Messenger.prefixedSend(player, "<gold>¡Has paralizado a <white>" + count + " <gold>mobs por 15 segundos!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInfiniteBagClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        
        // Case 1: Click with an item (cursor) on the bundle (current)
        if (current != null && CustomItemManager.isCustomItem(current, "infinite_bag")) {
            if (cursor != null && !cursor.getType().isAir()) {
                event.setCancelled(true);
                event.getView().setCursor(null); // Delete item from cursor
                
                SoundUtils.play(player, "entity.item.pickup", 0.5f, 0.5f);
                Bukkit.getScheduler().runTask(Tezzlar.getInstance(), player::updateInventory);
            }
        }
        // Case 2: Pass the bundle (cursor) over an item (current)
        else if (cursor != null && CustomItemManager.isCustomItem(cursor, "infinite_bag")) {
            if (current != null && !current.getType().isAir()) {
                ItemStack bag = cursor.clone(); // Save a copy of the bundle
                
                // Clear any items that might have been visually stored in the bundle
                if (bag.getItemMeta() instanceof BundleMeta meta) {
                    meta.setItems(Collections.emptyList());
                    bag.setItemMeta(meta);
                }
                
                event.setCancelled(true);
                
                // Delete item from slot directly
                if (event.getClickedInventory() != null) {
                    event.getClickedInventory().setItem(event.getSlot(), null);
                }
                
                // Force the bundle back to the cursor
                event.getView().setCursor(bag);
                
                SoundUtils.play(player, "entity.item.pickup", 0.5f, 0.5f);
                Bukkit.getScheduler().runTask(Tezzlar.getInstance(), player::updateInventory);
            }
        }
    }
}

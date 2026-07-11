package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GoatHornParalyzeMechanic extends DifficultyMechanic {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public GoatHornParalyzeMechanic(JavaPlugin plugin) {
        super(plugin, 4);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHornUse(PlayerInteractEvent event) {
        if (!isActive()) return;
        
        if (event.getAction().isRightClick() && event.getItem() != null && event.getItem().getType() == Material.GOAT_HORN) {
            if (CustomItemManager.isCustomItem(event.getItem(), "amethyst_horn")) return;
            
            Player player = event.getPlayer();
            
            long now = System.currentTimeMillis();
            if (cooldowns.containsKey(player.getUniqueId())) {
                if (now - cooldowns.get(player.getUniqueId()) < 15000) {
                    return; // Prevent execution spam before custom 15s cooldown applies
                }
            }
            cooldowns.put(player.getUniqueId(), now);
            
            // Set vanilla visual cooldown to 15 seconds (300 ticks)
            player.setCooldown(Material.GOAT_HORN, 300);
            
            player.setFoodLevel(Math.max(0, player.getFoodLevel() - 4));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0));

            int count = 0;
            for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
                if (entity instanceof Mob mob) {
                    if (mob.getPersistentDataContainer().has(new NamespacedKey(plugin, "giga_magma_cube"), PersistentDataType.BYTE)) continue;
                    
                    // setAware(false) disables pathfinding and targeting AI but preserves gravity
                    mob.setAware(false);
                    EntityUtils.setColoredGlowing(mob, NamedTextColor.DARK_BLUE);
                    
                    new BukkitRunnable() {
                        int passedTicks = 0;
                        @Override
                        public void run() {
                            if (!mob.isValid() || mob.isDead() || passedTicks >= 100) {
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
                    }.runTaskTimer(plugin, 0L, 10L); // 5 seconds duration (runs every 0.5s)
                    
                    count++;
                }
            }
            
            if (count > 0) {
                Messenger.prefixedSend(player, "<gold>¡Has paralizado a <white>" + count + " <gold>mobs por 5 segundos!");
            }
        }
    }
}

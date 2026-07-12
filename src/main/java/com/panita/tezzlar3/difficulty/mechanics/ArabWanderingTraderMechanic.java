package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ArabWanderingTraderMechanic extends DifficultyMechanic {
    private final NamespacedKey traderKey;
    private final NamespacedKey spawnTimeKey;
    private final NamespacedKey defusedKey;

    public ArabWanderingTraderMechanic(JavaPlugin plugin) {
        super(plugin, 29);
        this.traderKey = new NamespacedKey(plugin, "arab_trader");
        this.spawnTimeKey = new NamespacedKey(plugin, "arab_spawn_time");
        this.defusedKey = new NamespacedKey(plugin, "arab_defused");
        
        CustomMobManager.register(CustomMobType.ARAB_WANDERING_TRADER, this::spawnManual);
        startGlobalTimer();
    }

    public void spawnManual(Location loc) {
        WanderingTrader trader = (WanderingTrader) EntityUtils.spawnNatural(loc, EntityType.WANDERING_TRADER);
        if (trader != null) {
            transform(trader);
        }
    }

    private void transform(WanderingTrader trader) {
        trader.getPersistentDataContainer().set(traderKey, PersistentDataType.BYTE, (byte) 1);
        trader.getPersistentDataContainer().set(spawnTimeKey, PersistentDataType.LONG, System.currentTimeMillis());
        EntityUtils.setCustomName(trader, CustomMobType.ARAB_WANDERING_TRADER.getCustomName());
        
        // Prevent natural despawn
        trader.setRemoveWhenFarAway(false);
        trader.setDespawnDelay(-1); // For wandering traders specifically
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        if (event.getEntityType() == EntityType.WANDERING_TRADER) {
            if (EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) {
                transform((WanderingTrader) event.getEntity());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        if (event.getEntity() instanceof WanderingTrader trader) {
            if (trader.getPersistentDataContainer().has(traderKey, PersistentDataType.BYTE)) {
                // If it's void damage or kill command, let it die
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID || event.getCause() == EntityDamageEvent.DamageCause.KILL) {
                    return;
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTrade(PlayerTradeEvent event) {
        if (!isActive()) return;
        if (event.getVillager() instanceof WanderingTrader trader) {
            if (trader.getPersistentDataContainer().has(traderKey, PersistentDataType.BYTE)) {
                // Defused!
                if (!trader.getPersistentDataContainer().has(defusedKey, PersistentDataType.BYTE)) {
                    trader.getPersistentDataContainer().set(defusedKey, PersistentDataType.BYTE, (byte) 1);
                    
                    Messenger.prefixedSend(event.getPlayer(), "&a¡Has desactivado la bomba del Wandering Trader comprándole un ítem!");
                    SoundUtils.playInRadius(trader.getLocation(), "entity.experience_orb.pickup", 1.0f, 1.0f);
                    
                    // Kill peacefully
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        trader.setHealth(0); // Will drop natural drops
                    });
                }
            }
        }
    }

    private void startGlobalTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) return;
                
                long now = System.currentTimeMillis();
                for (World world : Bukkit.getWorlds()) {
                    for (WanderingTrader trader : world.getEntitiesByClass(WanderingTrader.class)) {
                        if (trader.isDead() || !trader.isValid()) continue;
                        
                        if (trader.getPersistentDataContainer().has(traderKey, PersistentDataType.BYTE)) {
                            if (trader.getPersistentDataContainer().has(defusedKey, PersistentDataType.BYTE)) {
                                continue;
                            }
                            
                            Long spawnTime = trader.getPersistentDataContainer().get(spawnTimeKey, PersistentDataType.LONG);
                            if (spawnTime != null) {
                                // 10 minutes = 600,000 ms
                                if (now - spawnTime >= 600_000L) {
                                    // Explode!
                                    trader.getWorld().createExplosion(trader.getLocation(), 10.0f, false, true);
                                    trader.remove();
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }
}

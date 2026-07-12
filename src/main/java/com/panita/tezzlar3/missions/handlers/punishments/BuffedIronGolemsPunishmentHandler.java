package com.panita.tezzlar3.missions.handlers.punishments;

import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.missions.handlers.PunishmentHandler;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class BuffedIronGolemsPunishmentHandler implements PunishmentHandler, Listener {

    private final NamespacedKey BUFFED_KEY;

    public BuffedIronGolemsPunishmentHandler(JavaPlugin plugin) {
        this.BUFFED_KEY = new NamespacedKey(plugin, "golem_buffed");

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            java.util.List<Player> punishedPlayers = new java.util.ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
                if (data != null && data.hasPunishment(getId())) {
                    punishedPlayers.add(player);
                }
            }
            
            if (punishedPlayers.isEmpty()) return;
            
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                for (IronGolem golem : world.getEntitiesByClass(IronGolem.class)) {
                    if (golem.getPersistentDataContainer().has(BUFFED_KEY, PersistentDataType.BYTE)) continue;
                    
                    for (Player player : punishedPlayers) {
                        if (player.getWorld().equals(golem.getWorld()) && player.getLocation().distanceSquared(golem.getLocation()) < 2304.0) { // 48 * 48
                            AttributeInstance maxHealth = golem.getAttribute(Attribute.MAX_HEALTH);
                            if (maxHealth != null) {
                                maxHealth.setBaseValue(maxHealth.getBaseValue() * 3);
                                golem.setHealth(maxHealth.getBaseValue());
                            }
                            golem.getPersistentDataContainer().set(BUFFED_KEY, PersistentDataType.BYTE, (byte) 1);
                            break;
                        }
                    }
                }
            }
        }, 100L, 100L); // Check every 5 seconds
    }

    @Override
    public String getId() {
        return "BUFFED_IRON_GOLEMS";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        // Passive effect
    }

    @EventHandler
    public void onGolemDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof IronGolem) {
            if (event.getDamager() instanceof Player player) {
                PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
                if (data != null && data.hasPunishment(getId())) {
                    event.setCancelled(true);
                }
            }
        }
    }
}

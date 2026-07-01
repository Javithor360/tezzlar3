package com.panita.tezzlar3.missions.handlers.punishments;

import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.missions.handlers.PunishmentHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class WardenNightmarePunishmentHandler implements PunishmentHandler, Listener {

    private final Random random = new Random();

    public WardenNightmarePunishmentHandler(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
                if (data != null && data.hasPunishment(getId())) {
                    World world = player.getWorld();
                    if (world.getEnvironment() == World.Environment.NORMAL) {
                        long time = world.getTime();
                        if (time >= 13000 && time <= 23000) {
                            // Apply darkness during the night
                            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, false, false, true));
                        }
                    }
                }
            }
        }, 20L, 20L); // Check every second
    }

    @Override
    public String getId() {
        return "WARDEN_NIGHTMARE";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        // Passive effect
    }

    @EventHandler
    public void onMonsterSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;
        
        if (event.getEntity() instanceof Monster && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            Location loc = event.getLocation();
            
            // Check if surface
            if (loc.getWorld().getEnvironment() == World.Environment.NORMAL) {
                int highestY = loc.getWorld().getHighestBlockYAt(loc);
                if (loc.getY() >= highestY) {
                    
                    boolean hasPunishedPlayerNearby = false;
                    for (Player player : loc.getWorld().getPlayers()) {
                        if (player.getLocation().distanceSquared(loc) <= 128 * 128) {
                            PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
                            if (data != null && data.hasPunishment(getId())) {
                                hasPunishedPlayerNearby = true;
                                break;
                            }
                        }
                    }
                    
                    if (hasPunishedPlayerNearby) {
                        // 0.002% chance = 0.00002
                        if (random.nextDouble() < 0.00002) {
                            event.setCancelled(true);
                            loc.getWorld().spawn(loc, Warden.class);
                        }
                    }
                }
            }
        }
    }
}

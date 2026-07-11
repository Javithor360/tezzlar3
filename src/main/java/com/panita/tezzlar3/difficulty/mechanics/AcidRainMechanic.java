package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class AcidRainMechanic extends DifficultyMechanic {
    private static AcidRainMechanic instance;
    private static org.bukkit.scheduler.BukkitTask currentTask;
    
    private final Random random = new Random();
    private boolean isAcidRain = false;
    private boolean isForcedAcidRain = false;
    private int acidRainTicks = 0;

    public AcidRainMechanic(JavaPlugin plugin) {
        super(plugin, 8); // Day 8
        
        if (instance != null) {
            HandlerList.unregisterAll(instance);
        }
        
        instance = this;
        
        if (currentTask != null) {
            currentTask.cancel();
        }
        
        // Task runs every 1 second (20 ticks)
        currentTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive() && !isForcedAcidRain) return;
            
            if (!isAcidRain) return;
            
            acidRainTicks++;
            
            for (World world : Bukkit.getWorlds()) {
                if (world.getEnvironment() != World.Environment.NORMAL) continue;
                if (!world.hasStorm()) continue;
                
                for (Player player : world.getPlayers()) {
                    if (player.isDead() || !player.isValid()) continue;
                    if (!PlayerUtils.isSurvival(player)) continue;
                    
                    if (player.isInRain()) {
                        boolean hasUmbrella = hasFancyUmbrella(player);
                        int requiredTicks = hasUmbrella ? 6 : 3; // 8s with umbrella, 3s without
                        
                        if (acidRainTicks % requiredTicks == 0) {
                            double newHealth = Math.max(0, player.getHealth() - 1.0);
                            player.setHealth(newHealth);
                            player.playHurtAnimation(0);
                            SoundUtils.playInRadius(player.getLocation(), "entity.player.hurt", 10.0f, 0.7F);
                        }
                    }
                }
            }
        }, 20L, 20L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) return;
        
        if (isForcedAcidRain) {
            if (!event.toWeatherState()) {
                event.setCancelled(true); // Don't let it stop raining
            }
            return;
        }

        if (!isActive()) return;

        if (event.toWeatherState()) {
            // Check if DeathTrain is active, if so, prevent acid rain
            if (DeathTrainMechanic.getInstance() != null && DeathTrainMechanic.getInstance().isActive() && DeathTrainMechanic.getInstance().getRemainingSeconds() > 0) {
                isAcidRain = false;
                return;
            }
            
            isAcidRain = random.nextInt(100) < 40;
            if (isAcidRain) {
                Messenger.prefixedBroadcast("<#64B5F6>Se informa a todos los supervivientes que una <#81C784>lluvia ácida</#81C784> está a punto de comenzar, se recomienda buscar refugio.</#64B5F6>");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    SoundUtils.play(p, "block.note_block.pling", 1, 2.0f);
                }
            }
        } else {
            isAcidRain = false;
        }
    }

    public static AcidRainMechanic getInstance() {
        return instance;
    }

    public void forceAcidRain() {
        isForcedAcidRain = true;
        isAcidRain = true;
        World world = Bukkit.getWorld(Global.WORLD_NAME);
        if (world != null) {
            world.setStorm(true);
            world.setWeatherDuration(144000); // Set high duration
            world.setThundering(false);
        }
        Messenger.prefixedBroadcast("<#64B5F6>Se informa a todos los supervivientes que una <#81C784>lluvia ácida prolongada</#81C784> ha comenzado, busquen refugio inmediatamente.</#64B5F6>");
        for (Player p : Bukkit.getOnlinePlayers()) {
            SoundUtils.play(p, "block.note_block.pling", 1, 2.0f);
        }
    }

    public void stopForcedAcidRain() {
        isForcedAcidRain = false;
        isAcidRain = false;
        World world = Bukkit.getWorld(Global.WORLD_NAME);
        if (world != null) {
            world.setStorm(false);
            world.setWeatherDuration(0);
        }
    }
    
    private boolean hasFancyUmbrella(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        ItemStack helmet = player.getInventory().getHelmet();
        return CustomItemManager.isCustomItem(main, "fancy_umbrella") 
                || CustomItemManager.isCustomItem(off, "fancy_umbrella")
                || CustomItemManager.isCustomItem(helmet, "fancy_umbrella");
    }
}

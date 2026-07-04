package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.config.Config;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.minievents.MiniEventsModule;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.timeline.util.TimeManager;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarManager;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class DeathTrainMechanic extends DifficultyMechanic implements Listener, ActionBarProvider {

    private static DeathTrainMechanic instance;
    private static org.bukkit.scheduler.BukkitTask currentTask;
    
    private int remainingSeconds = 0;
    private boolean stormActive = false;
    private final List<String> deadPlayers = new ArrayList<>();
    private final Map<String, ItemStack> headCache = new HashMap<>();
    private final Random random = new Random();

    public DeathTrainMechanic(JavaPlugin plugin) {
        super(plugin, 2); // Active from day 2
        
        if (instance != null) {
            HandlerList.unregisterAll(instance);
        }
        
        instance = this;
        
        this.remainingSeconds = Config.raw().getInt("difficulty.death_train_seconds", 0);
        List<String> savedPlayers = Config.raw().getStringList("difficulty.death_train_dead_players");
        if (savedPlayers != null) {
            this.deadPlayers.addAll(savedPlayers);
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        if (ActionBarManager.getInstance() != null) {
            ActionBarManager.getInstance().registerProvider(this);
        }

        if (currentTask != null) {
            currentTask.cancel();
        }

        currentTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            // Prevent overlap with other mechanics by pausing the DeathTrain
            if (MissionsModule.getRefugeManager() != null && MissionsModule.getRefugeManager().isActive()) return;
            if (MiniEventsModule.getManager() != null && MiniEventsModule.getManager().getActiveEvent() != null) return;
            
            if (remainingSeconds > 0) {
                remainingSeconds--;
                
                World overworld = Bukkit.getWorld(plugin.getConfig().getString("worldName", "world"));
                if (overworld != null) {
                    if (!overworld.hasStorm() || overworld.getWeatherDuration() < 100) {
                        overworld.setStorm(true);
                        overworld.setWeatherDuration(200);
                    }
                    stormActive = true;
                }
                
                if (remainingSeconds % 60 == 0 || remainingSeconds == 0) {
                    Tezzlar.getConfigManager().updateInt("difficulty.death_train_seconds", remainingSeconds, null);
                }

                for (Player player : Bukkit.getOnlinePlayers()) {

                    if (!PlayerUtils.isSurvival(player)) continue;
                    
                    // Slowness if exposed to rain
                    if (player.isInRain()) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0, false, false, true));
                    }
                }
            } else {
                if (stormActive) {
                    World overworld = Bukkit.getWorld(plugin.getConfig().getString("worldName", "world"));
                    if (overworld != null) {
                        overworld.setStorm(false);
                    }
                    stormActive = false;
                }
                if (!deadPlayers.isEmpty()) {
                    deadPlayers.clear();
                    Tezzlar.getConfigManager().updateStringList("difficulty.death_train_dead_players", deadPlayers, null);
                }
            }
        }, 20L, 20L);
    }

    public static DeathTrainMechanic getInstance() {
        return instance;
    }
    
    @Override
    public String getId() {
        return "deathtrain";
    }

    @Override
    public String getText(Player player) {
        if (!isActive() || remainingSeconds <= 0) return null;
        
        // Prevent overlap with other mechanics by pausing the DeathTrain
        if (MissionsModule.getRefugeManager() != null && MissionsModule.getRefugeManager().isActive()) return null;
        if (MiniEventsModule.getManager() != null && MiniEventsModule.getManager().getActiveEvent() != null) return null;
        
        String timeStr = Global.formatTimeTicks(remainingSeconds * 20L);
        return "<gray>DeathTrain activo por " + timeStr + "</gray>";
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = Math.min(remainingSeconds, 115200); // Max 32 hours
        this.remainingSeconds = Math.max(this.remainingSeconds, 0); // No negative
        Tezzlar.getConfigManager().updateInt("difficulty.death_train_seconds", this.remainingSeconds, null);
    }

    public void addDeathTrainTime(Player deceased) {
        if (!isActive()) return;
        
        int day = TimeManager.getCurrentDay();
        int secondsToAdd = day * 3600;
        
        this.remainingSeconds += secondsToAdd;
        this.remainingSeconds = Math.min(this.remainingSeconds, 115200); // Max 32 hours
        
        Tezzlar.getConfigManager().updateInt("difficulty.death_train_seconds", remainingSeconds, null);

        if (deceased != null) {
            if (!deadPlayers.contains(deceased.getName())) {
                deadPlayers.add(deceased.getName());
                Tezzlar.getConfigManager().updateStringList("difficulty.death_train_dead_players", deadPlayers, null);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMonsterSpawn(CreatureSpawnEvent event) {
        if (!isActive() || remainingSeconds <= 0) return;
        
        // Prevent overlap
        if (MissionsModule.getRefugeManager() != null && MissionsModule.getRefugeManager().isActive()) return;
        if (MiniEventsModule.getManager() != null && MiniEventsModule.getManager().getActiveEvent() != null) return;
        
        if (event.getEntity() instanceof Monster monster) {
            // Apply Strength II
            monster.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));
            
            // Apply annoying special effect
            EntityUtils.applyAnnoyingSpecialEffect(monster);

            // Equip dead player heads to zombies and skeletons
            if (!deadPlayers.isEmpty()) {
                if (monster instanceof Zombie || monster instanceof AbstractSkeleton) {
                    String playerName = deadPlayers.get(random.nextInt(deadPlayers.size()));
                    
                    ItemStack head = headCache.get(playerName);
                    if (head == null) {
                        head = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta meta = (SkullMeta) head.getItemMeta();
                        if (meta != null) {
                            OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
                            meta.setOwningPlayer(op);
                            head.setItemMeta(meta);
                        }
                        headCache.put(playerName, head.clone());
                    } else {
                        head = head.clone();
                    }
                    
                    if (monster.getEquipment() != null) {
                        monster.getEquipment().setHelmet(head);
                        monster.getEquipment().setHelmetDropChance(0.0f);
                    }
                }
            }
        }
    }
}

package com.panita.tezzlar3.minievents;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.config.CustomConfig;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.timeline.util.TimeManager;
import com.panita.tezzlar3.Tezzlar;
import org.bukkit.Bukkit;
import com.panita.tezzlar3.difficulty.mechanics.BabyKillCurseMechanic;
import com.panita.tezzlar3.difficulty.mechanics.OverworldToxicityMechanic;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.panita.tezzlar3.core.util.Global;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MiniEventManager {

    private final JavaPlugin plugin;
    private final List<MiniEvent> registeredEvents = new ArrayList<>();
    private final CustomConfig dataConfig;
    
    private MiniEvent activeEvent = null;
    private long activeEventRemainingTicks = 0;
    
    private long nextRouletteTicks = 72000L; // 60 minutes
    private boolean isRouletteSpinning = false;
    
    private final Random random = new Random();
    private int taskTaskId = -1;

    public MiniEventManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataConfig = new CustomConfig(plugin, "", "mini-events-data.yml");
        
        startTask();
    }
    
    public void init() {
        loadData();
    }

    public void registerEvent(MiniEvent event) {
        registeredEvents.add(event);
    }
    
    public List<MiniEvent> getRegisteredEvents() {
        return new ArrayList<>(registeredEvents);
    }
    
    public MiniEvent getEventById(String id) {
        for (MiniEvent event : registeredEvents) {
            if (event.getId().equalsIgnoreCase(id)) {
                return event;
            }
        }
        return null;
    }
    
    public void unregisterAll() {
        if (activeEvent != null) {
            activeEvent.stop(plugin);
        }
        registeredEvents.clear();
        stopTask();
        saveData();
    }

    private void startTask() {
        taskTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Handle active event timer
            if (activeEvent != null) {
                if (activeEventRemainingTicks > 0) {
                    activeEventRemainingTicks -= 20; // 1 second passed
                    
                    String timeStr = Global.formatTimeTicks(activeEventRemainingTicks);
                    String cleanName = MiniMessage.miniMessage().stripTags(activeEvent.getDisplayName());
                    String actionBarMsg = "<gray>" + cleanName + " (" + timeStr + ")</gray>";
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (MissionsModule.getRefugeManager() != null && MissionsModule.getRefugeManager().isActive()) continue;
                        if (OverworldToxicityMechanic.isToxic(player)) continue;
                        if (BabyKillCurseMechanic.isCursed(player)) continue;
                        Messenger.sendActionBar(player, actionBarMsg);
                    }
                    
                    if (activeEventRemainingTicks <= 0) {
                        stopActiveEvent();
                    } else if (activeEventRemainingTicks % 1200 == 0) { // every 60 seconds
                        saveData();
                    }
                }
            } else if (!isRouletteSpinning) {
                // Handle roulette timer
                nextRouletteTicks -= 20;
                if (nextRouletteTicks <= 0) {
                    nextRouletteTicks = 72000L; // Reset to 1 hour
                    saveData(); // Save reset state
                    
                    if (TimeManager.getCurrentDay() >= 19) {
                        int probability = Tezzlar.getConfigManager().getInt("mini-events.probability", 10);
                        if (random.nextInt(100) < probability) {
                            startRoulette(null);
                        }
                    }
                }
            }
        }, 20L, 20L).getTaskId();
    }
    
    private void stopTask() {
        if (taskTaskId != -1) {
            Bukkit.getScheduler().cancelTask(taskTaskId);
            taskTaskId = -1;
        }
    }

    private void startRoulette(MiniEvent forcedTarget) {
        isRouletteSpinning = true;
        
        List<MiniEvent> pool = new ArrayList<>();
        for (MiniEvent e : registeredEvents) {
            if (e.canExecute()) {
                pool.add(e);
            }
        }
        
        if (pool.isEmpty()) {
            if (forcedTarget == null) {
                isRouletteSpinning = false;
                return;
            } else {
                pool = new ArrayList<>(registeredEvents); // Fallback for visual roulette
            }
        }
        
        final MiniEvent targetEvent = (forcedTarget != null) ? forcedTarget : pool.get(random.nextInt(pool.size()));
        
        // Spinning delays in ticks (faster, more constant, slight slowdown at end)
        int[] delays = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 5, 6, 8, 10, 14, 20};
        
        playRouletteTick(0, delays, pool, targetEvent);
    }
    
    private void playRouletteTick(int index, int[] delays, List<MiniEvent> pool, MiniEvent targetEvent) {
        if (index >= delays.length) {
            // End of roulette
            isRouletteSpinning = false;
            triggerEvent(targetEvent);
            return;
        }
        
        String nameToShow;
        MiniEvent current = (index == delays.length - 1) ? targetEvent : pool.get(random.nextInt(pool.size()));
        
        if (index == delays.length - 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            }
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
            }
        }
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            Messenger.showTitle(p, "<obf><dark_gray>;;</obf> <b><gradient:#005155:#448367>¡Momento</gradient><gradient:#448367:#00566B> Pan</gradient><gradient:#00566B:#00566B>a!</gradient></b> <obf><dark_gray>;;", "<#FF69E2>→</#FF69E2> " + current.getDisplayName(), Duration.ZERO, Duration.ofMillis(delays[index] * 50L + 100L), Duration.ofMillis(100));
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            playRouletteTick(index + 1, delays, pool, targetEvent);
        }, delays[index]);
    }
    
    public void forceRoulette(MiniEvent forcedTarget) {
        if (!isRouletteSpinning && activeEvent == null) {
            nextRouletteTicks = 72000L;
            saveData();
            startRoulette(forcedTarget);
        }
    }

    private void triggerEvent(MiniEvent event) {
        activeEvent = event;
        activeEventRemainingTicks = event.getDurationTicks();
        event.start(plugin);
        
        if (activeEventRemainingTicks == 0) {
            // Instant event
            stopActiveEvent();
        } else {
            saveData();
            String startMsg = Tezzlar.getConfigManager().getString("mini-events.messages.event_start", "<yellow><b>¡EL MINI EVENTO HA COMENZADO!</b></yellow><newline>%event_name%<newline>");
            startMsg = startMsg.replace("%event_name%", event.getDisplayName());
            Messenger.broadcast(startMsg);
            Messenger.broadcast(event.getDescription());
        }
    }
    
    public void stopActiveEvent() {
        if (activeEvent != null) {
            String endMsg = Tezzlar.getConfigManager().getString("mini-events.messages.event_end", "<red><b>¡EL MINI EVENTO HA TERMINADO!</b></red><newline>%event_name%<newline>");
            endMsg = endMsg.replace("%event_name%", activeEvent.getDisplayName());
            Messenger.prefixedBroadcast(endMsg);
            SoundUtils.playGlobal("block.amethyst_cluster.break", 10, 0.0f);
            
            activeEvent.stop(plugin);
            activeEvent = null;
            activeEventRemainingTicks = 0;
            
            saveData();
        }
    }

    private void loadData() {
        dataConfig.reload();
        nextRouletteTicks = dataConfig.getConfig().getLong("next-roulette-ticks", 72000L);
        
        String activeId = dataConfig.getConfig().getString("active-event-id", null);
        if (activeId != null) {
            long remaining = dataConfig.getConfig().getLong("active-event-remaining", 0);
            if (remaining > 0) {
                // Determine if there's extra data to load
                java.util.Map<String, Object> extraData = null;
                if (dataConfig.getConfig().getConfigurationSection("active-event-extra") != null) {
                    extraData = dataConfig.getConfig().getConfigurationSection("active-event-extra").getValues(false);
                }
                
                final java.util.Map<String, Object> finalExtra = extraData;
                
                // Give it a slight delay to ensure everything is loaded before starting
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (MiniEvent event : registeredEvents) {
                        if (event.getId().equals(activeId)) {
                            activeEvent = event;
                            activeEventRemainingTicks = remaining;
                            if (finalExtra != null) {
                                event.loadExtraData(finalExtra);
                            }
                            event.start(plugin);
                            break;
                        }
                    }
                }, 20L);
            }
        }
    }

    public void saveData() {
        dataConfig.getConfig().set("next-roulette-ticks", nextRouletteTicks);
        if (activeEvent != null) {
            dataConfig.getConfig().set("active-event-id", activeEvent.getId());
            dataConfig.getConfig().set("active-event-remaining", activeEventRemainingTicks);
            java.util.Map<String, Object> extra = activeEvent.serializeExtraData();
            if (extra != null) {
                dataConfig.getConfig().createSection("active-event-extra", extra);
            } else {
                dataConfig.getConfig().set("active-event-extra", null);
            }
        } else {
            dataConfig.getConfig().set("active-event-id", null);
            dataConfig.getConfig().set("active-event-remaining", null);
            dataConfig.getConfig().set("active-event-extra", null);
        }
        dataConfig.save();
    }
    
    public MiniEvent getActiveEvent() {
        return activeEvent;
    }
}

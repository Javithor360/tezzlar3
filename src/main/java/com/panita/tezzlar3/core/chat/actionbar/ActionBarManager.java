package com.panita.tezzlar3.core.chat.actionbar;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActionBarManager {

    private static ActionBarManager instance;
    private final Map<String, ActionBarProvider> providers = new ConcurrentHashMap<>();
    private BukkitTask task;
    private long tickCounter = 0;

    public ActionBarManager() {
        instance = this;
    }

    public static ActionBarManager getInstance() {
        return instance;
    }

    public void registerProvider(ActionBarProvider provider) {
        providers.put(provider.getId(), provider);
    }

    public void unregisterProvider(String id) {
        providers.remove(id);
    }

    public void start() {
        if (task != null) {
            task.cancel();
        }

        task = Bukkit.getScheduler().runTaskTimer(Tezzlar.getInstance(), () -> {
            tickCounter++;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                List<String> urgentMessages = new ArrayList<>();
                List<String> backgroundMessages = new ArrayList<>();

                for (ActionBarProvider provider : providers.values()) {
                    try {
                        List<String> texts = provider.getTexts(player);
                        if (texts != null && !texts.isEmpty()) {
                            if (provider.isUrgent(player)) {
                                urgentMessages.addAll(texts);
                            } else {
                                backgroundMessages.addAll(texts);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                List<String> activeList = urgentMessages.isEmpty() ? backgroundMessages : urgentMessages;

                if (activeList.isEmpty()) {
                    continue;
                }

                if (activeList.size() == 1) {
                    Messenger.sendActionBar(player, activeList.get(0));
                } else {
                    int index = (int) ((tickCounter / 10) % activeList.size());
                    String msg = activeList.get(index);
                    String paginatedMsg = msg + " <dark_gray>[" + (index + 1) + "/" + activeList.size() + "]</dark_gray>";
                    Messenger.sendActionBar(player, paginatedMsg);
                }
            }
        }, 0L, 20L); // Runs every 1 second
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        providers.clear();
    }
}

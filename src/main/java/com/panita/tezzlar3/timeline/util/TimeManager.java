package com.panita.tezzlar3.timeline.util;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.config.Config;
import com.panita.tezzlar3.timeline.events.DayChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeManager {
    private static int currentDay = 1;
    private static String lastIncrementDate = "";
    private static int lastHour = -1;

    public static void init(JavaPlugin plugin) {
        currentDay = Config.raw().getInt("day", 1);
        lastIncrementDate = Config.raw().getString("last_increment_date", "");
        
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Mexico_City"));
            
            int currentHour = now.getHour();
            if (lastHour == -1) {
                lastHour = currentHour;
            } else if (lastHour != currentHour) {
                lastHour = currentHour;
                Bukkit.getPluginManager().callEvent(new com.panita.tezzlar3.timeline.events.HourChangeEvent(currentHour));
            }

            if (currentHour >= 14) {
                String todayDateStr = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
                if (!lastIncrementDate.equals(todayDateStr)) {
                    // Start of a new day!
                    int oldDay = currentDay;
                    int newDay = oldDay + 1;
                    setCurrentDay(newDay);
                    lastIncrementDate = todayDateStr;
                    Tezzlar.getConfigManager().updateString("last_increment_date", lastIncrementDate, null);
                    
                    Bukkit.getPluginManager().callEvent(new DayChangeEvent(oldDay, newDay));
                }
            }
        }, 20L * 60, 20L * 60); // Check every minute
    }

    public static int getCurrentDay() {
        return currentDay;
    }

    public static void setCurrentDay(int day) {
        currentDay = day;
        Tezzlar.getConfigManager().updateInt("day", day, null);
    }
}

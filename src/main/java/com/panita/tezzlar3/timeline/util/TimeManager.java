package com.panita.tezzlar3.timeline.util;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.config.Config;

public class TimeManager {
    private static int currentDay = 1;

    public static void init() {
        currentDay = Config.raw().getInt("day", 1);
    }

    public static int getCurrentDay() {
        return currentDay;
    }

    public static void setCurrentDay(int day) {
        currentDay = day;
        Tezzlar.getConfigManager().updateInt("day", day, null);
    }
}

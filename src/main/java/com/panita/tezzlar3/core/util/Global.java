package com.panita.tezzlar3.core.util;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.config.Config;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Global {
    public static String RAW_PREFIX = "";
    public static Component PREFIX;
    public static final Map<String, CommandMeta> ROOT_COMMANDS = new HashMap<>();

    public static void load() {
        RAW_PREFIX = Config.raw().getString("prefix", "");
        PREFIX = Messenger.mini(RAW_PREFIX);
    }

    public static boolean hasPermission(Player player, String permission) {
        if (permission == null || permission.isEmpty()) return true;

        return player.isOp() || player.hasPermission(permission);
    }

    public static double normalize(double value, double min, double max) {
        if (max == min) return 0.0;
        return Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
    }

    public static double ticksToGameDays(long ticks) {
        return (double) ticks / 24000; // 24,000 ticks = 1 game day
    }

    public static double ticksToHours(long ticks) {
        return (double) ticks / 72000; // 72,000 ticks = 1 hour
    }

    public static double hoursToTicks(double hours) {
        return hours * 72000; // 1 hour = 72,000 ticks
    }

    public static double ticksToSeconds(long ticks) {
        return (double) ticks / 20; // 20 ticks = 1 second
    }

    public static double secondsToTicks(double seconds) {
        return seconds * 20; // 1 second = 20 ticks
    }

    public static double ticksToMinutes(long ticks) {
        return (double) ticks / 1200; // 1200 ticks = 1 minute
    }

    public static double minutesToTicks(double minutes) {
        return minutes * 1200; // 1 minute = 1200 ticks
    }

    /**
     * Converts a duration in milliseconds into a human-readable string (days, hours, minutes).
     * @param diffMillis The duration in milliseconds.
     * @return A formatted string such as "2 días y 4 horas".
     */
    public static String formatDuration(long diffMillis) {
        if (diffMillis <= 0) return "unos instantes";
        
        long diffHours = diffMillis / (60 * 60 * 1000);
        long days = diffHours / 24;
        long hours = diffHours % 24;
        long diffMinutes = (diffMillis / (60 * 1000)) % 60;
        
        if (days > 0) {
            return days + " días y " + hours + " horas";
        } else if (hours > 0) {
            return hours + " horas y " + diffMinutes + " minutos";
        } else {
            return diffMinutes + " minutos";
        }
    }
}


package com.panita.tezzlar3.core.util;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.config.Config;
import com.panita.tezzlar3.core.config.ConfigDefaults;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;

public class Global {
    public static String RAW_PREFIX = "";
    public static Component PREFIX;
    public static String WORLD_NAME;
    public static final Map<String, CommandMeta> ROOT_COMMANDS = new HashMap<>();

    public static void load() {
        RAW_PREFIX = Config.raw().getString("prefix", "");
        PREFIX = Messenger.mini(RAW_PREFIX);
        WORLD_NAME = Config.raw().getString("worldName", ConfigDefaults.WORLD_NAME);
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
     * Formats ticks into HH:mm:ss or mm:ss
     */
    public static String formatTimeTicks(long ticks) {
        long totalSeconds = ticks / 20;
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        if (h > 0) {
            return String.format("%02d:%02d:%02d", h, m, s);
        } else {
            return String.format("%02d:%02d", m, s);
        }
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
        
        String dStr = days == 1 ? " día" : " días";
        String hStr = hours == 1 ? " hora" : " horas";
        String mStr = diffMinutes == 1 ? " minuto" : " minutos";
        
        if (days > 0) {
            return days + dStr + " y " + hours + hStr;
        } else if (hours > 0) {
            return hours + hStr + " y " + diffMinutes + mStr;
        } else {
            return diffMinutes + mStr;
        }
    }

    /**
     * Parses a string duration (e.g. 1w2d3h4m5s) into total milliseconds.
     * Supported units: w (weeks), d (days), h (hours), m (minutes), s (seconds).
     * If the string is purely numeric, it is treated as hours for backwards compatibility.
     * @param input The duration string.
     * @return Total milliseconds, or 0 if invalid.
     */
    public static long parseDurationToMillis(String input) {
        if (input == null || input.isEmpty()) return 0;
        
        // Backwards compatibility for plain numbers (hours)
        if (input.matches("\\d+")) {
            return Long.parseLong(input) * 60L * 60L * 1000L;
        }
        
        long totalMillis = 0;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)(w|d|h|m|s)").matcher(input.toLowerCase());
        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
                case "w": totalMillis += value * 7L * 24L * 60L * 60L * 1000L; break;
                case "d": totalMillis += value * 24L * 60L * 60L * 1000L; break;
                case "h": totalMillis += value * 60L * 60L * 1000L; break;
                case "m": totalMillis += value * 60L * 1000L; break;
                case "s": totalMillis += value * 1000L; break;
            }
        }
        return totalMillis;
    }

    /**
     * Validates if an offline player is valid (whether they are online or have played before).
     * If they are not valid, it automatically sends an error message to the sender.
     *
     * @param sender The CommandSender executing the action.
     * @param target The OfflinePlayer to evaluate.
     * @param targetName The name used in the command to display in the message.
     * @return true if valid and has played before, false otherwise.
     */
    public static boolean isValidPlayer(CommandSender sender, OfflinePlayer target, String targetName) {
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            Messenger.prefixedSend(sender, "&cEl jugador " + targetName + " nunca ha entrado al servidor.");
            return false;
        }
        return true;
    }
}


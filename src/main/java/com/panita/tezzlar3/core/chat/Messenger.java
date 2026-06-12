package com.panita.tezzlar3.core.chat;

import com.panita.tezzlar3.core.util.Global;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Messenger {
    private static final MiniMessage mini = MiniMessage.miniMessage();
    private static final ConcurrentHashMap<UUID, Map<String, BossBar>> activeBars = new ConcurrentHashMap<>();

    // ----> Basic Mini <----

    public static Component mini(String msg) {
        if (msg == null) return Component.empty();
        String converted = LegacyToMiniConverter.convert(msg);
        return mini.deserialize(converted);
    }

    public static Component miniPrefixed(String msg) {
        String prefixMini = LegacyToMiniConverter.convert(Global.RAW_PREFIX);
        String bodyMini = LegacyToMiniConverter.convert(msg);
        return mini.deserialize(prefixMini + bodyMini);
    }

    private static String applyPlaceholders(Player player, String msg) {
        if (msg == null) return "";
        // Safely check if PlaceholderAPI is actually loaded, otherwise just return raw message.
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, msg);
        }
        return msg;
    }

    // ----> Senders <----

    /**
     * Sends a message to a command sender.
     *
     * @param sender The command sender to send the message to.
     * @param msg The raw message to send.
     */
    public static void send(CommandSender sender, String msg) {
        sender.sendMessage(mini(msg));
    }

    /**
     * Sends a message to a command sender with the plugin prefix.
     *
     * @param sender The command sender to send the message to.
     * @param msg The raw message to send.
     */
    public static void prefixedSend(CommandSender sender, String msg) {
        sender.sendMessage(miniPrefixed(msg));
    }

    /**
     * Sends a message to a player.
     *
     * @param player The player to send the message to.
     * @param raw The raw message to send.
     */
    public static void send(Player player, String raw) {
        player.sendMessage(mini(raw));
    }

    /**
     * Sends a message to a player with the plugin prefix.
     *
     * @param player The player to send the message to.
     * @param raw The raw message to send.
     */
    public static void prefixedSend(Player player, String raw) {
        player.sendMessage(miniPrefixed(raw));
    }

    /**
     * Sends a message to a player applying PlaceholderAPI placeholders for a specific context player.
     *
     * @param receiver The player to send the message to.
     * @param context The player to apply placeholders for.
     * @param raw The raw message to send.
     */
    public static void placeholderSend(Player receiver, Player context, String raw) {
        String parsed = applyPlaceholders(context, raw);
        receiver.sendMessage(mini(parsed));
    }

    /**
     * Sends a message to a player applying PlaceholderAPI placeholders for themselves.
     *
     * @param receiver The player to send the message to.
     * @param raw The raw message to send.
     */
    public static void placeholderSend(Player receiver, String raw) {
        placeholderSend(receiver, receiver, raw);
    }

    /**
     * Sends a message to a player with the plugin prefix and applies PlaceholderAPI placeholders for a specific context player.
     *
     * @param receiver The player to send the message to.
     * @param context The player to apply placeholders for.
     * @param raw The raw message to send.
     */
    public static void prefixedPlaceholderSend(Player receiver, Player context, String raw) {
        String parsed = applyPlaceholders(context, raw);
        receiver.sendMessage(miniPrefixed(parsed));
    }

    /**
     * Sends a message to a player applying PlaceholderAPI placeholders for themselves.
     *
     * @param receiver The player to send the message to.
     * @param raw The raw message to send.
     */
    public static void prefixedPlaceholderSend(Player receiver, String raw) {
        prefixedPlaceholderSend(receiver, receiver, raw);
    }

    // ----> Broadcast <----

    /**
     * Broadcasts a message to all players.
     *
     * @param msg The raw message to send.
     */
    public static void broadcast(String msg) {
        Component message = mini(msg);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
        Bukkit.getConsoleSender().sendMessage(message);
    }

    /**
     * Broadcasts a message to all players with the plugin prefix.
     *
     * @param msg The raw message to send.
     */
    public static void prefixedBroadcast(String msg) {
        Component message = miniPrefixed(msg);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
        Bukkit.getConsoleSender().sendMessage(message);
    }

    /**
     * Broadcasts a message to all players and applies PlaceholderAPI placeholders for a specific player.
     *
     * @param context The player to apply placeholders for.
     * @param raw The raw message to send.
     */
    public static void placeholderBroadcast(Player context, String raw) {
        String parsed = applyPlaceholders(context, raw);
        Component message = mini(parsed);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
        Bukkit.getConsoleSender().sendMessage(message);
    }

    /**
     * Broadcasts a message to all players with the plugin prefix and applies PlaceholderAPI placeholders for a specific player.
     *
     * @param context The player to apply placeholders for.
     * @param raw The raw message to send.
     */
    public static void prefixedPlaceholderBroadcast(Player context, String raw) {
        String parsed = applyPlaceholders(context, raw);
        Component message = miniPrefixed(parsed);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
        Bukkit.getConsoleSender().sendMessage(message);
    }

    // ---> BossBar <----

    /**
     * Creates or updates a boss bar for a player.
     * @param player The player to show the boss bar to.
     * @param rawMsg The raw message to display on the boss bar.
     * @param color The color of the boss bar.
     * @param overlay The overlay style of the boss bar.
     * @param progress The progress of the boss bar (0.0 to 1.0).
     */
    public static void showBossBar(Player player, String id, String rawMsg, BossBar.Color color, BossBar.Overlay overlay, float progress) {
        String parsed = applyPlaceholders(player, rawMsg);
        Component title = mini(parsed);

        Map<String, BossBar> bars = activeBars.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        BossBar bar = bars.getOrDefault(id, BossBar.bossBar(title, progress, color, overlay));
        bar.name(title);
        bar.color(color);
        bar.overlay(overlay);
        bar.progress(progress);

        player.showBossBar(bar);
        bars.put(id, bar);
    }

    /**
     * Hides and removes a boss bar from a player.
     * @param player The player to hide the boss bar from.
     * @param id The identifier of the boss bar to hide.
     */
    public static void hideBossBar(Player player, String id) {
        Map<String, BossBar> bars = activeBars.get(player.getUniqueId());
        if (bars != null) {
            BossBar bar = bars.remove(id);
            if (bar != null) {
                player.hideBossBar(bar);
            }
            if (bars.isEmpty()) activeBars.remove(player.getUniqueId());
        }
    }

    // ---> Titles <----

    /**
     * Shows a title and subtitle to a player with specified timings.
     * @param player The player to show the title to.
     * @param rawTitle The raw title message.
     * @param rawSub The raw subtitle message.
     * @param fadeIn The duration for the title to fade in.
     * @param stay The duration for the title to stay on screen.
     * @param fadeOut The duration for the title to fade out.
     */
    public static void showTitle(Player player, String rawTitle, String rawSub, Duration fadeIn, Duration stay, Duration fadeOut) {
        String parsedTitle = applyPlaceholders(player, rawTitle);
        String parsedSub   = applyPlaceholders(player, rawSub);

        Title title = Title.title(mini(parsedTitle), mini(parsedSub), Title.Times.times(fadeIn, stay, fadeOut));
        player.showTitle(title);
    }

    /**
     * Shows a title to a player with specified timings and no subtitle.
     * @param player The player to show the title to.
     * @param rawTitle The raw title message.
     * @param fadeIn The duration for the title to fade in.
     * @param stay The duration for the title to stay on screen.
     * @param fadeOut The duration for the title to fade out.
     */
    public static void showTitle(Player player, String rawTitle, Duration fadeIn, Duration stay, Duration fadeOut) {
        showTitle(player, rawTitle, "", fadeIn, stay, fadeOut);
    }

    /**
     * Clears any active title from a player.
     * @param player The player to clear the title from.
     */
    public static void clearTitle(Player player) {
        player.clearTitle();
    }

    // ---> Extra <----

    /**
     * Sends a message to the console if the sender is not a player.
     *
     * @param sender The command sender (should be a console).
     * @param msg The raw message to send.
     */
    public static void consoleSend(CommandSender sender, String msg) {
        if (!(sender instanceof Player)) {
            send(sender, msg);
        }
    }
}

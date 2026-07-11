package com.panita.tezzlar3.core.util;

import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SoundUtils {

    private static final Map<String, Sound> soundCache = new HashMap<>();

    /**
     * Gets (and caches) a sound from config-friendly string keys.
     * Example: "entity.vindicator.hurt" or "block.chest.open"
     *
     * @param key the string key (lowercase, dot-separated)
     * @return the Sound if found, or null if invalid
     */
    public static Sound getSound(String key) {
        if (key == null || key.isEmpty()) return null;

        return soundCache.computeIfAbsent(key.toLowerCase(), k -> {
            try {
                return Registry.SOUND_EVENT.getOrThrow(Key.key(k));
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("[Tezzlar] Invalid sound key: " + k);
                return null;
            }
        });
    }

    /**
     * Plays a sound to a player if the key is valid.
     *
     * @param player   target player
     * @param key      sound key from config
     * @param volume   volume
     * @param pitch    pitch
     */
    public static void play(Player player, String key, float volume, float pitch) {
        Sound sound = getSound(key);
        if (sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    /**
     * Plays a sound in the world so nearby players can hear it.
     *
     * @param loc    The location to play the sound at.
     * @param key    The sound key.
     * @param radius The rough radius in blocks where the sound should be heard.
     * @param pitch  The pitch.
     */
    public static void playInRadius(org.bukkit.Location loc, String key, float radius, float pitch) {
        Sound sound = getSound(key);
        if (sound != null && loc != null && loc.getWorld() != null) {
            float volume = radius / 16.0f;
            loc.getWorld().playSound(loc, sound, volume, pitch);
        }
    }

    public static void playGlobal(String soundKey, float volume, float pitch) {
        Sound sound = getSound(soundKey);
        if (sound == null) {
            sound = Sound.ENTITY_PLAYER_LEVELUP;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
}

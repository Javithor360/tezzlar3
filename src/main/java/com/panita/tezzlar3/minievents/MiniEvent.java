package com.panita.tezzlar3.minievents;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public interface MiniEvent {

    /**
     * Starts the event.
     * @param plugin The JavaPlugin instance.
     */
    void start(JavaPlugin plugin);

    /**
     * Stops the event and cleans up listeners or tasks.
     * @param plugin The JavaPlugin instance.
     */
    void stop(JavaPlugin plugin);

    /**
     * Gets the unique internal ID of the event for configuration saving.
     */
    String getId();

    /**
     * Gets the human-readable display name (supports MiniMessage).
     */
    String getDisplayName();
    
    /**
     * Gets the generic display name for GUIs before the event starts.
     * @return The generic display name
     */
    default String getGenericName() {
        return getDisplayName();
    }

    /**
     * Gets the description of the event for chat broadcasting (supports MiniMessage).
     */
    String getDescription();

    /**
     * Gets the total duration of the event in ticks.
     * Return 0 if the event is instantaneous.
     */
    long getDurationTicks();

    /**
     * Optional method to serialize extra state data if the event needs it.
     */
    default Map<String, Object> serializeExtraData() {
        return null;
    }

    /**
     * Optional method to restore extra state data after a reboot.
     */
    default void loadExtraData(Map<String, Object> data) {
    }
}

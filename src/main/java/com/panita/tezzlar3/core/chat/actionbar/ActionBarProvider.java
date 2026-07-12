package com.panita.tezzlar3.core.chat.actionbar;

import org.bukkit.entity.Player;

import java.util.List;

public interface ActionBarProvider {
    /**
     * Gets the unique identifier for this provider.
     * @return the provider ID
     */
    String getId();

    /**
     * Returns the texts to display in the action bar for the given player.
     * Return null or empty list if no message should be displayed.
     *
     * @param player the player
     * @return the action bar texts or null
     */
    List<String> getTexts(Player player);

    /**
     * Determines if this provider's message is urgent.
     * Urgent messages override the cyclic pagination and display immediately.
     * If multiple urgent messages exist, they might still paginate or overlap,
     * but they take priority over background messages.
     *
     * @param player the player
     * @return true if urgent, false otherwise
     */
    default boolean isUrgent(Player player) {
        return false;
    }
}

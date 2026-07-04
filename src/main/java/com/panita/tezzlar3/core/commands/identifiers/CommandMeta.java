package com.panita.tezzlar3.core.commands.identifiers;

import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.SuggestionProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CommandMeta holds the metadata for a command, including its permission, syntax, and description.
 * It also handles permission checks and player-only restrictions.
 */
public class CommandMeta {
    private final AdvancedCommand command;
    private final String permission;
    private final boolean playerOnly;
    private final String syntax;
    private final String description;
    private final Map<String, CommandMeta> subCommands = new HashMap<>();
    private final List<String> arguments = new ArrayList<>();
    private final Map<Integer, SuggestionProvider> argumentSuggestions = new HashMap<>();

    /**
     * Constructor for CommandMeta.
     *
     * @param command The command associated with this metadata.
     * @param permission The permission required to execute the command.
     * @param playerOnly Whether the command is restricted to players only.
     * @param syntax The syntax of the command.
     * @param description A description of the command.
     */
    public CommandMeta(AdvancedCommand command, String permission, boolean playerOnly, String syntax, String description) {
        this.command = command;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.syntax = syntax;
        this.description = description;
    }

    /**
     * Checks if the sender has permission to execute the command.
     * Also checks if the command is restricted to players only.
     *
     * @param sender The command sender (e.g., a player).
     * @return true if the sender has permission and meets the conditions; false otherwise.
     */
    public String check(CommandSender sender) {
        // Check if the command is restricted to players only
        if (playerOnly && !(sender instanceof Player)) { // Check the instance of the sender
            return "player_only";
        }

        // Check if the sender has the required permission
        if (!permission.isEmpty() && !sender.hasPermission(permission)) {
            return "no_permission";
        }

        return "";
    }


    public AdvancedCommand getCommand() {
        return command;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isPlayerOnly() {
        return playerOnly;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, CommandMeta> getSubCommands() {
        return subCommands;
    }

    public void addSubCommand(String name, CommandMeta meta) {
        name = name.toLowerCase();
        if (this.subCommands.containsKey(name)) {
            CommandMeta existing = this.subCommands.get(name);
            meta.getSubCommands().putAll(existing.getSubCommands());
        }
        this.subCommands.put(name, meta);
    }

    /**
     * Retrieves a sub-command by its name.
     *
     * @param name The name of the sub-command.
     * @return The metadata for the sub-command, or null if not found.
     */
    public CommandMeta getSubcommandOrCreateInvalid(String name) {
        name = name.toLowerCase();
        if (!subCommands.containsKey(name)) {
            CommandMeta emptyMeta = new CommandMeta(null, "", false, "", "");
            subCommands.put(name, emptyMeta);
            return emptyMeta;
        }
        return subCommands.get(name);
    }

    public void setArgumentSuggestion(int index, SuggestionProvider provider) {
        argumentSuggestions.put(index, provider);
    }

    public SuggestionProvider getSuggestionProvider(int index) {
        return argumentSuggestions.get(index);
    }
}


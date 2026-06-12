package com.panita.tezzlar3.core.commands.dynamic;

import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import org.bukkit.command.CommandSender;

/**
 * Context for tab completion, containing the command sender, arguments, and metadata.
 */
public class TabContext {
    private final CommandSender sender;
    private final String[] args;
    private final int consumed;
    private final CommandMeta currentMeta;

    /**
     * Constructor for TabContext.
     *
     * @param sender      The command sender.
     * @param args        The command arguments.
     * @param consumed    The number of arguments consumed.
     * @param currentMeta The current command metadata.
     */
    public TabContext(CommandSender sender, String[] args, int consumed, CommandMeta currentMeta) {
        this.sender = sender;
        this.args = args;
        this.consumed = consumed;
        this.currentMeta = currentMeta;
    }

    public CommandSender getSender() {
        return sender;
    }

    public String[] getArgs() {
        return args;
    }

    public int getConsumed() {
        return consumed;
    }

    public CommandMeta getCurrentMeta() {
        return currentMeta;
    }

    /**
     * Gets the index of the current argument.
     * This is calculated as the total number of arguments minus the number of consumed arguments minus one.
     *
     * @return The index of the current argument.
     */
    public int getArgumentIndex() {
        return Math.max(0, args.length - consumed - 1);
    }

    /**
     * Gets the current argument based on the index.
     * If the index is out of bounds, an empty string is returned.
     *
     * @return The current argument.
     */
    public String getCurrentArg() {
        int i = args.length - 1;
        return i >= 0 ? args[i] : "";
    }

    public boolean isNewArgument() {
        return args.length == consumed || getCurrentArg().isEmpty();
    }
}


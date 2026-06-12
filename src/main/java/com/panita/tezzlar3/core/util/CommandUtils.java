package com.panita.tezzlar3.core.util;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.CommandMetadataCache;
import org.bukkit.command.CommandSender;

public class CommandUtils {
    /**
     * Checks if the number of arguments is less than the required amount.
     *
     * @param sender The command sender
     * @param args The command arguments
     * @param required The required number of arguments
     * @param commandClass The class of the command
     * @return true if the number of arguments is valid, false otherwise
     */
    public static boolean checkArgsOrUsage(CommandSender sender, String[] args, int required, Class<?> commandClass) {
        if (args.length < required) {
            String syntax = CommandMetadataCache.getSyntax(commandClass);
            Messenger.prefixedSend(sender, "&7Uso: &b" + syntax);
            return false;
        }
        return true;
    }
}


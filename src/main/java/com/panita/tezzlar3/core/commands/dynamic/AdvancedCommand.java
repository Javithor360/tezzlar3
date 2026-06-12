package com.panita.tezzlar3.core.commands.dynamic;

import org.bukkit.command.CommandSender;

/**
 * Interface for advanced commands in the Tezzlar plugin.
 */
public interface AdvancedCommand {
    void execute(CommandSender sender, String[] args);
}


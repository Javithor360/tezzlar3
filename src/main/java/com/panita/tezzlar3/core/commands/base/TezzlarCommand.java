package com.panita.tezzlar3.core.commands.base;

import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandSpec(
        name = "tezzlar",
        description = "tezzlar's main command",
        syntax = "/tezzlar <subcommand>",
        aliases = {"pc", "panita"},
        permission = "tezzlar.command.tezzlar"
)
public class TezzlarCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        Bukkit.dispatchCommand(sender, "tezzlar help");
    }
}


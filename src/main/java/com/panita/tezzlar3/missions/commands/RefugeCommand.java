package com.panita.tezzlar3.missions.commands;

import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import org.bukkit.command.CommandSender;

@CommandSpec(
        name = "refuge",
        description = "Manage the Refuge event.",
        syntax = "/refuge <activate|cancel>",
        permission = "tezzlar.command.refuge",
        aliases = {"refugio"},
        playerOnly = false
)
public class RefugeCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass());
    }
}

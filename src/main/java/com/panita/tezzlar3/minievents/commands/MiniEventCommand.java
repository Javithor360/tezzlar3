package com.panita.tezzlar3.minievents.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import org.bukkit.command.CommandSender;

@CommandSpec(
        name = "minievent",
        description = "Manage Tezzlar 3 mini events.",
        syntax = "/minievent <start|stop>",
        permission = "tezzlar.command.minievent"
)
public class MiniEventCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass());
    }
}

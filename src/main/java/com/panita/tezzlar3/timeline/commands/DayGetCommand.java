package com.panita.tezzlar3.timeline.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.command.CommandSender;

@SubCommandSpec(
        parent = "tezzlar day",
        name = "get",
        description = "Shows the current global day of the server.",
        syntax = "/tezzlar day get",
        permission = "tezzlar.command.day.get"
)
public class DayGetCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        Messenger.prefixedSend(sender, "&aEl día global actual es: &e" + TimeManager.getCurrentDay());
    }
}

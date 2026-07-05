package com.panita.tezzlar3.missions.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import org.bukkit.command.CommandSender;

@CommandSpec(
        name = "mission",
        description = "Missions administration command.",
        syntax = "/mission",
        permission = "tezzlar.command.mission"
)
public class MissionCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        Messenger.prefixedSend(sender, "&7Uso: &b/mission <check>");
    }
}

package com.panita.tezzlar3.missions.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import org.bukkit.command.CommandSender;

@CommandSpec(
        name = "rewards",
        description = "Rewards main command",
        syntax = "/rewards <subcommand>",
        permission = "tezzlar.command.rewards"
)
public class RewardsCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        Messenger.prefixedSend(sender, "&7Uso: &b/rewards claim");
    }
}

package com.panita.tezzlar3.difficulty.commands.deathtrain;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import org.bukkit.command.CommandSender;

@CommandSpec(
        name = "deathtrain",
        description = "DeathTrain administration commands.",
        syntax = "/deathtrain",
        permission = "tezzlar.command.deathtrain"
)
public class DeathTrainCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        Messenger.prefixedSend(sender, "&7Uso: &b/deathtrain <start|stop|add|remove|status>");
    }
}

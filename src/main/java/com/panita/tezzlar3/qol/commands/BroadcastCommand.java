package com.panita.tezzlar3.qol.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import org.bukkit.command.CommandSender;

@CommandSpec(
        name = "broadcast",
        aliases = {"bc", "anuncio"},
        description = "Envía un mensaje global a todos los jugadores usando MiniMessage.",
        syntax = "/broadcast <mensaje...>",
        permission = "tezzlar.command.qol.broadcast"
)
public class BroadcastCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        String message = String.join(" ", args);
        
        // Use standard broadcast so the sender can use <red>, <bold>, etc.
        // It will parse MiniMessage formatting natively.
        Messenger.broadcast(message);
    }
}

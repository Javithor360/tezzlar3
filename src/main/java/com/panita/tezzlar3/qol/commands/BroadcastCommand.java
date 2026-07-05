package com.panita.tezzlar3.qol.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.core.util.SoundUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandSpec(
        name = "broadcast",
        aliases = {"bc", "anuncio"},
        description = "Send a global message to every player in the server.",
        syntax = "/broadcast <raw|prefixed> <mensaje...>",
        permission = "tezzlar.command.qol.broadcast"
)
public class BroadcastCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 2, this.getClass())) {
            Messenger.prefixedSend(sender, "&7Uso: &b/broadcast <raw|prefixed> <mensaje...>");
            return;
        }

        String type = args[0].toLowerCase();
        
        // Remove the first argument to get the actual message
        String[] messageArgs = new String[args.length - 1];
        System.arraycopy(args, 1, messageArgs, 0, args.length - 1);
        String message = String.join(" ", messageArgs);
        
        if (type.equals("raw")) {
            Messenger.broadcast(message);
        } else if (type.equals("prefixed")) {
            Messenger.prefixedBroadcast(message);
        } else {
            Messenger.prefixedSend(sender, "&7Uso: &b/broadcast <raw|prefixed> <mensaje...>");
            return;
        }
        
        SoundUtils.playGlobal("ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.0f);
    }
    
    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> List.of("raw", "prefixed"));
    }
}

package com.panita.tezzlar3.troll.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

@CommandSpec(
        name = "sudo",
        description = "Force a player to execute an specific action",
        syntax = "/sudo <player> <args>",
        permission = "tezzlar.command.troll.sudo"
)
public class SudoCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 2, this.getClass())) return;

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            Messenger.prefixedSend(sender, "&cEl jugador " + args[0] + " no está en línea.");
            return;
        }

        // Join arguments from index 1 to the end
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        
        target.chat(message);
        Messenger.prefixedSend(sender, "&aForzaste a &e" + target.getName() + " &aa ejecutar/decir: &7" + message);
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> {
            String current = context.getCurrentArg().toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(current))
                    .collect(Collectors.toList());
        });
    }
}

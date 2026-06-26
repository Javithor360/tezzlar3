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

import java.util.stream.Collectors;

@CommandSpec(
        name = "thor",
        aliases = {"rayo", "thunder"},
        description = "Strikes lightning upon the target player.",
        syntax = "/thor <player>",
        permission = "tezzlar.command.troll.thor"
)
public class ThorCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            Messenger.prefixedSend(sender, "&cEl jugador " + args[0] + " no está en línea.");
            return;
        }

        // Strike lightning at the player's location
        target.getWorld().strikeLightning(target.getLocation());
        Messenger.prefixedSend(sender, "&e¡Le ha caído un rayo divino a " + target.getName() + "!");
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

package com.panita.tezzlar3.troll.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.troll.gui.TrollMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandSpec(
        name = "troll",
        description = "Opens the troll menu for a target player.",
        syntax = "/troll <player>",
        permission = "tezzlar.command.troll.troll",
        playerOnly = true
)
public class TrollCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            Messenger.prefixedSend(sender, "&cEl jugador " + args[0] + " no está en línea.");
            return;
        }

        new TrollMenu(player, target).open();
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

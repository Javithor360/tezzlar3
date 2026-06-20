package com.panita.tezzlar3.inventory.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.inventory.gui.PlayerDeathsMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandSpec(
        name = "restoreinventory",
        aliases = {"invrestore", "invrecover", "irestore"},
        description = "Abre un panel de recuperación de inventarios de muertes de un jugador.",
        syntax = "/restoreinventory <jugador>",
        permission = "tezzlar.command.restoreinventory",
        playerOnly = true
)
public class RestoreInventoryCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        Player staff = (Player) sender;
        String targetName = args[0];

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!Global.isValidPlayer(sender, target, targetName)) {
            return;
        }

        new PlayerDeathsMenu(staff, target.getUniqueId(), target.getName()).open();
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

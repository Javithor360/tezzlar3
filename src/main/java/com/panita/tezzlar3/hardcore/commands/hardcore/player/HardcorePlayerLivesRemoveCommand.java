package com.panita.tezzlar3.hardcore.commands.hardcore.player;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@SubCommandSpec(
        parent = "tezzlar hardcore player lives",
        name = "remove",
        description = "Removes lives from a player.",
        syntax = "/tezzlar hardcore player lives remove <player> <amount>",
        permission = "tezzlar.command.hardcore.player.lives.remove"
)
public class HardcorePlayerLivesRemoveCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 2, this.getClass())) return;

        String targetName = args[0];
        int amount;

        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            Messenger.prefixedSend(sender, "&cLa cantidad debe ser un número válido.");
            return;
        }

        if (amount < 0) {
            Messenger.prefixedSend(sender, "&cLa cantidad no puede ser negativa.");
            return;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            Messenger.prefixedSend(sender, "&cEse jugador nunca se ha conectado al servidor.");
            return;
        }
        
        if (!Global.isValidPlayer(sender, target, targetName)) {
            return;
        }

        int currentLives = HardcoreDataManager.getLives(target.getUniqueId(), target.getName());
        int newLives = Math.max(0, currentLives - amount);
        
        HardcoreDataManager.setLives(target.getUniqueId(), target.getName(), newLives);
        String vidasStr = amount == 1 ? " vida" : " vidas";
        Messenger.prefixedSend(sender, "&aSe han restado &e" + amount + vidasStr + " &aa &e" + target.getName() + "&a. (Quedan: &e" + newLives + "&a)");
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

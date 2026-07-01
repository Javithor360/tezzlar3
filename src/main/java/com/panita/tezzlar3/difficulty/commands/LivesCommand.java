package com.panita.tezzlar3.difficulty.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandSpec(
        name = "lives",
        aliases = {"vidas"},
        description = "Display the lives amount for a specific player",
        syntax = "/lives [player]"
)
public class LivesCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        OfflinePlayer target;

        if (args.length >= 1) {
            String targetName = args[0];
            @SuppressWarnings("deprecation")
            OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
            target = op;

            if (!target.hasPlayedBefore() && !target.isOnline()) {
                Messenger.prefixedSend(sender, "&cEse jugador nunca se ha conectado al servidor.");
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                Messenger.prefixedSend(sender, "&cDebes especificar un jugador si ejecutas este comando desde la consola.");
                return;
            }
            target = (Player) sender;
        }

        int deaths = HardcoreDataManager.getDeaths(target.getUniqueId(), target.getName());
        int lives = HardcoreDataManager.getLives(target.getUniqueId(), target.getName());
        int maxLives = HardcoreDataManager.getMaxLives(target.getUniqueId(), target.getName());

        Messenger.prefixedSend(sender, "&6--- Información de &e" + target.getName() + " &6---");
        Messenger.send(sender, "&3> &7Vidas: &a" + lives + "&8/&a" + maxLives);
        Messenger.send(sender, "&3> &7Muertes: &c" + deaths);
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

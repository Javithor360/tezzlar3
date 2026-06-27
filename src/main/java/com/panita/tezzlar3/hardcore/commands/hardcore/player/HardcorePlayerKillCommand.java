package com.panita.tezzlar3.hardcore.commands.hardcore.player;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.stream.Collectors;

@SubCommandSpec(
        parent = "tezzlar hardcore player",
        name = "kill",
        description = "Applies a preventive ban to a player without adding deaths.",
        syntax = "/tezzlar hardcore player kill <player> [time]",
        permission = "tezzlar.command.hardcore.player.kill"
)
public class HardcorePlayerKillCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        String targetName = args[0];
        long durationMillis = 24 * 3600000L; // Default 24 hours
        String formattedDuration = "24 horas";

        if (args.length >= 2) {
            long parsed = Global.parseDurationToMillis(args[1]);
            if (parsed <= 0) {
                Messenger.prefixedSend(sender, "&cEl tiempo ingresado no es válido. Usa formatos como 1w2d3h o un número de horas.");
                return;
            }
            durationMillis = parsed;
            formattedDuration = Global.formatDuration(durationMillis);
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!Global.isValidPlayer(sender, target, targetName)) {
            return;
        }

        Instant expirationInstant = Instant.now().plusMillis(durationMillis);

        HardcoreDataManager.setBanExpiration(target.getUniqueId(), target.getName(), expirationInstant.toEpochMilli());

        if (target.isOnline()) {
            Player onlinePlayer = target.getPlayer();
            if (onlinePlayer != null) {
                onlinePlayer.kick(Messenger.mini("<red>Has sido ejecutado preventivamente por un administrador durante " + formattedDuration + ".</red>"));
            }
        }

        Messenger.prefixedSend(sender, "&aSe ha aplicado un ban de muerte a &e" + target.getName() + "&a durante &e" + formattedDuration + "&a sin quitarle vidas.");
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

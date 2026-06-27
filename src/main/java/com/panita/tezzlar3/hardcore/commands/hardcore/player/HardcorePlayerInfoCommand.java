package com.panita.tezzlar3.hardcore.commands.hardcore.player;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.config.CustomConfig;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

@SubCommandSpec(
        parent = "tezzlar hardcore player",
        name = "info",
        description = "Shows the information of a player.",
        syntax = "/tezzlar hardcore player info <player>",
        permission = "tezzlar.command.hardcore.player.info"
)
public class HardcorePlayerInfoCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        String targetName = args[0];
        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            Messenger.prefixedSend(sender, "&cEse jugador nunca se ha conectado al servidor.");
            return;
        }

        if (!Global.isValidPlayer(sender, target, targetName)) {
            return;
        }

        int deaths = HardcoreDataManager.getDeaths(target.getUniqueId(), target.getName());
        int lives = HardcoreDataManager.getLives(target.getUniqueId(), target.getName());
        int maxLives = HardcoreDataManager.getMaxLives(target.getUniqueId(), target.getName());

        String lastPlayedStr;
        if (target.isOnline()) {
            lastPlayedStr = "&aEn línea";
        } else {
            long lastPlayed = target.getLastPlayed();
            if (lastPlayed == 0) {
                lastPlayedStr = "&cNunca";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                lastPlayedStr = "&b" + sdf.format(new Date(lastPlayed));
            }
        }
        
        String playtimeStr = "%statistic_time_played%";
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            playtimeStr = PlaceholderAPI.setPlaceholders(target, playtimeStr);
        }

        Messenger.prefixedSend(sender, "&6--- Información de &e" + target.getName() + " &6---");
        Messenger.send(sender, "&8> &7Vidas: &a" + lives + "&8/&a" + maxLives);
        Messenger.send(sender, "&8> &7Muertes: &c" + deaths);
        Messenger.send(sender, "&8> &7Tiempo de juego: &b" + playtimeStr);
        Messenger.send(sender, "&3> &7Última conexión: " + lastPlayedStr);
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

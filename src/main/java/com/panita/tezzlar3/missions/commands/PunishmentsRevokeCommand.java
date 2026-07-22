package com.panita.tezzlar3.missions.commands;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.config.CustomConfig;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.missions.util.MissionsConfigDefaults;
import com.panita.tezzlar3.core.util.CommandUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SubCommandSpec(
        parent = "punishments",
        name = "revoke",
        description = "Revokes a punishment from a player.",
        syntax = "/punishments revoke <player> <punishment>",
        permission = "tezzlar3.admin"
)
public class PunishmentsRevokeCommand implements AdvancedCommand, TabSuggestingCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 2, this.getClass())) return;

        String target = args[0];
        String punishment = args[1].toUpperCase();

        Player onlineTarget = Bukkit.getPlayerExact(target);
        if (onlineTarget != null) {
            PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(onlineTarget);
            if (data != null) {
                if (!data.hasPunishment(punishment)) {
                    Messenger.prefixedSend(sender, "&cEl jugador &e" + target + " &cno tiene el castigo &e" + punishment + " &cactivo.");
                } else {
                    data.removePunishment(punishment);
                    Messenger.prefixedSend(sender, "&aSe ha removido el castigo &e" + punishment + " &aal jugador &e" + target);
                }
            }
        } else {
            File dataDir = new File(Tezzlar.getInstance().getDataFolder(), "data");
            File playerFile = new File(dataDir, target + ".yml");
            
            if (playerFile.exists()) {
                CustomConfig customConfig = new CustomConfig(Tezzlar.getInstance(), "data", target + ".yml");
                FileConfiguration config = customConfig.getConfig();
                List<String> activePunishments = config.getStringList("active_punishments");
                
                if (!activePunishments.contains(punishment)) {
                    Messenger.prefixedSend(sender, "&cEl jugador &e" + target + " &cno tiene el castigo &e" + punishment + " &cactivo (Offline).");
                } else {
                    activePunishments.remove(punishment);
                    config.set("active_punishments", activePunishments);
                    customConfig.save();
                    Messenger.prefixedSend(sender, "&aSe ha removido el castigo &e" + punishment + " &aal jugador &e" + target + " &7(Offline)");
                }
            } else {
                Messenger.prefixedSend(sender, "&cNo se encontraron datos para el jugador &e" + target);
            }
        }
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
        
        meta.setArgumentSuggestion(1, context -> {
            return new ArrayList<>(MissionsConfigDefaults.MISSIONS_PUNISHMENTS_DICTIONARY.keySet());
        });
    }
}

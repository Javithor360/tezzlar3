package com.panita.tezzlar3.missions.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.config.CustomConfig;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.missions.util.MissionsConfigDefaults;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.panita.tezzlar3.Tezzlar;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

@CommandSpec(
        name = "punishments",
        description = "Shows active punishments for a player.",
        syntax = "/punishments [player]",
        permission = "tezzlar.command.punishments"
)
public class PunishmentsCommand implements AdvancedCommand, TabSuggestingCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        String targetName;
        
        if (args.length > 0) {
            targetName = args[0];
        } else {
            if (!(sender instanceof Player player)) {
                Messenger.prefixedSend(sender, "&cDebes especificar un jugador si lo ejecutas desde la consola.");
                return;
            }
            targetName = player.getName();
        }

        Player onlineTarget = Bukkit.getPlayerExact(targetName);
        List<String> activePunishments = null;

        if (onlineTarget != null) {
            PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(onlineTarget);
            if (data != null) {
                activePunishments = new java.util.ArrayList<>(data.getActivePunishments());
            }
        } else {
            java.io.File dataDir = new java.io.File(Tezzlar.getInstance().getDataFolder(), "data");
            java.io.File playerFile = new java.io.File(dataDir, targetName + ".yml");
            
            if (playerFile.exists()) {
                CustomConfig customConfig = new CustomConfig(Tezzlar.getInstance(), "data", targetName + ".yml");
                FileConfiguration config = customConfig.getConfig();
                activePunishments = config.getStringList("active_punishments");
            }
        }

        if (activePunishments == null || activePunishments.isEmpty()) {
            if (sender.getName().equalsIgnoreCase(targetName)) {
                String emptyMsg = Tezzlar.getConfigManager().getString("missions.messages.punishments_empty", MissionsConfigDefaults.MISSIONS_MESSAGES_PUNISHMENTS_EMPTY);
                Messenger.prefixedSend(sender, emptyMsg);
            } else {
                Messenger.prefixedSend(sender, "&aEl jugador &e" + targetName + " &ano tiene castigos activos.");
            }
            return;
        }

        Messenger.prefixedSend(sender, "&cCastigos activos de &e" + targetName + "&c:");
        for (String punishment : activePunishments) {
            Messenger.prefixedSend(sender, " &7- &c" + punishment);
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
    }
}

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
        name = "info",
        description = "Shows the active punishments of a player.",
        syntax = "/punishments info <player>"
)
public class PunishmentsInfoCommand implements AdvancedCommand, TabSuggestingCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        String targetName = args[0];
        Player onlineTarget = Bukkit.getPlayerExact(targetName);
        List<String> activePunishments = null;

        if (onlineTarget != null) {
            PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(onlineTarget);
            if (data != null) {
                activePunishments = new ArrayList<>(data.getActivePunishments());
            }
        } else {
            File dataDir = new File(Tezzlar.getInstance().getDataFolder(), "data");
            File playerFile = new File(dataDir, targetName + ".yml");
            
            if (playerFile.exists()) {
                CustomConfig customConfig = new CustomConfig(Tezzlar.getInstance(), "data", targetName + ".yml");
                FileConfiguration config = customConfig.getConfig();
                activePunishments = config.getStringList("active_punishments");
            }
        }

        if (activePunishments == null || activePunishments.isEmpty()) {
            Messenger.prefixedSend(sender, "&aEl jugador &e" + targetName + " &ano tiene castigos activos.");
            return;
        }

        Messenger.prefixedSend(sender, "&cCastigos activos de &e" + targetName + "&c:");
        for (String punishment : activePunishments) {
            String defaultDictValue = MissionsConfigDefaults.MISSIONS_PUNISHMENTS_DICTIONARY.getOrDefault(punishment, punishment);
            String friendlyName = Tezzlar.getConfigManager().getString("missions.punishments_dictionary." + punishment, defaultDictValue);
            Messenger.prefixedSend(sender, " &7- &c" + friendlyName);
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

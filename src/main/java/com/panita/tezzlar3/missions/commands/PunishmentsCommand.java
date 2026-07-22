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

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CommandSpec(
        name = "punishments",
        aliases = {"castigos"},
        description = "Shows active punishments for a player.",
        syntax = "/punishments [player]"
)
public class PunishmentsCommand implements AdvancedCommand, TabSuggestingCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Messenger.prefixedSend(sender, "&cDebes ejecutar esto como jugador, o usar /punishments info <jugador>.");
            return;
        }

        PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
        List<String> activePunishments = null;
        if (data != null) {
            activePunishments = new ArrayList<>(data.getActivePunishments());
        }

        if (activePunishments == null || activePunishments.isEmpty()) {
            String emptyMsg = Tezzlar.getConfigManager().getString("missions.messages.punishments_empty", MissionsConfigDefaults.MISSIONS_MESSAGES_PUNISHMENTS_EMPTY);
            Messenger.prefixedSend(sender, emptyMsg);
            return;
        }

        Messenger.prefixedSend(sender, "&cTus castigos activos:");
        for (String punishment : activePunishments) {
            String defaultDictValue = MissionsConfigDefaults.MISSIONS_PUNISHMENTS_DICTIONARY.getOrDefault(punishment, punishment);
            String friendlyName = Tezzlar.getConfigManager().getString("missions.punishments_dictionary." + punishment, defaultDictValue);
            Messenger.prefixedSend(sender, " &7- &c" + friendlyName);
        }
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        // No root arguments expected since subcommands handle them
    }
}

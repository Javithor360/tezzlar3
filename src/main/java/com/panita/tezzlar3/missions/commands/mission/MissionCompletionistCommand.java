package com.panita.tezzlar3.missions.commands.mission;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.config.CustomConfig;
import com.panita.tezzlar3.missions.MissionsModule;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SubCommandSpec(
        parent = "mission",
        name = "completionist",
        description = "Lists all players who have completed every available mission.",
        syntax = "/mission completionist",
        permission = "tezzlar.command.mission.completionist"
)
public class MissionCompletionistCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Retrieve all currently loaded mission IDs
        Set<String> allMissionIds = MissionsModule.getMissionManager().getLoadedMissions().keySet();

        if (allMissionIds.isEmpty()) {
            Messenger.prefixedSend(sender, "&cNo hay misiones cargadas actualmente.");
            return;
        }

        File dataFolder = new File(Tezzlar.getInstance().getDataFolder(), "data");
        if (!dataFolder.exists()) {
            Messenger.prefixedSend(sender, "&cNo hay datos de jugadores.");
            return;
        }

        // List all individual player data files, excluding the global missions file
        File[] files = dataFolder.listFiles(
                (dir, name) -> name.endsWith(".yml") && !name.equals("global_missions.yml")
        );

        if (files == null || files.length == 0) {
            Messenger.prefixedSend(sender, "&cNo hay datos de jugadores.");
            return;
        }

        List<String> completionists = new ArrayList<>();

        for (File file : files) {
            String playerName = file.getName().replace(".yml", "");
            CustomConfig customConfig = new CustomConfig(Tezzlar.getInstance(), "data", file.getName());
            FileConfiguration config = customConfig.getConfig();

            List<String> completedMissions = config.getStringList("completed_missions");

            // A player is a completionist if they have completed every loaded mission
            if (completedMissions.containsAll(allMissionIds)) {
                completionists.add(playerName);
            }
        }

        int totalMissions = allMissionIds.size();
        int completionistCount = completionists.size();
        int totalPlayers = files.length;

        Messenger.send(sender, "&8----------------------------------------");
        Messenger.send(sender, "&6&lCompletionistas &7(" + completionistCount + "/" + totalPlayers + " jugadores)");
        Messenger.send(sender, "&7Misiones totales: &e" + totalMissions);
        Messenger.send(sender, "");

        if (completionists.isEmpty()) {
            Messenger.send(sender, "  &8(Ningún jugador ha completado todas las misiones)");
        } else {
            Messenger.send(sender, "  &b" + String.join(", ", completionists));
        }

        Messenger.send(sender, "&8----------------------------------------");
    }
}

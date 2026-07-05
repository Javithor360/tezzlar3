package com.panita.tezzlar3.missions.commands;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.config.CustomConfig;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.Mission;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SubCommandSpec(
        parent = "mission",
        name = "check",
        description = "Revisa el estado y jugadores de una misión.",
        syntax = "/mission check <mission_id> <completed|uncompleted>",
        permission = "tezzlar.command.mission.check"
)
public class MissionCheckCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!com.panita.tezzlar3.core.util.CommandUtils.checkArgsOrUsage(sender, args, 2, this.getClass())) return;

        String missionId = args[0];
        String filterType = args[1].toLowerCase();

        if (!filterType.equals("completed") && !filterType.equals("uncompleted")) {
            Messenger.prefixedSend(sender, "&cUsa 'completed' o 'uncompleted'.");
            return;
        }

        Mission mission = MissionsModule.getMissionManager().getMission(missionId);
        if (mission == null) {
            Messenger.prefixedSend(sender, "&cLa misión '" + missionId + "' no existe.");
            return;
        }

        File dataFolder = new File(Tezzlar.getInstance().getDataFolder(), "data");
        if (!dataFolder.exists()) {
            Messenger.prefixedSend(sender, "&cNo hay datos de jugadores.");
            return;
        }

        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml") && !name.equals("global_missions.yml"));
        if (files == null) {
            Messenger.prefixedSend(sender, "&cNo hay datos de jugadores.");
            return;
        }

        List<String> completedPlayers = new ArrayList<>();
        List<String> uncompletedPlayers = new ArrayList<>();

        for (File file : files) {
            String playerName = file.getName().replace(".yml", "");
            CustomConfig customConfig = new CustomConfig(Tezzlar.getInstance(), "data", file.getName());
            FileConfiguration config = customConfig.getConfig();

            List<String> completedMissions = config.getStringList("completed_missions");
            if (completedMissions.contains(missionId)) {
                completedPlayers.add(playerName);
            } else {
                uncompletedPlayers.add(playerName);
            }
        }

        int totalPlayers = files.length;
        int currentDay = TimeManager.getCurrentDay();
        boolean isActive = currentDay >= mission.getStartDay() && currentDay <= mission.getEndDay();
        String status = isActive ? "&aActiva" : "&cExpirada";

        int rewardsCount = mission.getRewards() != null ? mission.getRewards().size() : 0;
        int punishmentsCount = mission.getPunishments() != null ? mission.getPunishments().size() : 0;

        boolean showCompleted = filterType.equals("completed");
        List<String> targetList = showCompleted ? completedPlayers : uncompletedPlayers;
        int count = targetList.size();

        String noKeyword = showCompleted ? "" : "no ";

        Messenger.send(sender, "&8----------------------------------------");
        Messenger.send(sender, "&e" + count + "/" + totalPlayers + " &fjugadores " + noKeyword + "han completado la misión:");
        Messenger.send(sender, "&7Título: &f" + mission.getName());
        Messenger.send(sender, "&7Estado: " + status);
        Messenger.send(sender, "&7Recompensas: &a" + rewardsCount);
        Messenger.send(sender, "&7Castigos: &c" + punishmentsCount);
        
        if (targetList.isEmpty()) {
            Messenger.send(sender, "&7Lista Solicitada: &8(Vacía)");
        } else {
            Messenger.send(sender, "&7Lista Solicitada:");
            Messenger.send(sender, "  &b" + String.join(", ", targetList));
        }
        Messenger.send(sender, "&8----------------------------------------");
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> {
            if (MissionsModule.getMissionManager() == null) return List.of();
            return new ArrayList<>(MissionsModule.getMissionManager().getLoadedMissions().keySet());
        });
        meta.setArgumentSuggestion(1, context -> List.of("completed", "uncompleted"));
    }
}

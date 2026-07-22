package com.panita.tezzlar3.missions.commands;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.config.CustomConfig;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.Mission;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandSpec(
        name = "minedlist",
        aliases = { "lista" },
        description = "Shows progress for MINE_BLOCKS missions.",
        syntax = "/minedlist [mission] [player]"
)
public class MinedListCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        String targetName = null;
        Player targetPlayer = null;

        if (args.length >= 2) {
            targetName = args[1];
            targetPlayer = Bukkit.getPlayer(targetName);
        } else {
            if (!(sender instanceof Player)) {
                Messenger.prefixedSend(sender, "&cDebes ser un jugador o especificar a uno.");
                return;
            }
            targetPlayer = (Player) sender;
            targetName = targetPlayer.getName();
        }

        PlayerMissionData data = null;
        CustomConfig offlineConfig = null;

        if (targetPlayer != null && targetPlayer.isOnline()) {
            data = MissionsModule.getDataManager().getPlayerData(targetPlayer);
            if (data == null) {
                Messenger.prefixedSend(sender, "&cNo se pudo cargar la información del jugador online.");
                return;
            }
        } else {
            if (!CustomConfig.exists(Tezzlar.getInstance(), "data", targetName + ".yml")) {
                Messenger.prefixedSend(sender, "&cNo hay registros de misiones para el jugador: &e" + targetName);
                return;
            }
            offlineConfig = new CustomConfig(Tezzlar.getInstance(), "data", targetName + ".yml");
        }

        Mission targetMission = null;
        if (args.length >= 1) {
            targetMission = MissionsModule.getMissionManager().getMission(args[0]);
            if (targetMission == null) {
                Messenger.prefixedSend(sender, "&cMisión no encontrada: " + args[0]);
                return;
            }
        } else {
            int currentDay = TimeManager.getCurrentDay();
            for (Mission mission : MissionsModule.getMissionManager().getLoadedMissions().values()) {
                if (currentDay >= mission.getStartDay() && currentDay <= mission.getEndDay() && (mission.getObjectiveType().equalsIgnoreCase("MINE_BLOCKS") || mission.getObjectiveType().equalsIgnoreCase("KILL_SHULKER_COLORS"))) {
                    boolean completed = false;
                    if (data != null) {
                        completed = data.hasCompleted(mission.getId());
                    } else if (offlineConfig != null) {
                        List<String> comp = offlineConfig.getConfig().getStringList("completed_missions");
                        completed = comp.contains(mission.getId());
                    }
                    if (!completed) {
                        targetMission = mission;
                        break;
                    }
                }
            }
            if (targetMission == null) {
                Messenger.prefixedSend(sender, "&cNo hay ninguna misión activa con lista de objetivos en este momento.");
                return;
            }
        }

        if ((!targetMission.getObjectiveType().equalsIgnoreCase("MINE_BLOCKS") && !targetMission.getObjectiveType().equalsIgnoreCase("KILL_SHULKER_COLORS")) || targetMission.getObjectiveTargetsMap() == null) {
            Messenger.prefixedSend(sender, "&cLa misión &e" + targetMission.getId() + " &cno tiene una lista de objetivos válida.");
            return;
        }

        int maxProgress = 0;
        int currentProgress = 0;

        Messenger.prefixedSend(sender, "&bProgreso de Misión: &e" + targetMission.getName());
        Messenger.prefixedSend(sender, "&7Tipo:");

        for (Map.Entry<String, Integer> entry : targetMission.getObjectiveTargetsMap().entrySet()) {
            String category = entry.getKey().split(",")[0];
            int req = entry.getValue();
            
            int prog = 0;
            String subKey = targetMission.getId() + "_sub_" + entry.getKey();
            
            if (data != null) {
                prog = data.getProgress(subKey);
            } else if (offlineConfig != null) {
                prog = offlineConfig.getConfig().getInt("active_progress." + subKey, 0);
            }
            
            maxProgress += req;
            currentProgress += Math.min(prog, req);

            if (targetMission.getObjectiveType().equalsIgnoreCase("KILL_SHULKER_COLORS")) {
                String symbol = prog >= req ? "&a✔" : "&c✘";
                Messenger.prefixedSend(sender, "&8- " + symbol + " &f" + category);
            } else {
                Messenger.prefixedSend(sender, "&8- &a" + category + "&8: &e" + prog + "&7/&e" + req);
            }
        }

        if (maxProgress > 0) {
            int percentage = (int) (((float) currentProgress / maxProgress) * 100);
            Messenger.prefixedSend(sender, "&aProgreso total: &e" + percentage + "%");
        }
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> {
            List<String> list = new ArrayList<>();
            for (Mission mission : MissionsModule.getMissionManager().getLoadedMissions().values()) {
                if (mission.getObjectiveType().equalsIgnoreCase("MINE_BLOCKS") || mission.getObjectiveType().equalsIgnoreCase("KILL_SHULKER_COLORS")) {
                    list.add(mission.getId());
                }
            }
            return list;
        });

        meta.setArgumentSuggestion(1, context -> {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        });
    }
}

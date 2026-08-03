package com.panita.tezzlar3.qol.commands;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

@CommandSpec(
        name = "trackstat",
        aliases = {"topstat", "statstop"},
        description = "Shows the top 10 players for a given statistic.",
        syntax = "/trackstat <statistic> [material/entity]",
        permission = "tezzlar.command.qol.trackstat"
)
public class TrackStatCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) {
            Messenger.prefixedSend(sender, "&7Uso: &b/trackstat <statistic> [material/entity]");
            return;
        }

        Statistic stat;
        try {
            stat = Statistic.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            Messenger.prefixedSend(sender, "&cEstadística inválida.");
            return;
        }

        Material material = null;
        EntityType entityType = null;

        if (stat.getType() == Statistic.Type.BLOCK || stat.getType() == Statistic.Type.ITEM) {
            if (args.length < 2) {
                Messenger.prefixedSend(sender, "&cEsta estadística requiere un material.");
                return;
            }
            try {
                material = Material.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                Messenger.prefixedSend(sender, "&cMaterial inválido.");
                return;
            }
        } else if (stat.getType() == Statistic.Type.ENTITY) {
            if (args.length < 2) {
                Messenger.prefixedSend(sender, "&cEsta estadística requiere una entidad.");
                return;
            }
            try {
                entityType = EntityType.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                Messenger.prefixedSend(sender, "&cEntidad inválida.");
                return;
            }
        }

        Messenger.prefixedSend(sender, "&7Calculando el top 10 para &b" + stat.name() + (args.length > 1 ? " " + args[1].toUpperCase() : "") + "&7...");

        final Material finalMat = material;
        final EntityType finalEnt = entityType;

        Bukkit.getScheduler().runTaskAsynchronously(Tezzlar.getInstance(), () -> {
            Map<String, Integer> stats = new HashMap<>();

            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (player.getName() == null) continue;

                int value = 0;
                try {
                    if (stat.getType() == Statistic.Type.UNTYPED) {
                        value = player.getStatistic(stat);
                    } else if (stat.getType() == Statistic.Type.BLOCK || stat.getType() == Statistic.Type.ITEM) {
                        value = player.getStatistic(stat, finalMat);
                    } else if (stat.getType() == Statistic.Type.ENTITY) {
                        value = player.getStatistic(stat, finalEnt);
                    }
                } catch (IllegalArgumentException | NoSuchMethodError e) {
                    continue;
                }

                if (value > 0) {
                    stats.put(player.getName(), value);
                }
            }

            List<Map.Entry<String, Integer>> top = stats.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .toList();

            Bukkit.getScheduler().runTask(Tezzlar.getInstance(), () -> {
                Messenger.send(sender, "");
                Messenger.send(sender, "&e&lTOP 10 &7- &b" + stat.name() + (args.length > 1 ? " " + args[1].toUpperCase() : ""));
                if (top.isEmpty()) {
                    Messenger.send(sender, " &cNo hay datos para mostrar.");
                } else {
                    int rank = 1;
                    for (Map.Entry<String, Integer> entry : top) {
                        String formattedValue = formatValue(stat, entry.getValue());
                        Messenger.send(sender, " &e" + rank + ". &a" + entry.getKey() + " &7- &f" + formattedValue);
                        rank++;
                    }
                }
                Messenger.send(sender, "");
            });
        });
    }

    private String formatValue(Statistic stat, int value) {
        if (stat == Statistic.PLAY_ONE_MINUTE || stat == Statistic.TIME_SINCE_REST) {
            long ticks = value;
            long hours = ticks / 72000;
            long minutes = (ticks % 72000) / 1200;
            if (hours > 0) {
                return hours + "h " + minutes + "m";
            } else {
                return minutes + "m";
            }
        }
        return String.format("%,d", value);
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> Arrays.stream(Statistic.values())
                .map(Enum::name)
                .collect(Collectors.toList()));
        
        meta.setArgumentSuggestion(1, context -> {
            String[] args = context.getArgs();
            if (args.length >= 1) {
                try {
                    Statistic stat = Statistic.valueOf(args[0].toUpperCase());
                    if (stat.getType() == Statistic.Type.BLOCK || stat.getType() == Statistic.Type.ITEM) {
                        return Arrays.stream(Material.values())
                                .filter(m -> stat.getType() == Statistic.Type.BLOCK ? m.isBlock() : m.isItem())
                                .map(Enum::name)
                                .collect(Collectors.toList());
                    } else if (stat.getType() == Statistic.Type.ENTITY) {
                        return Arrays.stream(EntityType.values())
                                .map(Enum::name)
                                .collect(Collectors.toList());
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
            return Collections.emptyList();
        });
    }
}

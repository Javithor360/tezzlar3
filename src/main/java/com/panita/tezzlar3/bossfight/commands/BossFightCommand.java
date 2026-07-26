package com.panita.tezzlar3.bossfight.commands;

import com.panita.tezzlar3.bossfight.util.BossManager;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandSpec(
        name = "bossfight",
        description = "Manage the Day 31 Boss Fight.",
        syntax = "/bossfight <start|phase|attack|stop>",
        permission = "tezzlar.bossfight.admin",
        playerOnly = true
)
public class BossFightCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        BossManager manager = BossManager.getInstance();

        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start":
                if (manager.getBoss() != null) {
                    Messenger.prefixedSend(player, "&cYa hay una batalla de jefe activa. Usa /bossfight stop primero.");
                    return;
                }
                manager.startFight(player);
                Messenger.prefixedSend(player, "&aHas iniciado la batalla de Jefe (Fase 1).");
                break;
                
            case "stop":
                if (manager.getBoss() == null) {
                    Messenger.prefixedSend(player, "&cNo hay ninguna batalla de jefe activa.");
                    return;
                }
                manager.stopFight();
                Messenger.prefixedSend(player, "&cLa batalla de jefe ha sido detenida.");
                break;
                
            case "phase":
                if (manager.getBoss() == null) {
                    Messenger.prefixedSend(player, "&cNo hay ninguna batalla de jefe activa.");
                    return;
                }
                if (args.length < 2) {
                    Messenger.prefixedSend(player, "&cUso correcto: /bossfight phase <1|2|3|4>");
                    return;
                }
                try {
                    int phase = Integer.parseInt(args[1]);
                    if (phase < 1 || phase > 4) {
                        Messenger.prefixedSend(player, "&cLa fase debe estar entre 1 y 4.");
                        return;
                    }
                    manager.setPhase(phase);
                    Messenger.prefixedSend(player, "&aFase cambiada a " + phase + ".");
                } catch (NumberFormatException e) {
                    Messenger.prefixedSend(player, "&cNúmero de fase inválido.");
                }
                break;
                
            case "attack":
                if (manager.getBoss() == null) {
                    Messenger.prefixedSend(player, "&cNo hay ninguna batalla de jefe activa.");
                    return;
                }
                if (args.length < 2) {
                    Messenger.prefixedSend(player, "&cUso correcto: /bossfight attack <nombre>");
                    return;
                }
                // TODO: Implement attack execution by name
                Messenger.prefixedSend(player, "&eEl sistema de ataques vía comando aún está en desarrollo.");
                break;
                
            default:
                Messenger.prefixedSend(player, "&cSubcomando desconocido.");
                break;
        }
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> {
            String current = context.getCurrentArg().toLowerCase();
            return Stream.of("start", "stop", "phase", "attack")
                    .filter(s -> s.startsWith(current))
                    .collect(Collectors.toList());
        });
        
        meta.setArgumentSuggestion(1, context -> {
            String[] args = context.getArgs();
            if (args.length < 2) return Collections.emptyList();
            
            String previous = args[args.length - 2].toLowerCase();
            String current = context.getCurrentArg().toLowerCase();
            
            if (previous.equals("phase")) {
                return Stream.of("1", "2", "3", "4")
                        .filter(s -> s.startsWith(current))
                        .collect(Collectors.toList());
            } else if (previous.equals("attack")) {
                // TODO: Return attack names later
                return Collections.emptyList();
            }
            return Collections.emptyList();
        });
    }
}

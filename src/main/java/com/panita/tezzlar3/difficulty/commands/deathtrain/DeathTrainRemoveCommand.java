package com.panita.tezzlar3.difficulty.commands.deathtrain;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.difficulty.mechanics.DeathTrainMechanic;
import org.bukkit.command.CommandSender;

import java.util.List;

@SubCommandSpec(
        parent = "deathtrain",
        name = "remove",
        description = "Removes time from the active death train.",
        syntax = "/deathtrain remove <time>",
        permission = "tezzlar.command.deathtrain.remove"
)
public class DeathTrainRemoveCommand implements AdvancedCommand, TabSuggestingCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!com.panita.tezzlar3.core.util.CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        if (DeathTrainMechanic.getInstance() != null) {
            int currentSeconds = DeathTrainMechanic.getInstance().getRemainingSeconds();
            if (currentSeconds <= 0) {
                Messenger.prefixedSend(sender, "&cNo hay un DeathTrain activo actualmente.");
                return;
            }

            long millisToRemove = Global.parseDurationToMillis(args[0]);
            if (millisToRemove <= 0) {
                Messenger.prefixedSend(sender, "&cFormato de tiempo inválido. Usa 1h, 30m, 1d, etc.");
                return;
            }

            int secondsToRemove = (int) (millisToRemove / 1000);
            DeathTrainMechanic.getInstance().setRemainingSeconds(currentSeconds - secondsToRemove);
            
            String timeStr = Global.formatTimeTicks(DeathTrainMechanic.getInstance().getRemainingSeconds() * 20L);
            Messenger.prefixedSend(sender, "&aDeathTrain actualizado. Tiempo total restante: &e" + timeStr);
        } else {
            Messenger.prefixedSend(sender, "&cLa mecánica de DeathTrain no está inicializada.");
        }
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> List.of("10m", "30m", "1h", "2h", "1d"));
    }
}

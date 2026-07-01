package com.panita.tezzlar3.difficulty.commands.deathtrain;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.difficulty.mechanics.DeathTrainMechanic;
import org.bukkit.command.CommandSender;

@SubCommandSpec(
        parent = "tezzlar deathtrain",
        name = "start",
        description = "Starts or extends the time for a death train event",
        syntax = "/tezzlar deathtrain start <time>",
        permission = "tezzlar.command.deathtrain.start"
)
public class DeathTrainStartCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        long parsedMillis = Global.parseDurationToMillis(args[0]);
        if (parsedMillis <= 0) {
            Messenger.prefixedSend(sender, "&cFormato de tiempo inválido. Usa formato como 1d12h30m.");
            return;
        }

        int secondsToAdd = (int) (parsedMillis / 1000);

        if (DeathTrainMechanic.getInstance() != null) {
            int currentSeconds = DeathTrainMechanic.getInstance().getRemainingSeconds();
            DeathTrainMechanic.getInstance().setRemainingSeconds(currentSeconds + secondsToAdd);
            
            String timeStr = Global.formatTimeTicks(DeathTrainMechanic.getInstance().getRemainingSeconds() * 20L);
            Messenger.prefixedSend(sender, "&aDeathTrain actualizado. Tiempo total restante: &e" + timeStr);
        } else {
            Messenger.prefixedSend(sender, "&cLa mecánica de DeathTrain no está inicializada.");
        }
    }
}

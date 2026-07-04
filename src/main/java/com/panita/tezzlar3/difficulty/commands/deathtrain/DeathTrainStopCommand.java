package com.panita.tezzlar3.difficulty.commands.deathtrain;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.difficulty.mechanics.DeathTrainMechanic;
import org.bukkit.command.CommandSender;

@SubCommandSpec(
        parent = "deathtrain",
        name = "stop",
        description = "Stops the current death train event",
        syntax = "/deathtrain stop",
        permission = "tezzlar.command.deathtrain.stop"
)
public class DeathTrainStopCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (DeathTrainMechanic.getInstance() != null) {
            if (DeathTrainMechanic.getInstance().getRemainingSeconds() <= 0) {
                Messenger.prefixedSend(sender, "&cNo hay un DeathTrain activo actualmente.");
                return;
            }
            
            DeathTrainMechanic.getInstance().setRemainingSeconds(0);
            Messenger.prefixedSend(sender, "&aDeathTrain detenido exitosamente.");
        } else {
            Messenger.prefixedSend(sender, "&cLa mecánica de DeathTrain no está inicializada.");
        }
    }
}

package com.panita.tezzlar3.difficulty.commands.deathtrain;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.difficulty.mechanics.DeathTrainMechanic;
import org.bukkit.command.CommandSender;

import java.util.List;

@SubCommandSpec(
        parent = "deathtrain",
        name = "status",
        description = "Muestra el estado actual del DeathTrain.",
        syntax = "/deathtrain status",
        permission = "tezzlar.command.deathtrain.status"
)
public class DeathTrainStatusCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (DeathTrainMechanic.getInstance() != null) {
            int remainingSeconds = DeathTrainMechanic.getInstance().getRemainingSeconds();
            
            if (remainingSeconds <= 0) {
                Messenger.prefixedSend(sender, "&eEl DeathTrain no está activo actualmente.");
                return;
            }
            
            String timeStr = Global.formatTimeTicks(remainingSeconds * 20L);
            Messenger.prefixedSend(sender, "&c&lEstado del DeathTrain:");
            Messenger.send(sender, "&7- &fTiempo restante: &e" + timeStr);
            
            List<String> deadPlayers = DeathTrainMechanic.getInstance().getDeadPlayers();
            if (deadPlayers.isEmpty()) {
                Messenger.send(sender, "&7- &fJugadores caídos: &aNinguno (por ahora)");
            } else {
                Messenger.send(sender, "&7- &fJugadores caídos (&c" + deadPlayers.size() + "&f):");
                Messenger.send(sender, "  &c" + String.join(", ", deadPlayers));
            }
        } else {
            Messenger.prefixedSend(sender, "&cLa mecánica de DeathTrain no está inicializada.");
        }
    }
}

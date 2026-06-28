package com.panita.tezzlar3.minievents.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.minievents.MiniEventManager;
import com.panita.tezzlar3.minievents.MiniEventsModule;
import org.bukkit.command.CommandSender;

@SubCommandSpec(
        parent = "minievent",
        name = "stop",
        description = "Stops the currently active Mini Event.",
        syntax = "/minievent stop",
        permission = "tezzlar.command.minievent.stop"
)
public class MiniEventStopSubCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        MiniEventManager manager = MiniEventsModule.getManager();
        if (manager == null) {
            Messenger.prefixedSend(sender, "<red>El módulo de Mini Eventos no está activo.</red>");
            return;
        }

        if (manager.getActiveEvent() == null) {
            Messenger.prefixedSend(sender, "<red>No hay ningún evento activo para detener.</red>");
            return;
        }

        Messenger.prefixedSend(sender, "<green>Deteniendo el Mini Evento actual...</green>");
        manager.stopActiveEvent();
    }
}

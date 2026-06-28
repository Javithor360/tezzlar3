package com.panita.tezzlar3.minievents.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.minievents.MiniEvent;
import com.panita.tezzlar3.minievents.MiniEventManager;
import com.panita.tezzlar3.minievents.MiniEventsModule;
import com.panita.tezzlar3.minievents.gui.MiniEventSummonMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@SubCommandSpec(
        parent = "minievent",
        name = "start",
        description = "Starts a mini event or opens GUI if no ID provided.",
        syntax = "/minievent start [id]",
        permission = "tezzlar.command.minievent.start"
)
public class MiniEventStartSubCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        MiniEventManager manager = MiniEventsModule.getManager();
        if (manager == null) {
            Messenger.prefixedSend(sender, "<red>El módulo de Mini Eventos no está activo.</red>");
            return;
        }

        if (args.length == 0) {
            if (sender instanceof Player player) {
                new MiniEventSummonMenu(player).open();
            } else {
                CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass());
            }
            return;
        }

        String eventId = args[0].toLowerCase();
        MiniEvent targetEvent = manager.getEventById(eventId);

        if (targetEvent == null) {
            Messenger.prefixedSend(sender, "<red>Evento no encontrado. Usa TAB para ver la lista.</red>");
            return;
        }

        if (manager.getActiveEvent() != null) {
            Messenger.prefixedSend(sender, "<red>¡Ya hay un evento en curso!</red>");
            return;
        }

        Messenger.prefixedSend(sender, "<green>Iniciando Mini Evento forzadamente...</green>");
        manager.forceRoulette(targetEvent);
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> {
            MiniEventManager manager = MiniEventsModule.getManager();
            if (manager == null) return List.of();
            return manager.getRegisteredEvents().stream()
                    .map(MiniEvent::getId)
                    .filter(id -> id.toLowerCase().startsWith(context.getCurrentArg().toLowerCase()))
                    .collect(Collectors.toList());
        });
    }
}

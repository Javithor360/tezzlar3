package com.panita.tezzlar3.timeline.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.timeline.events.DayChangeEvent;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@SubCommandSpec(
        parent = "tezzlar day",
        name = "set",
        description = "Sets the global day of the server.",
        syntax = "/tezzlar day set <number>",
        permission = "tezzlar.command.day.set"
)
public class DaySetCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        try {
            int newDay = Integer.parseInt(args[0]);
            if (newDay < 0) {
                Messenger.prefixedSend(sender, "&cEl día no puede ser un número negativo.");
                return;
            }

            int oldDay = TimeManager.getCurrentDay();

            TimeManager.setCurrentDay(newDay);
            Messenger.prefixedSend(sender, "&aEl día global ha sido actualizado a: &e" + newDay);

            DayChangeEvent event = new DayChangeEvent(oldDay, newDay);
            Bukkit.getPluginManager().callEvent(event);

        } catch (NumberFormatException e) {
            Messenger.prefixedSend(sender, "&cPor favor, introduce un número entero válido.");
        }
    }
}

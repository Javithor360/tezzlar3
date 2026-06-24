package com.panita.tezzlar3.timeline.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.timeline.util.TimeManager;
import com.panita.tezzlar3.core.util.Global;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@SubCommandSpec(
        parent = "tezzlar day",
        name = "get",
        description = "Shows the current global day of the server.",
        syntax = "/tezzlar day get",
        permission = "tezzlar.command.day.get"
)
public class DayGetCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        int currentDay = TimeManager.getCurrentDay();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Mexico_City"));
        String timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        ZonedDateTime next2PM = now.withHour(14).withMinute(0).withSecond(0).withNano(0);
        if (!now.isBefore(next2PM)) {
            next2PM = next2PM.plusDays(1);
        }
        
        long millisRemaining = Duration.between(now, next2PM).toMillis();
        String remainingStr = Global.formatDuration(millisRemaining);
        
        Messenger.prefixedSend(sender, "&aEl día global actual es: &e" + currentDay);
        Messenger.prefixedSend(sender, "&aHora real (UTC-6): &e" + timeStr);
        Messenger.prefixedSend(sender, "&aFaltan &e" + remainingStr + " &apara el Día " + (currentDay + 1));
    }
}

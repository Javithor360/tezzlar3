package com.panita.tezzlar3.hardcore.commands.hardcore.player;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;

@SubCommandSpec(
        parent = "tezzlar hardcore player",
        name = "kill",
        description = "Aplica un baneo preventivo a un jugador sin sumarle muertes.",
        syntax = "/hardcore player kill <jugador> [horas]",
        permission = "tezzlar.command.hardcore.player.kill"
)
public class HardcorePlayerKillCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        String targetName = args[0];
        int hours = 24; // Default to 24 hours if not specified

        if (args.length >= 2) {
            try {
                hours = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Messenger.prefixedSend(sender, "&cLas horas deben ser un número válido.");
                return;
            }
            if (hours <= 0) {
                Messenger.prefixedSend(sender, "&cLas horas deben ser mayores a 0.");
                return;
            }
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!Global.isValidPlayer(sender, target, targetName)) {
            return;
        }

        long durationMillis = hours * 3600000L;
        Instant expirationInstant = Instant.now().plusMillis(durationMillis);

        HardcoreDataManager.setBanExpiration(target.getUniqueId(), target.getName(), expirationInstant.toEpochMilli());

        if (target.isOnline()) {
            Player onlinePlayer = target.getPlayer();
            if (onlinePlayer != null) {
                String hStr = hours == 1 ? " hora" : " horas";
                onlinePlayer.kick(Messenger.mini("<red>Has sido ejecutado preventivamente por un administrador durante " + hours + hStr + ".</red>"));
            }
        }

        String hStr = hours == 1 ? " hora" : " horas";
        Messenger.prefixedSend(sender, "&aSe ha aplicado un ban de muerte a &e" + target.getName() + "&a durante &e" + hours + hStr + "&a sin quitarle vidas.");
    }
}

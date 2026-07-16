package com.panita.tezzlar3.difficulty.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.difficulty.listeners.MobDebugListener;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandSpec(
        name = "mobdebug",
        description = "Toggles mob debug mode",
        syntax = "/mobdebug",
        permission = "tezzlar.command.mobdebug",
        playerOnly = true
)
public class MobDebugCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        boolean enabled = MobDebugListener.toggleDebug(player.getUniqueId());
        if (enabled) {
            Messenger.prefixedSend(player, "&aModo &eMobDebug &aactivado. Golpea a un mob para ver sus estadísticas.");
        } else {
            Messenger.prefixedSend(player, "&cModo &eMobDebug &adesactivado.");
        }
    }
}

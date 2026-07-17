package com.panita.tezzlar3.missions.commands.ctm;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.missions.listeners.CTMEscortListener;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SubCommandSpec(
        parent = "ctm",
        name = "bypass",
        description = "Toggles byass mode for CTM wools",
        syntax = "/ctm bypass",
        permission = "tezzlar.command.ctm.bypass",
        playerOnly = true
)
public class CTMBypassCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        
        if (CTMEscortListener.bypassedPlayers.contains(player.getUniqueId())) {
            CTMEscortListener.bypassedPlayers.remove(player.getUniqueId());
            Messenger.prefixedSend(player, "&cModo Bypass de CTM desactivado. Las lanas volverán a aplicarte las reglas y penalizaciones de la misión.");
        } else {
            CTMEscortListener.bypassedPlayers.add(player.getUniqueId());
            Messenger.prefixedSend(player, "&aModo Bypass de CTM activado. Ya puedes manipular, ocultar y soltar lanas libremente sin desencadenar las alertas o fallar la escolta.");
        }
    }
}

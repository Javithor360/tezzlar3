package com.panita.tezzlar3.qol.commands.coordinates;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.qol.util.CoordinatesManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SubCommandSpec(
        parent = "tezzlar coordinates",
        name = "save",
        description = "Save your current location as a named site.",
        syntax = "/pc coordinates save <site_name>",
        permission = "tezzlar.command.coordinates.save",
        playerOnly = true
)
public class CoordinatesSave implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        Player player = (Player) sender;
        String siteName = args[0];
        Location loc = player.getLocation();

        if (CoordinatesManager.saveLocation(siteName, loc)) {
            Messenger.prefixedSend(player, "&aLocation &e" + siteName + " &asaved successfully.");
        } else {
            Messenger.prefixedSend(player, "&cA site with the name &e" + siteName + " &calready exists.");
        }
    }
}

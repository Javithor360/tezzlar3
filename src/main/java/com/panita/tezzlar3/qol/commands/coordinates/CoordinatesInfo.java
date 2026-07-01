package com.panita.tezzlar3.qol.commands.coordinates;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.qol.util.CoordinatesManager;
import org.bukkit.command.CommandSender;

import java.util.Set;

@SubCommandSpec(
        parent = "tezzlar coordinates",
        name = "info",
        description = "View information about saved coordinates.",
        syntax = "/pc coordinates info [site_name]",
        permission = "tezzlar.command.coordinates.info",
        playerOnly = false
)
public class CoordinatesInfo implements AdvancedCommand, TabSuggestingCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length >= 1) {
            // Show details for a specific site
            String siteName = args[0];
            if (!CoordinatesManager.getAllNames().contains(siteName)) {
                Messenger.prefixedSend(sender, "&cNo location found with the name &e" + siteName);
                return;
            }

            var loc = CoordinatesManager.getLocation(siteName);
            Messenger.prefixedSend(sender, "&aLocation &e" + siteName + "&a:");
            Messenger.prefixedSend(sender, "  World: &e" + loc.getWorld().getName());
            Messenger.prefixedSend(sender, "  X: &e" + loc.getX() + " Y: &e" + loc.getY() + " Z: &e" + loc.getZ());
            Messenger.prefixedSend(sender, "  Yaw: &e" + loc.getYaw() + " &fPitch: &e" + loc.getPitch());
        } else {
            // Display all saved sites
            Set<String> sites = CoordinatesManager.getAllNames();
            if (sites.isEmpty()) {
                Messenger.prefixedSend(sender, "&cNo saved coordinates.");
                return;
            }

            Messenger.prefixedSend(sender, "&aSaved locations:");
            String list = String.join(", ", sites);
            Messenger.prefixedSend(sender, "&e" + list);
        }
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> CoordinatesManager.getAllNames().stream()
                .filter(name -> name.toLowerCase().startsWith(context.getCurrentArg().toLowerCase()))
                .toList());
    }
}

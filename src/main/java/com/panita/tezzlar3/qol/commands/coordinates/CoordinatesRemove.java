package com.panita.tezzlar3.qol.commands.coordinates;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.qol.util.CoordinatesManager;
import org.bukkit.command.CommandSender;

@SubCommandSpec(
        parent = "tezzlar coordinates",
        name = "remove",
        description = "Remove a saved location by name.",
        syntax = "/pc coordinates remove <site_name>",
        permission = "tezzlar.command.coordinates.remove",
        playerOnly = false
)
public class CoordinatesRemove implements AdvancedCommand, TabSuggestingCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        String siteName = args[0];

        if (CoordinatesManager.removeLocation(siteName)) {
            Messenger.prefixedSend(sender, "&aLocation &e" + siteName + " &ahas been removed.");
        } else {
            Messenger.prefixedSend(sender, "&cNo location found with the name &e" + siteName);
        }
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> CoordinatesManager.getAllNames().stream()
                .filter(name -> name.toLowerCase().startsWith(context.getCurrentArg().toLowerCase()))
                .toList());
    }
}

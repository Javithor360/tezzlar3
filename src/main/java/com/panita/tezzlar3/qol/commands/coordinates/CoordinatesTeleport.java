package com.panita.tezzlar3.qol.commands.coordinates;


import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.qol.util.CoordinatesManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SubCommandSpec(
        parent = "Tezzlar coordinates",
        name = "teleport",
        description = "Teleport to a saved site.",
        syntax = "/pc coordinates teleport <site_name> [player]",
        permission = "Tezzlar.command.coordinates.teleport",
        playerOnly = false
)
public class CoordinatesTeleport implements AdvancedCommand, TabSuggestingCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        String siteName = args[0];
        Player target;

        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                Messenger.prefixedSend(sender, "&cPlayer &e" + args[1] + " &cnot found.");
                return;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            Messenger.prefixedSend(sender, "&cYou must specify a player when executing from console.");
            return;
        }

        Location loc = CoordinatesManager.getLocation(siteName);
        if (loc == null) {
            Messenger.prefixedSend(sender, "&cNo location found with the name &e" + siteName);
            return;
        }

        target.teleport(loc);
        Messenger.prefixedSend(sender, "&aTeleported &e" + target.getName() + " &ato location &e" + siteName);
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> CoordinatesManager.getAllNames().stream()
                .filter(name -> name.toLowerCase().startsWith(context.getCurrentArg().toLowerCase()))
                .toList());

        meta.setArgumentSuggestion(1, context -> Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(context.getCurrentArg().toLowerCase()))
                .toList());
    }
}

package com.panita.tezzlar3.core.commands.base;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.Global;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.stream.Collectors;

@SubCommandSpec(
        parent = "tezzlar",
        name = "help",
        description = "Displays help information for Tezzlar commands",
        syntax = "/tezzlar help [command]"
)
public class HelpCommand implements AdvancedCommand, TabSuggestingCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            // If no command name is provided, show general help
            Messenger.prefixedSend(sender, "<gold>Comandos disponibles:</gold>");
            for (Map.Entry<String, CommandMeta> entry : Global.ROOT_COMMANDS.entrySet()) {
                String root = entry.getKey();
                CommandMeta meta = entry.getValue();
                if (canSee(meta, sender)) {
                    showCommandHelp(sender, "/" + root, meta, "  ");
                }
            }
        } else {
            // If a command name is provided, show help for that specific command
            String rootName = args[0].toLowerCase();
            CommandMeta meta = Global.ROOT_COMMANDS.get(rootName);

            if (meta == null || !canSee(meta, sender)) {
                Messenger.prefixedSend(sender, "<red>Comando no encontrado:</red> <gray>" + args[0] + "</gray>");
                return;
            }

            Messenger.prefixedSend(sender, "<gold>Información sobre el comando /" + rootName + ":</gold>");
            showCommandHelp(sender, "/" + rootName, meta, "  ");
        }
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> Global.ROOT_COMMANDS.entrySet().stream()
                .filter(entry -> canSee(entry.getValue(), context.getSender()))
                .map(Map.Entry::getKey)
                .filter(name -> name.toLowerCase().startsWith(context.getCurrentArg().toLowerCase()))
                .collect(Collectors.toList()));
    }

    private void showCommandHelp(CommandSender sender, String path, CommandMeta meta, String indent) {
        if (meta.getCommand() != null || canSee(meta, sender)) {
            String syntax = meta.getSyntax() != null && !meta.getSyntax().isEmpty() ? meta.getSyntax() : path;
            String desc = meta.getDescription() != null ? meta.getDescription() : "(sin descripción)";

            Messenger.send(sender, indent + "<aqua>" + syntax + "</aqua> <gray>-</gray> <white>" + desc + "</white>");
        }

        meta.getSubCommands().forEach((name, subMeta) -> {
            if (canSee(subMeta, sender)) {
                showCommandHelp(sender, path + " " + name, subMeta, indent + "  ");
            }
        });
    }

    private boolean canSee(CommandMeta meta, CommandSender sender) {
        String perm = meta.getPermission();
        return perm == null || perm.isEmpty() || sender.hasPermission(perm);
    }
}


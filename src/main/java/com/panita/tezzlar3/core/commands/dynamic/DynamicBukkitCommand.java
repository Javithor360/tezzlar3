package com.panita.tezzlar3.core.commands.dynamic;

import com.mojang.brigadier.CommandDispatcher;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DynamicBukkitCommand extends Command implements TabCompleter, CommandExecutor {
    private final CommandMeta rootMeta; // The main command metadata
    private final CommandDispatcher<CommandSender> dispatcher = new CommandDispatcher<>();
    /**
     * Constructor for DynamicBukkitCommand.
     *
     * @param name The name of the command.
     * @param rootMeta The metadata for the main command.
     */
    public DynamicBukkitCommand(String name, CommandMeta rootMeta) {
        super(name);
        this.rootMeta = rootMeta;
    }

    /**
     * Executes the command with the given sender and arguments.
     *
     * @param sender The command sender (e.g., a player).
     * @param label The label of the command.
     * @param args The arguments passed to the command.
     * @return true if the command was executed successfully; false otherwise.
     */
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        return execute(sender, rootMeta, args);
    }

    /**
     * Recursive method to look for subcommands and execute the appropriate command.
     *
     * @param sender The command sender (e.g., a player).
     * @param currentMeta The current command metadata.
     * @param args The arguments passed to the command.
     * @return true if the command was executed successfully; false otherwise.
     */
    private boolean execute(CommandSender sender, CommandMeta currentMeta, String[] args) {
        // If there are no arguments, execute the main command
        if (args.length == 0) {
            return executeMetaCommand(sender, currentMeta, new String[0]);
        }

        // If there are arguments, check if they match any subcommands
        String nextArg = args[0].toLowerCase();
        Map<String, CommandMeta> subCommands = currentMeta.getSubCommands(); // Get the subcommands of the current meta

        if (subCommands.containsKey(nextArg)) {
            // Recursive call with the next subcommand and remaining arguments
            return execute(sender, subCommands.get(nextArg), Arrays.copyOfRange(args, 1, args.length));
        }

        // If the argument does not match any subcommands, check if the current command can be executed
        if (!subCommands.isEmpty()) {
            return sendErrorMessage(sender, "invalid_arguments");
        }

        // If the argument does not match any subcommands, execute the main command
        return executeMetaCommand(sender, currentMeta, args);
    }

    /**
     * Provides tab completion suggestions for the command.
     *
     * @param sender The command sender (e.g., a player).
     * @param label The label of the command.
     * @param args The arguments passed to the command.
     * @return A list of suggestions for tab completion.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>(); // List to hold suggestions
        CommandMeta currentMeta = rootMeta; // Start with the root command metadata
        int consumed = 0; // Number of arguments consumed

        // Iterate through the arguments and check for subcommands
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            if (currentMeta.getSubCommands().containsKey(arg)) { // Check if the argument matches a subcommand
                currentMeta = currentMeta.getSubCommands().get(arg); // Update the current meta to the subcommand
                consumed++;
            } else {
                break; // If the argument does not match a subcommand, break the loop
            }
        }

        // If there are no arguments, suggest the main subcommands
        if (consumed == args.length - 1 && !currentMeta.getSubCommands().isEmpty()) {
            String input = args[args.length - 1].toLowerCase();

            suggestions.addAll(
                    currentMeta.getSubCommands().entrySet().stream()
                            .filter(entry -> {
                                String perm = entry.getValue().getPermission();
                                return perm == null || perm.isEmpty() || sender.hasPermission(perm);
                            })
                            .map(Map.Entry::getKey)
                            .filter(cmd -> cmd.startsWith(input))
                            .toList()
            );

            return suggestions;
        }

        // If there are arguments, suggest the next argument based on the current meta
        TabContext context = new TabContext(sender, args, consumed, currentMeta);
        SuggestionProvider provider = currentMeta.getSuggestionProvider(context.getArgumentIndex());

        if (provider != null) {
            suggestions.addAll(provider.suggest(context));
        }

        return suggestions;
    }


    /**
     * Handles the command execution.
     *
     * @param sender The command sender (e.g., a player).
     * @param command The command being executed.
     * @param label The label of the command.
     * @param args The arguments passed to the command.
     * @return true if the command was executed successfully; false otherwise.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return execute(sender, label, args);
    }

    /**
     * Executes the command and checks for errors.
     *
     * @param sender The command sender (e.g., a player).
     * @param currentMeta The current command metadata.
     * @param args The arguments passed to the command.
     * @return true if the command was executed successfully; false otherwise.
     */
    private boolean executeMetaCommand(CommandSender sender, CommandMeta currentMeta, String[] args) {
        String errorCode = currentMeta.check(sender);

        // Verify if there is an error code
        if (!errorCode.isEmpty()) {
            return sendErrorMessage(sender, errorCode); // Handle the error
        }

        if (currentMeta.getCommand() != null) {
            currentMeta.getCommand().execute(sender, args);
        } else {
            sendErrorMessage(sender, "invalid_command");
        }
        return true;
    }

    /**
     * Sends an error message to the command sender.
     *
     * @param sender The command sender (e.g., a player).
     * @param type The type of error message to send.
     * @return true if the error message was sent successfully; false otherwise.
     */
    private boolean sendErrorMessage(CommandSender sender, String type) {
        switch (type) {
            case "invalid_command":
                Messenger.prefixedSend(sender,"&7Comando inválido");
                break;
            case "invalid_arguments":
                Messenger.prefixedSend(sender, "&7Argumentos inválidos");
                break;
            case "no_permission":
                Messenger.prefixedSend(sender,"&cNo tienes permiso de ejecutar este comando");
                break;
            case "player_only":
                Messenger.consoleSend(sender,"Este comando solo puede ser ejecutado por un jugador");
                break;
            default:
                Messenger.prefixedSend(sender,"&cError desconocido");
        }
        return true;
    }
}


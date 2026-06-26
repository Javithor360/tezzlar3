package com.panita.tezzlar3.core.commands;

import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.DynamicBukkitCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.Global;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CommandRegistry is a utility class for registering commands in a Bukkit plugin.
 * It uses reflection to scan for classes annotated with @CommandSpec and @SubCommandSpec,
 * and registers them with the Bukkit command map.
 */
public class CommandRegistry {
    private final JavaPlugin plugin;
    // Map to hold subcommands for each command
    private static final Map<String, CommandMeta> rootCommands = new HashMap<>();
    // Map to hold module-registered commands
    private static final Map<String, List<String>> moduleRegisteredCommands = new HashMap<>();

    /**
     * Constructor for CommandRegistry.
     *
     * @param plugin The JavaPlugin instance to register commands with.
     */
    public CommandRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Retrieves the root command metadata for a given command name.
     *
     * @param name The name of the command.
     * @return The CommandMeta instance for the command, or null if not found.
     */
    public static CommandMeta getRootCommand(String name) {
        return rootCommands.get(name.toLowerCase());
    }

    /**
     * Registers all commands and subcommands found within the specified base package.
     * It scans for classes annotated with @CommandSpec (main commands) and @SubCommandSpec (subcommands).
     *
     * @param basePackage The base package where commands and subcommands are located.
     */
    public void registerAll(String basePackage) {
        // Use Reflections to scan for classes in the specified package
        Reflections reflections = new Reflections(basePackage);

        // Get all classes annotated with @CommandSpec (main commands)
        Set<Class<?>> rootCommandClasses = reflections.getTypesAnnotatedWith(CommandSpec.class);
        // Get all classes annotated with @SubCommandSpec (subcommands)
        Set<Class<?>> subCommandClasses = reflections.getTypesAnnotatedWith(SubCommandSpec.class);

        // Then register the main commands
        for (Class<?> clazz : rootCommandClasses) {
            try {
                // Check if the class is annotated with @CommandSpec
                CommandSpec spec = clazz.getAnnotation(CommandSpec.class);
                // Check if the class implements AdvancedCommand
                AdvancedCommand cmd = (AdvancedCommand) clazz.getDeclaredConstructor().newInstance();
                // Instance to hold the command metadata
                CommandMeta meta = new CommandMeta(cmd, spec.permission(), spec.playerOnly(), spec.syntax(), spec.description());

                // Check if the command implements TabSuggestingCommand
                if (cmd instanceof TabSuggestingCommand suggesting) {
                    suggesting.applySuggestions(meta); // Apply tab suggestions to the command
                }

                // Register the main command
                rootCommands.put(spec.name().toLowerCase(), meta);
                Global.ROOT_COMMANDS.put(spec.name().toLowerCase(), meta);
                // Register the command with its aliases
                registerBukkitCommand(spec.name(), meta, Arrays.asList(spec.aliases()));

                moduleRegisteredCommands.computeIfAbsent(basePackage, k -> new ArrayList<>()).add(spec.name().toLowerCase());
            } catch (Exception e) {
                plugin.getLogger().warning("[ERROR] Error while registering root command: " + e.getMessage());
            }
        }

        // First, register all subcommands grouped by their parent command
        for (Class<?> clazz : subCommandClasses) {
            try {
                // Check if the class is annotated with @SubCommandSpec
                SubCommandSpec spec = clazz.getAnnotation(SubCommandSpec.class);
                // Check if the class implements AdvancedCommand
                AdvancedCommand cmd = (AdvancedCommand) clazz.getDeclaredConstructor().newInstance();
                // Instance to hold the command metadata
                CommandMeta meta = new CommandMeta(cmd, spec.permission(), spec.playerOnly(), spec.syntax(), spec.description());

                // Check if the command implements TabSuggestingCommand
                if (cmd instanceof TabSuggestingCommand suggesting) {
                    suggesting.applySuggestions(meta); // Apply tab suggestions to the command
                }

                // Get a full path of the command
                String[] path = (spec.parent() + " " + spec.name()).toLowerCase().split(" ");
                if (path.length < 2) {
                    plugin.getLogger().warning("[WARN] Invalid subcommand: It's mandatory to provide at least one parent and a name.");
                    continue;
                }

                // Iterate through the path and build the sublevels
                CommandMeta current = rootCommands.get(path[0]);
                if (current == null) {
                    plugin.getLogger().warning("[WARN] Root command not found for: " + path[0]);
                    continue;
                }

                for (int i = 1; i < path.length - 1; i++) {
                    current = current.getSubcommandOrCreateInvalid(path[i]);
                }

                current.addSubCommand(path[path.length - 1], meta);
            } catch (Exception e) {
                plugin.getLogger().warning("[ERROR] Error in subcommand: " + e.getMessage());
            }
        }
    }

    /**
     * Registers a command with Bukkit, including its aliases and subcommands.
     *
     * @param name The name of the command.
     * @param meta The CommandMeta instance containing command metadata.
     * @param aliases An array of aliases for the command.
     */
    private void registerBukkitCommand(String name, CommandMeta meta, List<String> aliases) {
        try {
            // Define the command map field in the Bukkit server class
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true); // Make the field accessible
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer()); // Get the command map instance

            // Create a new dynamic command with its metadata and subcommands (if any)
            DynamicBukkitCommand dynamicCommand = new DynamicBukkitCommand(name, meta);
            dynamicCommand.setAliases(aliases != null ? aliases : new ArrayList<>()); // Set the command aliases

            // Create a new PluginCommand instance for Tab Completion
            PluginCommand pluginCommand = createPluginCommand(name, plugin);
            pluginCommand.setExecutor(dynamicCommand);
            pluginCommand.setPermission(meta.getPermission());
            pluginCommand.setTabCompleter(dynamicCommand);
            pluginCommand.setAliases(aliases != null ? aliases : new ArrayList<>());

            commandMap.register(plugin.getName(), pluginCommand); // Register the command with the command map
            plugin.getLogger().info("[INFO] Registered command /" + name);
        } catch (Exception e) {
            plugin.getLogger().warning("[ERROR] Couldn't register command /" + name + ": " + e.getMessage());
        }
    }

    /**
     * Creates a new PluginCommand instance for the specified command name and plugin.
     *
     * @param name The name of the command.
     * @param plugin The JavaPlugin instance to associate with the command.
     * @return A new PluginCommand instance.
     * @throws Exception If an error occurs during instantiation.
     */
    private PluginCommand createPluginCommand(String name, JavaPlugin plugin) throws Exception {
        // Gets the constructor of PluginCommand
        Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        constructor.setAccessible(true);
        return constructor.newInstance(name, plugin); // Create a new instance of PluginCommand
    }

    /**
     * Unregisters all commands associated with the specified base package using reflection.
     *
     * @param basePackage The base package of the module whose commands should be unregistered.
     */
    public void unregisterAll(String basePackage) {
        List<String> registered = moduleRegisteredCommands.get(basePackage);
        if (registered == null || registered.isEmpty()) return;

        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            for (String cmdName : registered) {
                knownCommands.remove(cmdName);
                knownCommands.remove(plugin.getName().toLowerCase() + ":" + cmdName);

                CommandMeta meta = rootCommands.get(cmdName);
                if (meta != null && meta.getCommand() != null) {
                    CommandSpec spec = meta.getCommand().getClass().getAnnotation(CommandSpec.class);
                    if (spec != null) {
                        for (String alias : spec.aliases()) {
                            knownCommands.remove(alias.toLowerCase());
                            knownCommands.remove(plugin.getName().toLowerCase() + ":" + alias.toLowerCase());
                        }
                    }
                }

                rootCommands.remove(cmdName);
                Global.ROOT_COMMANDS.remove(cmdName);
                plugin.getLogger().info("[INFO] Unregistered command /" + cmdName);
            }

            moduleRegisteredCommands.remove(basePackage);

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.updateCommands();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[ERROR] Failed to unregister commands for " + basePackage + ": " + e.getMessage());
        }
    }
}


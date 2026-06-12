package com.panita.tezzlar3.core.commands.base.module;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.config.ConfigManager;
import com.panita.tezzlar3.core.modules.ModuleManager;
import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.core.util.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SubCommandSpec(
        parent = "tezzlar module",
        name = "config",
        description = "Change a module's configuration value",
        syntax = "/tezzlar module config <module_id> <config_name> <value>",
        permission = "tezzlar.command.module.config"
)
public class ModuleConfigCommand implements AdvancedCommand, TabSuggestingCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 3, this.getClass())) return;

        String moduleId = args[0];
        String configKey = args[1];
        String value = args[2];

        ModuleManager moduleManager = Tezzlar.getModuleManager();
        PluginModule module = moduleManager.getAllModules().stream()
                .filter(m -> m.id().equalsIgnoreCase(moduleId))
                .findFirst()
                .orElse(null);

        if (module == null) {
            Messenger.prefixedSend(sender, "&cNo existe ningún módulo con ID '" + moduleId + "'.");
            return;
        }

        ConfigManager cm = module.configManager();
        String fullPath = moduleId + "." + configKey;

        try {
            // Detect boolean
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                boolean boolValue = Boolean.parseBoolean(value);
                cm.updateBoolean(fullPath, boolValue, (path, val) -> module.reload(Tezzlar.getInstance()));
            }
            // Detect String List by comma
            else if (value.contains(",")) {
                List<String> list = Arrays.stream(value.split(",")).map(String::trim).toList();
                cm.updateStringList(fullPath, list, (path, val) -> module.reload(Tezzlar.getInstance()));
            }
            // Detect int
            else {
                try {
                    int intValue = Integer.parseInt(value);
                    cm.updateInt(fullPath, intValue, (path, val) -> module.reload(Tezzlar.getInstance()));
                } catch (NumberFormatException e1) {
                    // Detect double
                    try {
                        double doubleValue = Double.parseDouble(value);
                        cm.updateDouble(fullPath, doubleValue, (path, val) -> module.reload(Tezzlar.getInstance()));
                    } catch (NumberFormatException e2) {
                        // fallback to string
                        cm.updateString(fullPath, value, (path, val) -> module.reload(Tezzlar.getInstance()));
                    }
                }
            }

            Messenger.prefixedSend(sender, "&7Configuración &e'" + configKey + "' &7de módulo &b'" + moduleId + "' &7actualizada a: &a" + value);
        } catch (Exception ex) {
            Messenger.prefixedSend(sender, "&cError al actualizar la configuración: " + ex.getMessage());
        }
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        // First argument: module IDs
        meta.setArgumentSuggestion(0, context -> Tezzlar.getModuleManager().getAllModules().stream()
                .map(PluginModule::id)
                .filter(id -> id.toLowerCase().startsWith(context.getCurrentArg().toLowerCase()))
                .toList()
        );

        // Second argument: config keys of the specified module
        meta.setArgumentSuggestion(1, context -> {
            if (context.getArgs().length < 1) return List.of();
            String moduleId = context.getArgs()[2];
            PluginModule module = Tezzlar.getModuleManager().getAllModules().stream()
                    .filter(m -> m.id().equalsIgnoreCase(moduleId))
                    .findFirst().orElse(null);

            if (module == null) return List.of();
            ConfigurationSection section = module.configManager().getSection(module.id());
            return getAllConfigPaths("", section).stream()
                    .filter(key -> key.toLowerCase().startsWith(context.getCurrentArg().toLowerCase()))
                    .toList();
        });
    }

    private List<String> getAllConfigPaths(String basePath, ConfigurationSection section) {
        List<String> paths = new ArrayList<>();
        if (section == null) return paths;

        for (String key : section.getKeys(false)) {
            Object val = section.get(key);
            String fullPath = basePath.isEmpty() ? key : basePath + "." + key;

            if (val instanceof ConfigurationSection nested) {
                paths.addAll(getAllConfigPaths(fullPath, nested));
            } else {
                paths.add(fullPath);
            }
        }

        return paths;
    }
}


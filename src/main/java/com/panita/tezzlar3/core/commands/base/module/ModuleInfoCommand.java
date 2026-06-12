package com.panita.tezzlar3.core.commands.base.module;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.modules.PluginModule;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

@SubCommandSpec(
        parent = "tezzlar module",
        name = "info",
        description = "Manage or view information about modules",
        syntax = "/tezzlar module info [module_id]",
        permission = "tezzlar.command.module.info"
)
public class ModuleInfoCommand implements AdvancedCommand, TabSuggestingCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        // Show all modules if no specific module ID is provided
        if (args.length == 0) {
            List<PluginModule> all = Tezzlar.getModuleManager().getAllModules();

            Messenger.prefixedSend(sender, "&9===== Módulos =====");
            for (PluginModule module : all) {
                boolean enabled = Tezzlar.getModuleManager().isModuleActive(module.id());
                Messenger.send(sender, "&e- " + module.id() + ": " + (enabled ? "&aActivado" : "&cDesactivado"));
            }
            return;
        }

        // Show detailed info for a specific module
        String moduleId = args[0];
        PluginModule module = Tezzlar.getModuleManager().getAllModules().stream()
                .filter(m -> m.id().equalsIgnoreCase(moduleId))
                .findFirst()
                .orElse(null);

        if (module == null) {
            Messenger.prefixedSend(sender, "&cEl módulo con ID '" + moduleId + "' no existe.");
            return;
        }

        // Retrieve and display the module's configuration
        ConfigurationSection section = module.configManager().getSection(module.id());

        Messenger.prefixedSend(sender, "&9===== Información del Módulo: " + module.id() + " =====");
        Messenger.send(sender, "&7Estado: " + (Tezzlar.getModuleManager().isModuleActive(module.id()) ? "&aActivado" : "&cDesactivado"));

        if (section != null) {
            printSection(sender, section, "");
        } else {
            Messenger.prefixedSend(sender, "&cEl módulo '" + moduleId + "' no tiene configuración disponible.");
        }
    }

    // Recursively prints a configuration section and its subsections
    private void printSection(CommandSender sender, ConfigurationSection section, String prefix) {
        for (String key : section.getKeys(false)) {
            Object val = section.get(key);

            // Handle nested sections
            if (val instanceof ConfigurationSection subSection) {
                Messenger.send(sender, prefix + "⦿ " + key + ":");
                printSection(sender, subSection, prefix + "   ");
            // Handle lists and primitive types
            } else if (val instanceof List<?> list) {
                Messenger.send(sender, prefix + "⦿ " + key + ": &d" + list.toString());
            // Primitive types (String, Integer, Boolean, etc.)
            } else {
                Messenger.send(sender, prefix + "⦿ " + key + ": &d" + val);
            }
        }
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> {
            String current = context.getCurrentArg().toLowerCase();
            return Tezzlar.getModuleManager().getAllModules().stream()
                    .map(PluginModule::id)
                    .filter(id -> id.toLowerCase().startsWith(current))
                    .toList();
        });
    }
}


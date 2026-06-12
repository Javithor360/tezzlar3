package com.panita.tezzlar3.core.commands.base.module;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.modules.ModuleManager;
import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.core.util.CommandUtils;
import org.bukkit.command.CommandSender;

@SubCommandSpec(
        parent = "tezzlar module",
        name = "toggle",
        description = "Activate or deactivate a module",
        syntax = "/tezzlar module toggle [module_id]",
        permission = "tezzlar.command.module.toggle"
)
public class ModuleToggleCommand implements AdvancedCommand, TabSuggestingCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        String moduleId = args[0];
        ModuleManager moduleManager = Tezzlar.getModuleManager();
        PluginModule module = moduleManager.getAllModules().stream()
                .filter(m -> m.id().equalsIgnoreCase(moduleId))
                .findFirst()
                .orElse(null);

        if (module == null) {
            Messenger.prefixedSend(sender, "&cNo existe ningún módulo con ID '" + moduleId + "'.");
            return;
        }

        if (module.isEnabled()) {
            moduleManager.disableModule(module);
            Messenger.prefixedSend(sender, "&cMódulo '" + moduleId + "' desactivado.");
        } else {
            moduleManager.reloadModule(module);
            Messenger.prefixedSend(sender, "&aMódulo '" + moduleId + "' activado.");
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


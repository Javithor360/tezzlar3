package com.panita.tezzlar3.core.commands.base;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.config.Config;
import com.panita.tezzlar3.core.config.ConfigManager;
import com.panita.tezzlar3.core.modules.ModuleManager;
import com.panita.tezzlar3.core.modules.PluginModule;
import org.bukkit.command.CommandSender;

@SubCommandSpec(
        parent = "tezzlar",
        name = "reload",
        description = "Reload Tezzlar's configuration and all active modules.",
        syntax = "/tezzlar reload",
        permission = "tezzlar.command.reload"
)
public class ReloadCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        Tezzlar plugin = Tezzlar.getInstance();

        // Reload main config
        Config.reload(plugin);

        // Reload ConfigManager
        Tezzlar.setConfigManager(new ConfigManager(plugin, plugin.getConfig()));

        // Iterate through all modules and reload their configs
        ModuleManager moduleManager = Tezzlar.getModuleManager();
        for (PluginModule module : moduleManager.getAllModules()) {
            moduleManager.reloadModule(module);
        }

        Messenger.prefixedSend(sender, "&aConfiguración recargada correctamente.");
    }
}


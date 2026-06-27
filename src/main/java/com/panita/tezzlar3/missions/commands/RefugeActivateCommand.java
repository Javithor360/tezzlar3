package com.panita.tezzlar3.missions.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.refuge.RefugeManager;
import org.bukkit.command.CommandSender;

@SubCommandSpec (
        parent = "refuge",
        name = "activate",
        description = "Start the Refuge event",
        syntax = "/refuge activate",
        playerOnly = false
)
public class RefugeActivateCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        RefugeManager manager = MissionsModule.getRefugeManager();
        if (manager == null) {
            Messenger.prefixedSend(sender, "&cEl gestor de refugio no está inicializado.");
            return;
        }
        
        if (manager.isActive()) {
            Messenger.prefixedSend(sender, "&cEl evento de refugio ya está activo.");
            return;
        }
        
        manager.startEvent();
        Messenger.prefixedSend(sender, "&aEvento de refugio activado.");
    }
}

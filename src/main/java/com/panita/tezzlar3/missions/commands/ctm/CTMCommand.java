package com.panita.tezzlar3.missions.commands.ctm;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import org.bukkit.command.CommandSender;

@CommandSpec(
        name = "ctm",
        description = "Administración de la mecánica CTM.",
        syntax = "/ctm",
        permission = "tezzlar.command.ctm"
)
public class CTMCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        Messenger.prefixedSend(sender, "&7Uso: &b/ctm <status|reset>");
    }
}

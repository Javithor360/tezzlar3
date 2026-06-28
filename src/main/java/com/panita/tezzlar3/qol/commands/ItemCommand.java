package com.panita.tezzlar3.qol.commands;

import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.qol.gui.customitems.CustomItemMainMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SubCommandSpec(
        parent = "tezzlar",
        name = "item",
        description = "Opens the custom items manager menu.",
        syntax = "/tezzlar item",
        permission = "tezzlar.command.item",
        playerOnly = true
)
public class ItemCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        // If args is empty, it executes this block
        if (args.length == 0) {
            Player player = (Player) sender;
            new CustomItemMainMenu(player).open();
        }
    }
}

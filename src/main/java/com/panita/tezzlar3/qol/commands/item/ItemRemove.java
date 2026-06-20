package com.panita.tezzlar3.qol.commands.item;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.command.CommandSender;

import java.util.stream.Collectors;

@SubCommandSpec(
        parent = "tezzlar item",
        name = "remove",
        description = "Removes a saved custom item from the JSON file.",
        syntax = "/Tezzlar item remove <item_name>",
        permission = "Tezzlar.command.item.remove",
        playerOnly = false
)
public class ItemRemove implements AdvancedCommand, TabSuggestingCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        String itemName = args[0];
        CustomItemManager.ItemResult result = CustomItemManager.removeItem(itemName);

        switch (result) {
            case SUCCESS -> Messenger.prefixedSend(sender, "&aItem &e" + itemName + " &ahas been removed successfully.");
            case NOT_FOUND -> Messenger.prefixedSend(sender, "&cNo custom item found with the name &e" + itemName);
            case ERROR -> Messenger.prefixedSend(sender, "&cAn unexpected error occurred while removing the item.");
        }
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> CustomItemManager.getAllItemNames().stream()
                .filter(name -> name.toLowerCase().startsWith(context.getCurrentArg().toLowerCase()))
                .collect(Collectors.toList()));
    }
}

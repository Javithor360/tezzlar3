package com.panita.tezzlar3.qol.commands.item;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SubCommandSpec(
        parent = "tezzlar item",
        name = "save",
        description = "Save the item in your hand into the custom item collection.",
        syntax = "/Tezzlar item save <item_name>",
        permission = "Tezzlar.command.item.save",
        playerOnly = true
)
public class ItemSave implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        // Player Only is already checked by the annotation
        Player player = (Player) sender;

        String itemName = args[0];
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType().isAir()) {
            Messenger.prefixedSend(player, "&cYou need to hold an &eitem &ain your main hand.");
            return;
        }

        CustomItemManager.ItemResult result = CustomItemManager.saveItem(itemName, itemInHand);

        switch (result) {
            case SUCCESS -> Messenger.prefixedSend(player, "&aItem &e" + itemName + " &asaved successfully.");
            case DUPLICATE_NAME -> Messenger.prefixedSend(player, "&cAn item with the name &e" + itemName + " &calready exists.");
            case ERROR -> Messenger.prefixedSend(player, "&cAn unexpected error occurred while saving the item.");
        }
    }
}

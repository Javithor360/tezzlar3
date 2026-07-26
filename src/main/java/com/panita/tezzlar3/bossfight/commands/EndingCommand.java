package com.panita.tezzlar3.bossfight.commands;

import com.panita.tezzlar3.bossfight.util.EndingItems;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.chat.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

@CommandSpec(
        name = "ending",
        description = "Gives the required items for the ending",
        permission = "tezzlar3.ending",
        playerOnly = true
)
public class EndingCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        
        giveItemForcefully(player, 2, EndingItems.getFinalMessageItem());
        giveItemForcefully(player, 3, EndingItems.getInstantPortalItem());
        giveItemForcefully(player, 5, EndingItems.getSalvationItem());
        giveItemForcefully(player, 6, EndingItems.getSunlightItem());
        
        Messenger.prefixedSend(player, "<green>Ítems del final entregados.</green>");
    }

    private void giveItemForcefully(Player player, int slot, ItemStack item) {
        ItemStack existing = player.getInventory().getItem(slot);
        if (existing != null && existing.getType() != Material.AIR) {
            player.getInventory().setItem(slot, null);
            player.getInventory().addItem(existing);
        }
        player.getInventory().setItem(slot, item);
    }
}

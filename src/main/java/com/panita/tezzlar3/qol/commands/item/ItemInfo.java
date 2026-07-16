package com.panita.tezzlar3.qol.commands.item;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

@SubCommandSpec(
        parent = "tezzlar item",
        name = "info",
        description = "Shows basic info about a selected item",
        syntax = "/tezzlar item info",
        permission = "tezzlar.command.item.info",
        playerOnly = true
)
public class ItemInfo implements AdvancedCommand {
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            Messenger.prefixedSend(player, "&cDebes tener un item en la mano principal para ver su información.");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        String type = item.getType().name();
        
        // Adventure check for custom name
        String customName = "Ninguno";
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            if (item.getItemMeta().displayName() != null) {
                customName = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
            } else {
                customName = item.getItemMeta().getDisplayName();
            }
        }

        int maxStack = item.getMaxStackSize();
        
        boolean isCustom = CustomItemManager.isCustomItem(item);
        String customId = "N/A";
        if (isCustom && meta != null) {
            NamespacedKey key = new NamespacedKey(Tezzlar.getInstance(), "custom_item_id");
            customId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if (customId == null) customId = "Desconocido";
        }

        Messenger.send(player, "");
        Messenger.send(player, "&e&lInformación del Ítem");
        Messenger.send(player, "&7Tipo de Material: &b" + type);
        Messenger.send(player, "&7Nombre: &b" + customName);
        Messenger.send(player, "&7Max Stack Size: &b" + maxStack);
        Messenger.send(player, "&7Ítem Custom de Tezzlar3: " + (isCustom ? "&aSí" : "&cNo"));
        if (isCustom) {
            Messenger.send(player, "&7Custom ID (PDC): &b" + customId);
        }
        
        Component nbtButton = Component.text("[")
            .color(NamedTextColor.WHITE)
            .append(Component.text("Copiar NBT", NamedTextColor.GREEN))
            .append(Component.text("]"))
            .clickEvent(ClickEvent.runCommand("/data get entity @s SelectedItem"))
            .hoverEvent(HoverEvent.showText(Component.text("Haz clic para ejecutar /data get entity @s SelectedItem y copiar el NBT", NamedTextColor.GREEN)));
            
        player.sendMessage(Component.text(" ").append(nbtButton));
        Messenger.send(player, "");
    }
}

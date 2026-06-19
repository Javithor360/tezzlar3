package com.panita.tezzlar3.inventory.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@CommandSpec(
        name = "gravekit",
        description = "Te da un kit de prueba para el sistema de tumbas.",
        permission = "tezzlar.command.gravekit",
        playerOnly = true
)
public class GraveKitCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        // Helmet
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        helmet.addUnsafeEnchantment(Enchantment.PROTECTION, 4);
        
        // Chestplate
        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        chestplate.addUnsafeEnchantment(Enchantment.FIRE_PROTECTION, 4);
        
        // Leggings
        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        leggings.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 4);
        
        // Boots
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        boots.addUnsafeEnchantment(Enchantment.PROTECTION, 4);
        
        // Sword
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
        ItemMeta swordMeta = sword.getItemMeta();
        swordMeta.customName(Messenger.mini("&cEspada de Prueba"));
        sword.setItemMeta(swordMeta);
        
        // Other items
        ItemStack shield = new ItemStack(Material.SHIELD);
        ItemStack apples = new ItemStack(Material.GOLDEN_APPLE, 12);
        ItemStack carrots = new ItemStack(Material.GOLDEN_CARROT, 43);
        ItemStack diamondBlocks = new ItemStack(Material.DIAMOND_BLOCK, 15);
        ItemStack bedrock = new ItemStack(Material.BEDROCK, 64);

        // Equip
        player.getEquipment().setHelmet(helmet);
        player.getEquipment().setChestplate(chestplate);
        player.getEquipment().setLeggings(leggings);
        player.getEquipment().setBoots(boots);
        player.getEquipment().setItemInMainHand(sword);
        player.getEquipment().setItemInOffHand(shield);

        // Give items
        player.getInventory().addItem(apples, carrots, diamondBlocks, bedrock);

        Messenger.prefixedSend(player, "&a¡Kit de prueba de tumbas recibido!");
    }
}

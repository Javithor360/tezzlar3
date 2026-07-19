package com.panita.tezzlar3.missions.handlers.punishments;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.missions.handlers.PunishmentHandler;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class OffhandRestrictionPunishmentHandler implements PunishmentHandler, Listener {
    private final ItemStack indicatorItem = new ItemStack(Material.STRUCTURE_VOID);
    
    public OffhandRestrictionPunishmentHandler(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getId() {
        return "OFFHAND_RESTRICTION";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        // Enforce the logic right when the punishment applies
        applyRestriction(player);
    }

    private void applyRestriction(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack offhand = inv.getItemInOffHand();

        if (offhand != null && offhand.getType() != Material.AIR && offhand.getType() != Material.STRUCTURE_VOID) {
            HashMap<Integer, ItemStack> left = inv.addItem(offhand);
            if (!left.isEmpty()) {
                for (ItemStack item : left.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
        }
        
        inv.setItemInOffHand(indicatorItem.clone());
    }

    private boolean isPunished(Player player) {
        PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
        return data != null && data.hasPunishment(getId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (isPunished(event.getPlayer()) && PlayerUtils.isSurvival(event.getPlayer())) {
            event.setCancelled(true);
            Messenger.prefixedSend(event.getPlayer(), "<red>Tienes prohibido usar la mano secundaria debido a un castigo activo.</red>");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        if (event.getSlot() == 40 && PlayerUtils.isSurvival(player)) {
            if (isPunished(player)) {
                event.setCancelled(true);
                Messenger.prefixedSend(player, "<red>Tienes prohibido usar la mano secundaria debido a un castigo activo.</red>");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == Material.STRUCTURE_VOID) {
            event.setCancelled(true);
        }
    }
}

package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.minievents.MiniEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.GameMode;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.util.PlayerUtils;

import java.util.HashMap;

public class NoOffhandEvent implements MiniEvent, Listener {

    private final ItemStack indicatorItem = new ItemStack(Material.STRUCTURE_VOID);

    @Override
    public void start(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (PlayerUtils.isSurvival(player)) {
                applyRestriction(player);
            }
        }
    }

    @Override
    public void stop(JavaPlugin plugin) {
        HandlerList.unregisterAll(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            removeRestriction(player);
        }
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

    private void removeRestriction(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack offhand = inv.getItemInOffHand();

        if (offhand != null && offhand.getType() == Material.STRUCTURE_VOID) {
            inv.setItemInOffHand(new ItemStack(Material.AIR));
        }
    }

    @Override
    public String getId() {
        return "no_offhand";
    }

    @Override
    public String getDisplayName() {
        return "<#FF5555>Sin Mano Secundaria</#FF5555>";
    }

    @Override
    public String getDescription() {
        return "\n&7Durante la próxima &b1 hora&7, no se podrá utilizar la mano secundaria.\n\n&3- &7Los ítems que estuvieran ahí serán devueltos a tu inventario.";
    }

    @Override
    public long getDurationTicks() {
        return 60 * 60 * 20L; // 1 hour
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (PlayerUtils.isSurvival(event.getPlayer())) {
            applyRestriction(event.getPlayer());
        }
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode() == GameMode.SURVIVAL || event.getNewGameMode() == GameMode.ADVENTURE) {
            applyRestriction(event.getPlayer());
        } else {
            removeRestriction(event.getPlayer());
        }
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        if (PlayerUtils.isSurvival(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && PlayerUtils.isSurvival(player)) {
            if (event.getSlot() == 40) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == Material.STRUCTURE_VOID) {
            event.setCancelled(true);
        }
    }
}

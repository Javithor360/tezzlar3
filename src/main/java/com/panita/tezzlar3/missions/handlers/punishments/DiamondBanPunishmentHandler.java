package com.panita.tezzlar3.missions.handlers.punishments;

import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.missions.handlers.PunishmentHandler;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class DiamondBanPunishmentHandler implements PunishmentHandler, Listener {
    
    @Override
    public String getId() {
        return "DIAMOND_BAN";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
        if (data != null) {
            data.addPunishment(getId());
        }
    }
    
    private boolean isDiamondGear(Material material) {
        if (material == null) return false;
        String name = material.name();
        return name.contains("DIAMOND");
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
        if (data != null && data.hasPunishment(getId())) {
            if (isDiamondGear(event.getItem().getItemStack().getType())) {
                event.setCancelled(true);
                event.getItem().remove(); // Destroy the item in the world
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
        if (data != null && data.hasPunishment(getId())) {
            ItemStack current = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            
            if ((current != null && isDiamondGear(current.getType())) || 
                (cursor != null && isDiamondGear(cursor.getType()))) {
                event.setCancelled(true);
                if (current != null) current.setAmount(0); // Destroy in slot
                if (cursor != null) cursor.setAmount(0); // Destroy in cursor
            }
        }
    }
}

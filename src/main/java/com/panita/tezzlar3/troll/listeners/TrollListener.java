package com.panita.tezzlar3.troll.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TrollListener implements Listener {
    
    private static final Set<UUID> copperTrailTargets = new HashSet<>();
    private static final Set<Location> protectedCopperBlocks = new HashSet<>();

    public static void addCopperTrailTarget(UUID uuid) {
        copperTrailTargets.add(uuid);
        // Troll duration: 5 seconds leaving copper blocks
        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
            copperTrailTargets.remove(uuid);
        }, 100L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!copperTrailTargets.contains(event.getPlayer().getUniqueId())) return;

        Location to = event.getTo();
        if (to == null) return;
        
        Block blockUnder = to.clone().subtract(0, 1, 0).getBlock();
        
        // Avoid air, blocks that are already copper, and special blocks that should not be overwritten
        if (blockUnder.getType().isAir() || blockUnder.getType() == Material.RAW_COPPER_BLOCK) return;
        if (!blockUnder.getType().isSolid() || blockUnder.getType() == Material.BEDROCK || blockUnder.getType() == Material.END_PORTAL_FRAME) return;
        if (blockUnder.getState() instanceof InventoryHolder) return;

        // Save original block data
        BlockData originalData = blockUnder.getBlockData().clone();
        
        // Replace with raw copper
        blockUnder.setType(Material.RAW_COPPER_BLOCK);
        protectedCopperBlocks.add(blockUnder.getLocation());

        // Revert back after 5 seconds
        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
            blockUnder.setBlockData(originalData);
            protectedCopperBlocks.remove(blockUnder.getLocation());
        }, 100L);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (protectedCopperBlocks.contains(event.getBlock().getLocation())) {
            event.setCancelled(true);
            Messenger.prefixedSend(event.getPlayer(), "&cNo puedes romper un bloque en transformación.");
        }
    }
}

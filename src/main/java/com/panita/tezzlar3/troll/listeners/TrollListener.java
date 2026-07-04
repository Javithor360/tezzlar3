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
    private static final Set<UUID> magmaTrailTargets = new HashSet<>();
    private static final Set<UUID> debrisTrailTargets = new HashSet<>();
    private static final Set<Location> protectedTrailBlocks = new HashSet<>();

    public static void addCopperTrailTarget(UUID uuid) {
        copperTrailTargets.add(uuid);
        // Troll duration: 5 seconds leaving copper blocks
        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
            copperTrailTargets.remove(uuid);
        }, 100L);
    }

    public static void addMagmaTrailTarget(UUID uuid) {
        magmaTrailTargets.add(uuid);
        // Troll duration: 15 seconds leaving magma blocks
        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
            magmaTrailTargets.remove(uuid);
        }, 300L);
    }

    public static void addDebrisTrailTarget(UUID uuid) {
        debrisTrailTargets.add(uuid);
        // Troll duration: 5 seconds leaving ancient debris
        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
            debrisTrailTargets.remove(uuid);
        }, 100L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        boolean copper = copperTrailTargets.contains(uuid);
        boolean magma = magmaTrailTargets.contains(uuid);
        boolean debris = debrisTrailTargets.contains(uuid);
        
        if (!copper && !magma && !debris) return;

        Location to = event.getTo();
        if (to == null) return;
        
        Block blockUnder = to.clone().subtract(0, 1, 0).getBlock();
        
        // Avoid air, blocks that are already the target material, and special blocks that should not be overwritten
        if (blockUnder.getType().isAir()) return;
        if (!blockUnder.getType().isSolid() || blockUnder.getType() == Material.BEDROCK || blockUnder.getType() == Material.END_PORTAL_FRAME) return;
        if (blockUnder.getState() instanceof InventoryHolder) return;

        Material targetMaterial;
        if (copper) targetMaterial = Material.RAW_COPPER_BLOCK;
        else if (debris) targetMaterial = Material.ANCIENT_DEBRIS;
        else targetMaterial = Material.MAGMA_BLOCK;
        
        if (blockUnder.getType() == targetMaterial) return;

        // Save original block data
        BlockData originalData = blockUnder.getBlockData().clone();
        
        // Replace with target material
        blockUnder.setType(targetMaterial);
        protectedTrailBlocks.add(blockUnder.getLocation());

        // Revert back after 5 seconds
        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
            blockUnder.setBlockData(originalData);
            protectedTrailBlocks.remove(blockUnder.getLocation());
        }, 100L);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (protectedTrailBlocks.contains(event.getBlock().getLocation())) {
            event.setCancelled(true);
            Messenger.prefixedSend(event.getPlayer(), "&cNo puedes romper un bloque en transformación.");
        }
    }
}

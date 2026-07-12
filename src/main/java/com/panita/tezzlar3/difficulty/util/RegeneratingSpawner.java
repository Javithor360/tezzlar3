package com.panita.tezzlar3.difficulty.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class RegeneratingSpawner {
    private final Location location;
    private final ItemStack spawnerItem;
    private long timeRemainingMillis;

    public RegeneratingSpawner(Location location, CreatureSpawner spawnerState, long timeRemainingMillis) {
        this.location = location;
        this.timeRemainingMillis = timeRemainingMillis;
        
        // Save the full spawner state (including custom NBT and PDC) into an ItemStack
        ItemStack item = new ItemStack(Material.SPAWNER);
        if (item.getItemMeta() instanceof BlockStateMeta meta) {
            meta.setBlockState(spawnerState);
            item.setItemMeta(meta);
        }
        this.spawnerItem = item;
    }

    public RegeneratingSpawner(Location location, ItemStack spawnerItem, long timeRemainingMillis) {
        this.location = location;
        this.spawnerItem = spawnerItem;
        this.timeRemainingMillis = timeRemainingMillis;
    }

    public Location getLocation() {
        return location;
    }

    public ItemStack getSpawnerItem() {
        return spawnerItem;
    }

    public long getTimeRemainingMillis() {
        return timeRemainingMillis;
    }

    public void setTimeRemainingMillis(long timeRemainingMillis) {
        this.timeRemainingMillis = timeRemainingMillis;
    }
}

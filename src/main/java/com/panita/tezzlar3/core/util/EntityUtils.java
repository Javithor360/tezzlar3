package com.panita.tezzlar3.core.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.panita.tezzlar3.core.chat.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.NamespacedKey;

import java.util.Base64;
import java.util.UUID;

public class EntityUtils {
    /**
     * Get the nearest player to a location within a certain radius.
     * @param loc The location to check from
     * @param radius The radius to check within
     * @return The nearest player, or null if none found
     */
    public static Player getNearestPlayer(Location loc, double radius) {
        World world = loc.getWorld();
        if (world == null) return null;

        Player nearest = null;
        double closest = radius * radius; // Compare squared distances

        for (Player player : world.getPlayers()) {
            if (!player.getWorld().equals(world)) continue;

            double dist = player.getLocation().distanceSquared(loc);
            if (dist <= closest) {
                closest = dist;
                nearest = player;
            }
        }

        return nearest;
    }

    /**
     * Check if there's enough space in height for an entity.
     * @param loc The location to check
     * @param up Number of blocks needed above
     * @param down Number of blocks needed below
     * @return True if there's enough space, false otherwise
     */
    public static boolean isEnoughSpaceY(Location loc, int up, int down) {
        World world = loc.getWorld();
        if (world == null) return false;

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        for (int dy = -down; dy <= up; dy++) {
            Material type = world.getBlockAt(x, y + dy, z).getType();
            if (!type.isAir()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a custom player skull item with a specific texture or profile.
     * @return The custom player skull item.
     */
    public static ItemStack createSkull(String profileUrl) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
        profile.setProperty(new ProfileProperty("textures",
                Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + profileUrl + "\"}}}").getBytes())
        ));
        skullMeta.setPlayerProfile(profile);
        skull.setItemMeta(skullMeta);

        return skull;
    }

    /**
     * Checks if a spawn reason is considered natural or valid for applying difficulty mechanics.
     * Allowed: NATURAL, SPAWNER, SPAWNER_EGG
     */
    public static boolean isValidNaturalSpawn(SpawnReason reason) {
        return reason == SpawnReason.NATURAL || 
               reason == SpawnReason.SPAWNER ||
               reason == SpawnReason.SPAWNER_EGG;
    }

    /**
     * Checks if a mob has been marked as a custom Tezzlar mob (e.g. Beekeeper, Infrared).
     * This relies on the custom mobs using PersistentDataContainer keys that start with "is_".
     */
    public static boolean isCustomMob(LivingEntity entity) {
        for (NamespacedKey key : entity.getPersistentDataContainer().getKeys()) {
            if (key.getKey().startsWith("is_")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Equips armor to a LivingEntity with a specified drop chance.
     */
    public static void equipArmor(LivingEntity entity, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, float dropChance) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment != null) {
            if (helmet != null) {
                equipment.setHelmet(helmet);
                equipment.setHelmetDropChance(dropChance);
            }
            if (chestplate != null) {
                equipment.setChestplate(chestplate);
                equipment.setChestplateDropChance(dropChance);
            }
            if (leggings != null) {
                equipment.setLeggings(leggings);
                equipment.setLeggingsDropChance(dropChance);
            }
            if (boots != null) {
                equipment.setBoots(boots);
                equipment.setBootsDropChance(dropChance);
            }
        }
    }

    /**
     * Applies a formatted custom name to an entity using MiniMessage and sets its visibility.
     */
    public static void setCustomName(LivingEntity entity, String name, boolean visible) {
        entity.customName(Messenger.mini(name));
        entity.setCustomNameVisible(visible);
    }

    /**
     * Applies a formatted custom name to an entity using MiniMessage and hides it by default.
     */
    public static void setCustomName(LivingEntity entity, String name) {
        setCustomName(entity, name, false);
    }
}


package com.panita.tezzlar3.missions.handlers.punishments;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.missions.handlers.PunishmentHandler;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.configuration.ConfigurationSection;

public class WitherSkeletonSpawnPunishmentHandler implements PunishmentHandler, Listener {
    private final double spawnChance;

    public WitherSkeletonSpawnPunishmentHandler(double spawnChance) {
        this.spawnChance = spawnChance;
    }

    @Override
    public String getId() {
        return "WITHER_SKELETON_SPAWN_ON_DAMAGE";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        // Passive effect
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getWorld().getEnvironment() == World.Environment.NETHER) {
                PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
                if (data != null && data.hasPunishment(getId())) {
                    if (Math.random() <= spawnChance) {
                        WitherSkeleton skeleton = (WitherSkeleton) player.getWorld().spawnEntity(player.getLocation(), EntityType.WITHER_SKELETON);
                        
                        // Prevent drops
                        if (skeleton.getEquipment() != null) {
                            skeleton.getEquipment().clear();
                            skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
                            skeleton.getEquipment().setItemInMainHandDropChance(0f);
                        }
                        
                        // Add persistent data to intercept death and clear all drops
                        NamespacedKey key = new NamespacedKey(Tezzlar.getInstance(), "no_drops");
                        skeleton.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                        
                        // Force aggro on player
                        skeleton.setTarget(player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSkeletonDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof WitherSkeleton skeleton) {
            NamespacedKey key = new NamespacedKey(Tezzlar.getInstance(), "no_drops");
            if (skeleton.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
        }
    }
}

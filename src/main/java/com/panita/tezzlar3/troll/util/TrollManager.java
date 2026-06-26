package com.panita.tezzlar3.troll.util;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.troll.listeners.TrollListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrollManager {

    public static void executeThor(Player target) {
        target.getWorld().strikeLightning(target.getLocation());
    }

    public static void executeShuffle(Player target) {
        List<ItemStack> contents = new ArrayList<>();
        // Shuffle only the main inventory (first 36 slots, ignoring armor/offhand)
        for (int i = 0; i < 36; i++) {
            contents.add(target.getInventory().getItem(i));
        }
        Collections.shuffle(contents);
        for (int i = 0; i < 36; i++) {
            target.getInventory().setItem(i, contents.get(i));
        }
    }

    public static boolean executeWaterDrop(Player executor, Player target) {
        if (target.getWorld().getEnvironment() == World.Environment.NETHER) {
            return false;
        }

        if (target.getInventory().firstEmpty() != -1) {
            target.getInventory().addItem(new ItemStack(Material.WATER_BUCKET));
        }

        target.setVelocity(new Vector(0, 5, 0));
        return true;
    }

    public static void executeFakeCreeper(Player target) {
        Location loc = target.getLocation().subtract(target.getLocation().getDirection().normalize().multiply(1.5));
        Creeper creeper = (Creeper) target.getWorld().spawnEntity(loc, EntityType.CREEPER);
        creeper.setPowered(true);
        creeper.setExplosionRadius(0);
        creeper.setTarget(target);
        creeper.ignite();
    }

    public static void executeArachnophobia(Player target) {
        Location loc = target.getLocation();
        if (loc.getBlock().getType().isAir()) {
            loc.getBlock().setType(Material.COBWEB);
            // Remove the cobweb after 5 seconds
            Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                if (loc.getBlock().getType() == Material.COBWEB) {
                    loc.getBlock().setType(Material.AIR);
                }
            }, 100L);
        }
    }

    public static void executeSilverfishSwarm(Player target) {
        Location loc = target.getLocation();
        NamespacedKey parasiteKey = new NamespacedKey(Tezzlar.getInstance(), "is_parasite");
        
        for (int i = 0; i < 5; i++) {
            Silverfish sf = (Silverfish) loc.getWorld().spawnEntity(loc, EntityType.SILVERFISH);
            sf.setTarget(target);
            
            // The first one is the parasite
            if (i == 0) {
                sf.getPersistentDataContainer().set(parasiteKey, PersistentDataType.BYTE, (byte) 1);
                EntityUtils.setCustomName(sf, "&cLepisma Parásito");
                
                Bukkit.getScheduler().runTask(Tezzlar.getInstance(), () -> {
                    if (!sf.isValid() || sf.isDead()) return;
                    
                    AttributeInstance scale = sf.getAttribute(Attribute.SCALE);
                    if (scale != null) scale.setBaseValue(2.0);
                    
                    AttributeInstance health = sf.getAttribute(Attribute.MAX_HEALTH);
                    if (health != null) {
                        health.setBaseValue(16.0);
                        if (sf.getHealth() > 16.0) sf.setHealth(16.0);
                    }
                    
                    AttributeInstance speed = sf.getAttribute(Attribute.MOVEMENT_SPEED);
                    if (speed != null) speed.setBaseValue(speed.getBaseValue() * 1.7);
                });
            }
        }
    }

    public static void enableCopperTrail(Player target) {
        TrollListener.addCopperTrailTarget(target.getUniqueId());
    }

    public static void executeHalfHeart(Player target) {
        target.setHealth(1.0);
    }

    public static void executeStarve(Player target) {
        target.setFoodLevel(0);
        target.setSaturation(0.0f);
    }
}

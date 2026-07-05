package com.panita.tezzlar3.troll.util;

import org.bukkit.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.troll.listeners.TrollListener;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.Duration;

import com.panita.tezzlar3.hardcore.util.HardcoreConfigDefaults;
import com.panita.tezzlar3.hardcore.util.HardcoreMessageFormatter;
import com.panita.tezzlar3.core.chat.Messenger;

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
        Creeper creeper = (Creeper) EntityUtils.spawnNatural(loc, EntityType.CREEPER);
        creeper.setPowered(true);
        creeper.setExplosionRadius(0);
        creeper.setTarget(target);
        creeper.ignite();
    }

    public static void executeArachnophobia(Player target) {
        Location loc = target.getLocation();
        if (loc.getBlock().getType().isAir()) {
            loc.getBlock().setType(Material.COBWEB);
            // Remove the cobweb after 10 seconds (doubled from 5)
            Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                if (loc.getBlock().getType() == Material.COBWEB) {
                    loc.getBlock().setType(Material.AIR);
                }
            }, 200L);
        }
    }

    public static void executeSilverfishSwarm(Player target) {
        Location loc = target.getLocation();
        NamespacedKey parasiteKey = new NamespacedKey(Tezzlar.getInstance(), "is_parasite");
        
        for (int i = 0; i < 5; i++) {
            Silverfish sf = (Silverfish) EntityUtils.spawnNatural(loc, EntityType.SILVERFISH);
            sf.setTarget(target);
            
            // The first one is the parasite
            if (i == 0) {
                sf.getPersistentDataContainer().set(parasiteKey, PersistentDataType.BYTE, (byte) 1);
                EntityUtils.setCustomName(sf, "<#F03C57>Lepisma Parásito</#F03C57>");
                
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

    public static void enableMagmaTrail(Player target) {
        TrollListener.addMagmaTrailTarget(target.getUniqueId());
    }

    public static void enableDebrisTrail(Player target) {
        TrollListener.addDebrisTrailTarget(target.getUniqueId());
    }

    public static void executeHalfHeart(Player target) {
        target.setHealth(1.0);
    }

    public static void executeStarve(Player target) {
        target.setFoodLevel(0);
        target.setSaturation(0.0f);
    }

    public static void executeObsidianBox(Player target) {
        Location loc = target.getLocation().getBlock().getLocation();
        for (int y = -1; y <= 2; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if ((y == 0 || y == 1) && x == 0 && z == 0) continue;
                    loc.clone().add(x, y, z).getBlock().setType(Material.OBSIDIAN);
                }
            }
        }
    }

    public static void executeFakeDeath(Player target) {
        String rawGeneric = Tezzlar.getConfigManager().getString(
                "hardcore.messages.genericDeathMessage",
                HardcoreConfigDefaults.HARDCORE_GENERICDEATHMESSAGE
        );
        String genericMsg = HardcoreMessageFormatter.processPlaceholders(rawGeneric, target, null);
        
        String fallbackDefault = HardcoreConfigDefaults.HARDCORE_DEATHMESSAGES.getOrDefault(
                target.getName(), 
                HardcoreConfigDefaults.HARDCORE_DEATHMESSAGES.get("default")
        );
        String defaultCustomConfig = Tezzlar.getConfigManager().getString(
                "hardcore.messages.deathMessages.default", 
                fallbackDefault
        );
        String rawCustom = Tezzlar.getConfigManager().getString(
                "hardcore.messages.deathMessages." + target.getName(),
                defaultCustomConfig
        );
        String customMsg = HardcoreMessageFormatter.processPlaceholders(rawCustom, target, null);
        
        String rawTitle = Tezzlar.getConfigManager().getString(
                "hardcore.messages.deathTitle",
                HardcoreConfigDefaults.HARDCORE_DEATHTITLE
        );
        String rawSubtitle = Tezzlar.getConfigManager().getString(
                "hardcore.messages.deathSubtitle",
                HardcoreConfigDefaults.HARDCORE_DEATHSUBTITLE
        );
        
        String parsedTitle = HardcoreMessageFormatter.processPlaceholders(rawTitle, target, null);
        String parsedSub = HardcoreMessageFormatter.processPlaceholders(rawSubtitle, target, null);
        
        List<String> sounds = Tezzlar.getConfigManager().getStringList("hardcore.deathSounds");
        if (sounds == null || sounds.isEmpty()) {
            sounds = HardcoreConfigDefaults.HARDCORE_DEATHSOUNDS;
        }
        
        boolean showTitle = rawTitle != null && !rawTitle.trim().isEmpty() || rawSubtitle != null && !rawSubtitle.trim().isEmpty();
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(target.getWorld()) && p.getLocation().distance(target.getLocation()) <= 20) {
                Messenger.send(p, genericMsg);
                Messenger.send(p, customMsg);
                if (showTitle) {
                    Messenger.showTitle(
                            p, 
                            parsedTitle, 
                            parsedSub, 
                            Duration.ZERO, 
                            Duration.ofSeconds(5), 
                            Duration.ofMillis(1000)
                    );
                }
                for (String soundStr : sounds) {
                    String[] parts = soundStr.split(";");
                    if (parts.length > 0) {
                        String soundName = parts[0];
                        float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
                        float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
                        p.playSound(p.getLocation(), soundName, volume, pitch);
                    }
                }
            }
        }
    }

    public static void executeFakeTotem(Player target) {
        target.playEffect(EntityEffect.PROTECTED_FROM_DEATH);
    }

    public static void executeDropItem(Player target) {
        ItemStack item = target.getInventory().getItemInMainHand();
        if (item != null && item.getType() != Material.AIR) {
            target.getWorld().dropItemNaturally(target.getLocation(), item);
            target.getInventory().setItemInMainHand(null);
        }
    }

    public static void executeSwapHands(Player target) {
        ItemStack main = target.getInventory().getItemInMainHand();
        ItemStack off = target.getInventory().getItemInOffHand();
        target.getInventory().setItemInMainHand(off);
        target.getInventory().setItemInOffHand(main);
    }

    public static void executeFakeWarden(Player target) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 0, false, true, true));
        target.playSound(target.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 1.0f, 1.0f);
    }

    public static void executeFakeThor(Player target) {
        target.getWorld().strikeLightningEffect(target.getLocation());
    }

    public static void executeFakeExplosion(Player target) {
        target.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, target.getLocation(), 1);
        target.playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
    }

    public static void executeThorPro(Player target) {
        Location center = target.getLocation();
        World world = target.getWorld();
        
        // Spawn lightning effects around the player
        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() - 0.5) * 10;
            double offsetZ = (Math.random() - 0.5) * 10;
            Location stormLoc = center.clone().add(offsetX, 0, offsetZ);
            world.strikeLightningEffect(world.getHighestBlockAt(stormLoc).getLocation());
        }
        
        // One real lightning on the player
        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
            world.strikeLightning(target.getLocation());
        }, 10L);
    }
}

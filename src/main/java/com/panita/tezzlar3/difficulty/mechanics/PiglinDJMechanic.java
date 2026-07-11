package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.core.util.EntityUtils;

import java.util.Random;

public class PiglinDJMechanic extends DifficultyMechanic {
    private final Random random = new Random();
    private final NamespacedKey djKey;

    public PiglinDJMechanic(JavaPlugin plugin) {
        super(plugin, 26);
        this.djKey = new NamespacedKey(plugin, "piglin_dj");
        CustomMobManager.register(CustomMobType.PIGLIN_DJ, this::spawnManual);
    }

    public void spawnManual(Location loc) {
        PiglinBrute dj = (PiglinBrute) EntityUtils.spawnNatural(loc, EntityType.PIGLIN_BRUTE);
        transform(dj);
    }

    private void transform(PiglinBrute dj) {
        dj.getPersistentDataContainer().set(djKey, PersistentDataType.BYTE, (byte) 1);
        EntityUtils.setCustomName(dj, CustomMobType.PIGLIN_DJ.getCustomName());
        dj.setRemoveWhenFarAway(true);
        dj.setImmuneToZombification(true);

        // Stats
        if (dj.getAttribute(Attribute.MAX_HEALTH) != null) {
            dj.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40.0);
            dj.setHealth(40.0);
        }
        if (dj.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            dj.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(50.0);
        }

        // Equipment
        ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
        ItemStack chestplate = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.NETHERITE_LEGGINGS);
        ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);
        EntityUtils.equipArmor(dj, helmet, chestplate, leggings, boots, 0.0f);
        
        if (dj.getEquipment() != null) {
            dj.getEquipment().setItemInMainHand(new ItemStack(Material.MUSIC_DISC_PIGSTEP));
            dj.getEquipment().setItemInOffHand(new ItemStack(Material.JUKEBOX));
            dj.getEquipment().setItemInMainHandDropChance(0f);
            dj.getEquipment().setItemInOffHandDropChance(0f);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.PIGLIN) {
            if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
            World world = entity.getWorld();
            if (world.getEnvironment() == World.Environment.NETHER) {
                // 4% chance to replace a regular Piglin spawn with the Piglin DJ
                if (random.nextDouble() <= 0.04) {
                    Location loc = entity.getLocation();
                    event.setCancelled(true);
                    
                    PiglinBrute dj = (PiglinBrute) world.spawnEntity(loc, EntityType.PIGLIN_BRUTE);
                    transform(dj);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isActive()) return;

        LivingEntity entity = event.getEntity();
        if (entity.getType() == EntityType.PIGLIN_BRUTE && entity.getPersistentDataContainer().has(djKey, PersistentDataType.BYTE)) {
            Location deathLoc = entity.getLocation();
            World world = deathLoc.getWorld();

            // Spawn 5 random piglins
            for (int i = 0; i < 5; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 4;
                double offsetZ = (random.nextDouble() - 0.5) * 4;
                Location spawnLoc = deathLoc.clone().add(offsetX, 0, offsetZ);
                
                Piglin piglin = (Piglin) world.spawnEntity(spawnLoc, EntityType.PIGLIN);
                piglin.setImmuneToZombification(true);
            }

            // Find a solid block below to place the Jukebox, or just place it at deathLoc
            Block block = deathLoc.getBlock();
            Material oldMaterial = block.getType();
            block.setType(Material.JUKEBOX);
            
            if (block.getState() instanceof Jukebox jukebox) {
                jukebox.setRecord(new ItemStack(Material.MUSIC_DISC_PIGSTEP));
                jukebox.update();
            }

            // Schedule to break the jukebox after 5 seconds (100 ticks)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (block.getType() == Material.JUKEBOX) {
                    if (block.getState() instanceof Jukebox jukebox) {
                        jukebox.stopPlaying(); // Stops the music
                        jukebox.setRecord(null); // Clear the record inside so it doesn't pop out when setting to air
                        jukebox.update();
                    }
                    block.setType(oldMaterial); // Revert to old material (probably AIR)
                }
                
                // Drop the items
                world.dropItemNaturally(deathLoc, new ItemStack(Material.MUSIC_DISC_PIGSTEP));
                world.dropItemNaturally(deathLoc, new ItemStack(Material.JUKEBOX));
                
            }, 100L); // 5 seconds = 100 ticks
        }
    }
}

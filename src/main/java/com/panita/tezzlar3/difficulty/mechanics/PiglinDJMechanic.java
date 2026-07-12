package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.MobGearUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.*;
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
                piglin.setAdult(); // Ensure they are adults
                MobGearUtils.equipRandomGear(piglin); // Equip gear
            }

            // Find a solid block below to place the Jukebox
            Block block = deathLoc.getBlock();
            while (block.getType() == Material.AIR && block.getY() > block.getWorld().getMinHeight()) {
                block = block.getRelative(BlockFace.DOWN);
            }
            if (block.getType() != Material.AIR) {
                block = block.getRelative(BlockFace.UP);
            }
            
            Material oldMaterial = block.getType();
            block.setType(Material.JUKEBOX);
            
            if (block.getState(false) instanceof Jukebox jukebox) {
                jukebox.setRecord(new ItemStack(Material.MUSIC_DISC_PIGSTEP));
                jukebox.update(true);
            }
            
            // Play sound manually
            world.playSound(block.getLocation(), Sound.MUSIC_DISC_PIGSTEP, 3.0f, 1.0f);
            
            // Final variables for the task
            final Block finalBlock = block;
            
            // Schedule to break the jukebox after 10 seconds (200 ticks)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (finalBlock.getType() == Material.JUKEBOX) {
                    if (finalBlock.getState(false) instanceof Jukebox jukebox) {
                        jukebox.stopPlaying();
                        jukebox.setRecord(null);
                        jukebox.update(true);
                    }
                    finalBlock.setType(oldMaterial); // Revert
                }
                
                // Stop the manually played sound for nearby players
                for (Player p : world.getPlayers()) {
                    if (p.getLocation().distanceSquared(finalBlock.getLocation()) < 10000) { // 100 blocks radius
                        p.stopSound(Sound.MUSIC_DISC_PIGSTEP);
                    }
                }
                
                // Drop the items safely
                world.dropItemNaturally(finalBlock.getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.MUSIC_DISC_PIGSTEP));
                world.dropItemNaturally(finalBlock.getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.JUKEBOX));
                
            }, 200L); // 10 seconds = 200 ticks
        }
    }
}

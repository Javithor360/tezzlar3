package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class PyromaniacPiglinMechanic extends DifficultyMechanic {
    private final Random random = new Random();
    private final NamespacedKey pyroKey;

    public PyromaniacPiglinMechanic(JavaPlugin plugin) {
        super(plugin, 27);
        this.pyroKey = new NamespacedKey(plugin, "is_pyromaniac_piglin");
        CustomMobManager.register(CustomMobType.PYROMANIAC_PIGLIN, this::spawnManual);
    }

    public void spawnManual(Location loc) {
        Piglin piglin = (Piglin) EntityUtils.spawnNatural(loc, EntityType.PIGLIN);
        if (piglin != null) {
            transform(piglin);
        }
    }

    private void transform(Piglin piglin) {
        piglin.getPersistentDataContainer().set(pyroKey, PersistentDataType.BYTE, (byte) 1);
        EntityUtils.setCustomName(piglin, CustomMobType.PYROMANIAC_PIGLIN.getCustomName());
        piglin.setImmuneToZombification(true);
        piglin.setRemoveWhenFarAway(true);
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!piglin.isValid() || piglin.isDead()) return;
            
            if (piglin.getAttribute(Attribute.MAX_HEALTH) != null) {
                piglin.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40.0);
                piglin.setHealth(40.0);
            }
            
            EntityEquipment eq = piglin.getEquipment();
            if (eq != null) {
                Key modelKey = Key.key("panita", "fallen_hero");
                
                ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
                Equippable eqCompH = helmet.getData(DataComponentTypes.EQUIPPABLE);
                if (eqCompH != null) helmet.setData(DataComponentTypes.EQUIPPABLE, eqCompH.toBuilder().assetId(modelKey).build());
                eq.setHelmet(helmet);
                eq.setHelmetDropChance(0.0f);
                
                ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
                Equippable eqCompC = chestplate.getData(DataComponentTypes.EQUIPPABLE);
                if (eqCompC != null) chestplate.setData(DataComponentTypes.EQUIPPABLE, eqCompC.toBuilder().assetId(modelKey).build());
                eq.setChestplate(chestplate);
                eq.setChestplateDropChance(0.0f);
                
                ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
                Equippable eqCompL = leggings.getData(DataComponentTypes.EQUIPPABLE);
                if (eqCompL != null) leggings.setData(DataComponentTypes.EQUIPPABLE, eqCompL.toBuilder().assetId(modelKey).build());
                eq.setLeggings(leggings);
                eq.setLeggingsDropChance(0.0f);
                
                ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
                Equippable eqCompB = boots.getData(DataComponentTypes.EQUIPPABLE);
                if (eqCompB != null) boots.setData(DataComponentTypes.EQUIPPABLE, eqCompB.toBuilder().assetId(modelKey).build());
                eq.setBoots(boots);
                eq.setBootsDropChance(0.0f);
                
                ItemStack crossbow = new ItemStack(Material.CROSSBOW);
                eq.setItemInMainHand(crossbow);
                eq.setItemInMainHandDropChance(0.0f);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;

        if (event.getEntity() instanceof Monster) {
            if (EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) {
                if (random.nextDouble() <= 0.007) { // 0.7% chance
                    Location loc = event.getEntity().getLocation();
                    event.setCancelled(true);
                    
                    Piglin piglin = (Piglin) loc.getWorld().spawnEntity(loc, EntityType.PIGLIN);
                    transform(piglin);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isActive()) return;
        
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.PIGLIN) {
            if (entity.getPersistentDataContainer().has(pyroKey, PersistentDataType.BYTE)) {
                event.getDrops().clear();
                event.setDroppedExp(750);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Piglin piglin) {
            if (piglin.getPersistentDataContainer().has(pyroKey, PersistentDataType.BYTE)) {
                Entity projectile = event.getProjectile();
                projectile.getPersistentDataContainer().set(pyroKey, PersistentDataType.BYTE, (byte) 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Arrow arrow) {
            if (arrow.getPersistentDataContainer().has(pyroKey, PersistentDataType.BYTE)) {
                Location hitLoc = event.getHitEntity() != null ? event.getHitEntity().getLocation() : (event.getHitBlock() != null ? event.getHitBlock().getLocation() : arrow.getLocation());
                
                hitLoc.getWorld().createExplosion(hitLoc, 5.0f, false, true);
                arrow.remove();
            }
        }
    }
}

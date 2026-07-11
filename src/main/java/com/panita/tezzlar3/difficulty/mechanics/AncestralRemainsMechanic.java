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
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Piglin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class AncestralRemainsMechanic extends DifficultyMechanic {

    private final Random random = new Random();

    public AncestralRemainsMechanic(JavaPlugin plugin) {
        super(plugin, 25);
        CustomMobManager.register(CustomMobType.ANCESTRAL_REMAINS, this::spawnManual);
    }

    public void spawnManual(Location loc) {
        Piglin piglin = (Piglin) EntityUtils.spawnNatural(loc, EntityType.PIGLIN);
        if (piglin != null) {
            makeAncestral(piglin);
        }
    }

    private void makeAncestral(Piglin piglin) {
        EntityUtils.setCustomName(piglin, "<#803522>Restos Ancestrales</#803522>");
        piglin.setImmuneToZombification(true);
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!piglin.isValid() || piglin.isDead()) return;
            
            if (piglin.getAttribute(Attribute.MAX_HEALTH) != null) {
                piglin.getAttribute(Attribute.MAX_HEALTH).setBaseValue(60.0);
                piglin.setHealth(60.0);
            }
            
            EntityEquipment eq = piglin.getEquipment();
            if (eq != null) {
                Key modelKey = Key.key("panita", "fallen_hero");
                
                eq.setHelmet(applyFallenHero(new ItemStack(Material.DIAMOND_HELMET), modelKey));
                eq.setHelmetDropChance(0.0f);
                
                eq.setChestplate(applyFallenHero(new ItemStack(Material.DIAMOND_CHESTPLATE), modelKey));
                eq.setChestplateDropChance(0.0f);
                
                eq.setLeggings(applyFallenHero(new ItemStack(Material.DIAMOND_LEGGINGS), modelKey));
                eq.setLeggingsDropChance(0.0f);
                
                eq.setBoots(applyFallenHero(new ItemStack(Material.DIAMOND_BOOTS), modelKey));
                eq.setBootsDropChance(0.0f);
                
                ItemStack crossbow = new ItemStack(Material.CROSSBOW);
                eq.setItemInMainHand(crossbow);
                eq.setItemInMainHandDropChance(0.05f);
            }
        });
    }

    private ItemStack applyFallenHero(ItemStack item, Key modelKey) {
        try {
            Equippable comp = item.getData(DataComponentTypes.EQUIPPABLE);
            if (comp != null) {
                item.setData(DataComponentTypes.EQUIPPABLE, comp.toBuilder().assetId(modelKey).build());
            }
        } catch (Exception ignored) {}
        return item;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPiglinSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        LivingEntity entity = event.getEntity();
        if (event.getEntityType() == EntityType.PIGLIN) {
            if (EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) {
                // 10% chance
                if (random.nextDouble() <= 0.10) {
                    event.setCancelled(true);
                    spawnManual(entity.getLocation());
                }
            }
        }
    }
}

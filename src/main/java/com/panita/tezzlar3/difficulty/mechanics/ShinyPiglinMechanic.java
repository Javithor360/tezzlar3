package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.util.ItemUtils;
import com.panita.tezzlar3.core.util.MobGearUtils;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import org.bukkit.Location;

import java.util.Random;

public class ShinyPiglinMechanic extends DifficultyMechanic {

    private final Random random = new Random();
    private final NamespacedKey SHINY_KEY;

    public ShinyPiglinMechanic(JavaPlugin plugin) {
        super(plugin, 7);
        this.SHINY_KEY = new NamespacedKey(plugin, "is_shiny");
        CustomMobManager.register(CustomMobType.SHINY_PIGLIN, this::spawnManual);
    }

    public void spawnManual(Location loc) {
        Piglin piglin = loc.getWorld().spawn(loc, Piglin.class);
        piglin.setAdult(); 
        makeShiny(piglin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNetherMobSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;

        LivingEntity entity = event.getEntity();
        
        // Prevent infinite loops when we spawn the Shiny Piglin manually
        if (entity.getPersistentDataContainer().has(SHINY_KEY, PersistentDataType.BYTE)) {
            return;
        }
        
        EntityType type = entity.getType();

        // Only Piglins, Zombified Piglins, and Endermen in the Nether
        if (entity.getWorld().getEnvironment() != World.Environment.NETHER) return;
        if (type != EntityType.PIGLIN && type != EntityType.ZOMBIFIED_PIGLIN && type != EntityType.ENDERMAN) return;

        // 3% chance to become a Shiny Piglin
        if (random.nextDouble() < 0.01) {
            // Cancel the original spawn
            event.setCancelled(true);
            spawnManual(entity.getLocation());
        }
    }

    private void makeShiny(LivingEntity entity) {
        entity.getPersistentDataContainer().set(SHINY_KEY, PersistentDataType.BYTE, (byte) 1);
        
        // Custom Name (visible = false by default in the utility method)
        EntityUtils.setCustomName(entity, "&6Piglin Shiny");

        // Health (100 HP)
        AttributeInstance health = entity.getAttribute(Attribute.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(100.0);
            entity.setHealth(100.0);
        }

        // Base Armor (50)
        AttributeInstance armor = entity.getAttribute(Attribute.ARMOR);
        if (armor != null) {
            armor.setBaseValue(50.0);
        }

        // Triple Base Damage
        AttributeInstance damage = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(damage.getBaseValue() * 3.0);
        }

        // Force Equip Netherite Axe (with 0% drop chance)
        EntityEquipment eq = entity.getEquipment();
        if (eq != null) {
            ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
            axe = MobGearUtils.applyRandomEnchantments(axe, MobGearUtils.getTier(TimeManager.getCurrentDay()));
            eq.setItemInMainHand(axe);
            eq.setItemInMainHandDropChance(0.0f);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onShinyDeath(EntityDeathEvent event) {
        if (!isActive()) return;

        LivingEntity entity = event.getEntity();
        if (entity.getPersistentDataContainer().has(SHINY_KEY, PersistentDataType.BYTE)) {
            // Clear the default drops of the original mob
            event.getDrops().clear();

            Player killer = entity.getKiller();
            // Get looting bonus which already factors in the enchantment level and randomness
            int lootingBonus = killer != null ? ItemUtils.getLootingBonus(killer) : 0; 

            // Calculate and drop Raw Gold (Base: 8 to 32)
            int rawGoldAmount = 8 + random.nextInt(25) + (lootingBonus * 8);
            while (rawGoldAmount > 0) {
                int stackSize = Math.min(64, rawGoldAmount);
                event.getDrops().add(new ItemStack(Material.RAW_GOLD, stackSize));
                rawGoldAmount -= stackSize;
            }

            // Calculate and drop Gold Nuggets (Base: 32 to 64)
            int nuggetAmount = 32 + random.nextInt(33) + (lootingBonus * 16);
            while (nuggetAmount > 0) {
                int stackSize = Math.min(64, nuggetAmount);
                event.getDrops().add(new ItemStack(Material.GOLD_NUGGET, stackSize));
                nuggetAmount -= stackSize;
            }
        }
    }
}

package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.ItemUtils;
import com.panita.tezzlar3.core.util.MobGearUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class ShinyPiglinMechanic extends DifficultyMechanic {

    private final Random random = new Random();
    private final NamespacedKey SHINY_KEY;

    public ShinyPiglinMechanic(JavaPlugin plugin) {
        super(plugin, 7);
        this.SHINY_KEY = new NamespacedKey(plugin, "is_shiny");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNetherMobSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;

        LivingEntity entity = event.getEntity();
        EntityType type = entity.getType();

        // Only Piglins, Zombified Piglins, and Endermen in the Nether
        if (entity.getWorld().getEnvironment() != World.Environment.NETHER) return;
        if (type != EntityType.PIGLIN && type != EntityType.ZOMBIFIED_PIGLIN && type != EntityType.ENDERMAN) return;

        // 3% chance to become a Shiny Piglin
        if (random.nextDouble() < 0.03) {
            // Apply 1 tick delay to override vanilla spawning properly
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!entity.isValid() || entity.isDead()) return;
                makeShiny(entity);
            });
        }
    }

    private void makeShiny(LivingEntity entity) {
        entity.getPersistentDataContainer().set(SHINY_KEY, PersistentDataType.BYTE, (byte) 1);
        
        // Custom Name
        entity.customName(Messenger.mini("&6Piglin Shiny"));
        entity.setCustomNameVisible(true);

        // Triple Base Damage
        AttributeInstance damage = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(damage.getBaseValue() * 3.0);
        }

        // Force Equip Netherite Axe (with 0% drop chance)
        EntityEquipment eq = entity.getEquipment();
        if (eq != null) {
            ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
            axe = MobGearUtils.applyRandomEnchantments(axe);
            
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

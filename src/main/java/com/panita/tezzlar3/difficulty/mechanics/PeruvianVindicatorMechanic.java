package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Vindicator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class PeruvianVindicatorMechanic extends DifficultyMechanic {

    private final Random random = new Random();
    private final NamespacedKey PERUVIAN_KEY;
    private final Material[] CARPETS = {
            Material.WHITE_CARPET, Material.ORANGE_CARPET, Material.MAGENTA_CARPET,
            Material.LIGHT_BLUE_CARPET, Material.YELLOW_CARPET, Material.LIME_CARPET,
            Material.PINK_CARPET, Material.GRAY_CARPET, Material.LIGHT_GRAY_CARPET,
            Material.CYAN_CARPET, Material.PURPLE_CARPET, Material.BLUE_CARPET,
            Material.BROWN_CARPET, Material.GREEN_CARPET, Material.RED_CARPET, Material.BLACK_CARPET
    };

    public PeruvianVindicatorMechanic(JavaPlugin plugin) {
        super(plugin, 13);
        PERUVIAN_KEY = new NamespacedKey(plugin, "is_peruvian");
    }

    private Material getRandomCarpet() {
        return CARPETS[random.nextInt(CARPETS.length)];
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMonsterSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Monster)) return;
        
        // Prevent overwriting existing custom mobs
        if (EntityUtils.isCustomMob(entity)) return;
        
        // Prevent infinite loops
        if (entity instanceof Vindicator vindicator && vindicator.getPersistentDataContainer().has(PERUVIAN_KEY, PersistentDataType.BYTE)) return;

        if (EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) {
            World.Environment env = entity.getWorld().getEnvironment();
            if (env == World.Environment.NORMAL || env == World.Environment.THE_END) {
                
                // 8% chance to spawn
                if (random.nextDouble() < 0.08) {
                    entity.getWorld().spawn(entity.getLocation(), Llama.class, llama -> {
                        // Decorate and prepare llama
                        llama.getPersistentDataContainer().set(PERUVIAN_KEY, PersistentDataType.BYTE, (byte) 1);
                        llama.setAdult();
                        llama.setTamed(true);
                        llama.getInventory().setDecor(new ItemStack(getRandomCarpet()));
                        
                        // Spawn Vindicator passenger
                        llama.getWorld().spawn(llama.getLocation(), Vindicator.class, vindicator -> {
                            vindicator.getPersistentDataContainer().set(PERUVIAN_KEY, PersistentDataType.BYTE, (byte) 1);
                            EntityUtils.setCustomName(vindicator, "<gradient:#E43434:#FFFFFF>Vindi</gradient><gradient:#FFFFFF:#FFFFFF>cator Pe</gradient><gradient:#FFFFFF:#E43434>ruano</gradient>");
                            
                            ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
                            axe.addUnsafeEnchantment(Enchantment.SHARPNESS, 6);
                            vindicator.getEquipment().setItemInMainHand(axe);
                            vindicator.getEquipment().setItemInMainHandDropChance(0.3f);
                            
                            llama.addPassenger(vindicator);
                            
                            EntityUtils.trySetAttribute(vindicator, Attribute.MAX_HEALTH, 35.0);
                            vindicator.setHealth(35.0);
                        });
                    });
                    
                    event.setCancelled(true);
                }
            }
        }
    }
}

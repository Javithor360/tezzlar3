package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.qol.util.CustomItemManager;
import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Ravager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TotemRavagerMechanic extends DifficultyMechanic {

    private final NamespacedKey TOTEM_RAVAGER_KEY;
    private final NamespacedKey TOTEM_RAVAGER_TOP_KEY;

    private final List<String> TOTEM_IDS = Arrays.asList(
            "axolotl_totem", "fish_totem", "sulfur_totem", "tadpole_totem",
            "ghast_totem", "copper_totem", "bee_totem", "chicken_totem",
            "sniffer_totem", "turtle_totem", "golden_totem"
    );

    public TotemRavagerMechanic(JavaPlugin plugin) {
        super(plugin, 28);
        this.TOTEM_RAVAGER_KEY = new NamespacedKey(plugin, "totem_ravager");
        this.TOTEM_RAVAGER_TOP_KEY = new NamespacedKey(plugin, "totem_ravager_top");
        
        CustomMobManager.register(CustomMobType.TOTEM_RAVAGER, this::spawnManual);
    }
    
    public void spawnManual(Location loc) {
        spawnTotemRavager(loc);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (!(event.getEntity() instanceof Monster)) return;
        
        if (event.getEntity().getPersistentDataContainer().has(TOTEM_RAVAGER_KEY, PersistentDataType.BYTE)) return;

        if (EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) {
            // 0.01% chance
            if (ThreadLocalRandom.current().nextDouble() < 0.0001) {
                event.setCancelled(true);
                spawnTotemRavager(event.getLocation());
            }
        }
    }

    private void spawnTotemRavager(Location loc) {
        Ravager current = null;
        for (int i = 0; i < 5; i++) {
            Ravager ravager = (Ravager) loc.getWorld().spawnEntity(loc, EntityType.RAVAGER);
            ravager.getPersistentDataContainer().set(TOTEM_RAVAGER_KEY, PersistentDataType.BYTE, (byte) 1);
            
            // Set base health to 50
            EntityUtils.trySetAttribute(ravager, Attribute.MAX_HEALTH, 50.0);
            ravager.setHealth(50.0);
            
            // Apply elite buffs on top of 50 HP
            EliteMobStatsMechanic.applyEliteBuffs(ravager);
            
            EntityUtils.setCustomName(ravager, CustomMobType.TOTEM_RAVAGER.getCustomName());

            if (i == 4) { // Top ravager
                ravager.getPersistentDataContainer().set(TOTEM_RAVAGER_TOP_KEY, PersistentDataType.BYTE, (byte) 1);
            }

            if (current != null) {
                current.addPassenger(ravager);
            }
            current = ravager;
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            if (event.getEntity().getPersistentDataContainer().has(TOTEM_RAVAGER_TOP_KEY, PersistentDataType.BYTE)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(TOTEM_RAVAGER_TOP_KEY, PersistentDataType.BYTE)) {
            // Drop random totem (including vanilla)
            int randomIndex = ThreadLocalRandom.current().nextInt(TOTEM_IDS.size() + 1); // +1 for vanilla
            ItemStack drop;
            if (randomIndex == TOTEM_IDS.size()) {
                drop = new ItemStack(Material.TOTEM_OF_UNDYING);
            } else {
                drop = CustomItemManager.getItem(TOTEM_IDS.get(randomIndex));
                if (drop == null) {
                    drop = new ItemStack(Material.TOTEM_OF_UNDYING);
                }
            }
            event.getDrops().add(drop);
        }
    }
}

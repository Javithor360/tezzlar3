package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.util.ItemUtils;

import java.util.Random;

public class ZombieBeekeeperMechanic extends DifficultyMechanic {

    private final NamespacedKey BEEKEEPER_KEY;
    private final Random random = new Random();

    public ZombieBeekeeperMechanic(JavaPlugin plugin) {
        super(plugin, 3);
        BEEKEEPER_KEY = new NamespacedKey(plugin, "is_beekeeper");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onZombieSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Zombie zombie) {
            // Only natural, spawner, and spawn eggs
            if (EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) {
                
                // 10% chance to become a beekeeper
                if (random.nextDouble() < 0.10) {
                    zombie.getPersistentDataContainer().set(BEEKEEPER_KEY, PersistentDataType.BYTE, (byte) 1);
                    
                    // Set Custom Name
                    EntityUtils.setCustomName(zombie, "&eZombi Apicultor");
                    
                    // Health (30 HP)
                    AttributeInstance healthAttr = zombie.getAttribute(Attribute.MAX_HEALTH);
                    if (healthAttr != null) {
                        healthAttr.setBaseValue(30.0);
                        zombie.setHealth(30.0);
                    }
                    
                    // Double Attack Damage
                    AttributeInstance damageAttr = zombie.getAttribute(Attribute.ATTACK_DAMAGE);
                    if (damageAttr != null) {
                        damageAttr.setBaseValue(damageAttr.getBaseValue() * 2.0);
                    }
                    
                    EntityUtils.equipArmor(zombie, 
                        new ItemStack(Material.BEE_NEST), 
                        new ItemStack(Material.GOLDEN_CHESTPLATE), 
                        new ItemStack(Material.GOLDEN_LEGGINGS), 
                        new ItemStack(Material.GOLDEN_BOOTS), 
                        0.0f);
                        
                    EntityEquipment eq = zombie.getEquipment();
                    if (eq != null) {
                        eq.setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));
                        eq.setItemInMainHandDropChance(0.0f);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBeekeeperDamage(EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Zombie zombie) {
            if (zombie.getPersistentDataContainer().has(BEEKEEPER_KEY, PersistentDataType.BYTE)) {
                
                // Adjust enchantment damage specific to Beekeeper (Day 3 logic)
                if (event.getDamager() instanceof Player player) {
                    ItemStack weapon = player.getInventory().getItemInMainHand();
                    if (weapon.hasItemMeta()) {
                        
                        // If it has Sharpness or Smite, it deals ZERO damage
                        if (weapon.containsEnchantment(Enchantment.SHARPNESS) || 
                            weapon.containsEnchantment(Enchantment.SMITE)) {
                            event.setDamage(0);
                            return;
                        }
                        
                        double damageToAdd = 0.0;
                        
                        // Bane of Arthropods (DAMAGE_ARTHROPODS): 2.5 * level
                        if (weapon.containsEnchantment(Enchantment.BANE_OF_ARTHROPODS)) {
                            int level = weapon.getEnchantments().getOrDefault(Enchantment.BANE_OF_ARTHROPODS, 0);
                            damageToAdd += 2.5 * level;
                        }
                        
                        double finalDamage = event.getDamage() + damageToAdd;
                        event.setDamage(finalDamage);
                    }
                }

                // Spawn angry bee
                Bee bee = zombie.getWorld().spawn(zombie.getLocation().add(0, 1.5, 0), Bee.class);
                
                // Double bee health
                AttributeInstance beeHealthAttr = bee.getAttribute(Attribute.MAX_HEALTH);
                if (beeHealthAttr != null) {
                    beeHealthAttr.setBaseValue(beeHealthAttr.getBaseValue() * 2.0);
                    bee.setHealth(beeHealthAttr.getBaseValue());
                }
                
                Player target = null;
                if (event.getDamager() instanceof Player p) {
                    target = p;
                } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
                    target = p;
                }
                
                if (target != null) {
                    bee.setTarget(target);
                }
                
                bee.setAnger(600); // 30 seconds of anger
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBeekeeperDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Zombie zombie) {
            if (zombie.getPersistentDataContainer().has(BEEKEEPER_KEY, PersistentDataType.BYTE)) {
                // Drop 1 to 3 honey bottles
                int amount = random.nextInt(3) + 1;
                
                // Apply Looting
                Player killer = zombie.getKiller();
                amount += ItemUtils.getLootingBonus(killer);
                
                event.getDrops().add(new ItemStack(Material.HONEY_BOTTLE, amount));
            }
        }
    }
}

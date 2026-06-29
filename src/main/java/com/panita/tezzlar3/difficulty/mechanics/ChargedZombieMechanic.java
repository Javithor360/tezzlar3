package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.NamespacedKey;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import org.bukkit.Location;

public class ChargedZombieMechanic extends DifficultyMechanic {

    private final NamespacedKey CHARGED_KEY;

    public ChargedZombieMechanic(JavaPlugin plugin) {
        super(plugin, 17);
        this.CHARGED_KEY = new NamespacedKey(plugin, "is_charged_zombie");
        
        // Aura particles task
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (Entity entity : player.getNearbyEntities(32, 32, 32)) {
                    if (entity instanceof Zombie zombie && zombie.getPersistentDataContainer().has(CHARGED_KEY, PersistentDataType.BYTE)) {
                        // Spawn blue electric aura
                        player.spawnParticle(Particle.ELECTRIC_SPARK, zombie.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.05);
                    }
                }
            }
        }, 10L, 10L); // Twice per second
        
        CustomMobManager.register(CustomMobType.CHARGED_ZOMBIE, this::spawnManual);
    }

    public void spawnManual(Location loc) {
        Zombie zombie = (Zombie) EntityUtils.spawnNatural(loc, EntityType.ZOMBIE);
        transform(zombie);
    }

    private void transform(Zombie zombie) {
        // Set custom stats
        EntityUtils.trySetAttribute(zombie, Attribute.MAX_HEALTH, 40.0);
        zombie.setHealth(40.0);
        EntityUtils.trySetAttribute(zombie, Attribute.ARMOR, 12.0);
        EntityUtils.trySetAttribute(zombie, Attribute.ATTACK_DAMAGE, 8.0);
        
        // Set visuals
        zombie.setInvisible(true);
        EntityUtils.equipArmor(zombie, new ItemStack(Material.DIAMOND_HELMET), null, null, null, 0.0f);
        
        // Add custom identifier
        zombie.getPersistentDataContainer().set(CHARGED_KEY, PersistentDataType.BYTE, (byte) 1);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onZombieSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;
        
        if (event.getEntityType() == EntityType.ZOMBIE) {
            if (EntityUtils.isCustomMob(event.getEntity())) return;
            
            // 3% probability
            if (Math.random() < 0.03) {
                Zombie zombie = (Zombie) event.getEntity();
                transform(zombie);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        
        if (event.getDamager() instanceof Zombie zombie && event.getEntity() instanceof Player player) {
            if (zombie.getPersistentDataContainer().has(CHARGED_KEY, PersistentDataType.BYTE)) {
                // 25% chance of slowness
                if (Math.random() < 0.25) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 3)); // 200 ticks = 10s, amp 3 = IV
                }
            }
        }
    }
}

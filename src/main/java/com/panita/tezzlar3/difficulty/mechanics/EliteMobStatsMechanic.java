package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.util.EntityUtils;

public class EliteMobStatsMechanic extends DifficultyMechanic {

    private final NamespacedKey ELITE_KEY;

    public EliteMobStatsMechanic(JavaPlugin plugin) {
        super(plugin, 11);
        ELITE_KEY = new NamespacedKey(plugin, "elite_buffed");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return; // Never apply to players
        
        // Prevent infinite duplications (e.g. slimes splitting)
        if (entity.getPersistentDataContainer().has(ELITE_KEY, PersistentDataType.BYTE)) return;
        
        // Delay 1 tick to allow custom mobs (like Beekeeper or Realistic Spiders) 
        // to modify their base health and damage first.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!entity.isValid() || entity.isDead()) return;
            
            // Skip the Giga Magma Cube boss
            if (entity.getPersistentDataContainer().has(GigaMagmaCubeMechanic.BOSS_KEY, PersistentDataType.BYTE)) {
                return;
            }
            
            // Mark as buffed
            entity.getPersistentDataContainer().set(ELITE_KEY, PersistentDataType.BYTE, (byte) 1);
            
            // Double Health
            AttributeInstance healthAttr = entity.getAttribute(Attribute.MAX_HEALTH);
            if (healthAttr != null) {
                double newHealth = healthAttr.getBaseValue() * 2.0;
                EntityUtils.trySetAttribute(entity, Attribute.MAX_HEALTH, newHealth);
                try {
                    entity.setHealth(entity.getAttribute(Attribute.MAX_HEALTH).getValue());
                } catch (Exception ignored) {}
            }
            
            // Double Damage
            AttributeInstance damageAttr = entity.getAttribute(Attribute.ATTACK_DAMAGE);
            if (damageAttr != null) {
                EntityUtils.trySetAttribute(entity, Attribute.ATTACK_DAMAGE, damageAttr.getBaseValue() * 2.0);
            }
        });
    }
}

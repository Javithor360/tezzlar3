package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.timeline.util.TimeManager;

public class SpawnerMobBuffMechanic extends DifficultyMechanic {

    private final NamespacedKey SPAWNER_BUFF_KEY;

    public SpawnerMobBuffMechanic(JavaPlugin plugin) {
        super(plugin, 16);
        SPAWNER_BUFF_KEY = new NamespacedKey(plugin, "spawner_buffed");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawnerMob(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        int day = TimeManager.getCurrentDay();
        if (day >= 19) return; // Deactivate from day 19 onwards
        
        if (event.getSpawnReason() != SpawnReason.SPAWNER) return;
        
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return;
        
        if (entity.getPersistentDataContainer().has(SPAWNER_BUFF_KEY, PersistentDataType.BYTE)) return;
        
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!entity.isValid() || entity.isDead()) return;
            
            entity.getPersistentDataContainer().set(SPAWNER_BUFF_KEY, PersistentDataType.BYTE, (byte) 1);
            
            // x3 Health
            AttributeInstance healthAttr = entity.getAttribute(Attribute.MAX_HEALTH);
            if (healthAttr != null) {
                double newHealth = healthAttr.getBaseValue() * 3.0;
                EntityUtils.trySetAttribute(entity, Attribute.MAX_HEALTH, newHealth);
                try {
                    entity.setHealth(entity.getAttribute(Attribute.MAX_HEALTH).getValue());
                } catch (Exception ignored) {}
            }
            
            // x3 Attack
            AttributeInstance damageAttr = entity.getAttribute(Attribute.ATTACK_DAMAGE);
            if (damageAttr != null) {
                EntityUtils.trySetAttribute(entity, Attribute.ATTACK_DAMAGE, damageAttr.getBaseValue() * 3.0);
            }
            
            // x4 Armor
            AttributeInstance armorAttr = entity.getAttribute(Attribute.ARMOR);
            if (armorAttr != null) {
                double baseArmor = armorAttr.getBaseValue();
                if (baseArmor == 0) {
                    baseArmor = 2.0; // If it doesn't have natural armor, give it a base of 2.0 before multiplying
                }
                EntityUtils.trySetAttribute(entity, Attribute.ARMOR, baseArmor * 4.0);
            } else {
                // Register attribute if it doesn't have it (e.g. creepers or spiders)
                EntityUtils.trySetAttribute(entity, Attribute.ARMOR, 8.0); // 2.0 * 4 = 8.0
            }
        });
    }
}

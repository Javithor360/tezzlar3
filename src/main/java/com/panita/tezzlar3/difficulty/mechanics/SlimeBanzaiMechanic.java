package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class SlimeBanzaiMechanic extends DifficultyMechanic {

    private final Random random = new Random();
    private final NamespacedKey BANZAI_KEY;

    public SlimeBanzaiMechanic(JavaPlugin plugin) {
        super(plugin, 12);
        BANZAI_KEY = new NamespacedKey(plugin, "is_banzai");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMonsterSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Monster)) return;
        
        // Prevent infinite loops and overwriting other custom mobs
        if (EntityUtils.isCustomMob(entity)) return;
        if (entity instanceof Slime slime && slime.getPersistentDataContainer().has(BANZAI_KEY, PersistentDataType.BYTE)) return;

        if (EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) {
            World.Environment env = entity.getWorld().getEnvironment();
            if (env == World.Environment.NORMAL || env == World.Environment.THE_END) {
                
                // 1% chance to replace any monster with a Slime Banzai
                if (random.nextDouble() < 0.01) {
                    entity.getWorld().spawn(entity.getLocation(), Slime.class, slime -> {
                        slime.setSize(4);
                        slime.getPersistentDataContainer().set(BANZAI_KEY, PersistentDataType.BYTE, (byte) 1);
                        EntityUtils.setCustomName(slime, "<#7EF7B6>Slime Banzai</#7EF7B6>");
                        
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (!slime.isValid() || slime.isDead()) return;
                            
                            EntityUtils.trySetAttribute(slime, Attribute.BOUNCINESS, 40.0);
                            EntityUtils.trySetAttribute(slime, Attribute.JUMP_STRENGTH, 3.0);
                            EntityUtils.trySetAttribute(slime, Attribute.SAFE_FALL_DISTANCE, 40.0);
                        });
                    });
                    event.setCancelled(true);
                }
            }
        }
    }
}

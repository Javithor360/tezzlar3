package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class RealisticSpiderMechanic extends DifficultyMechanic {

    private final NamespacedKey REALISTIC_KEY;
    private final Random random = new Random();

    public RealisticSpiderMechanic(JavaPlugin plugin) {
        super(plugin, 5);
        REALISTIC_KEY = new NamespacedKey(plugin, "is_realistic");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpiderSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        // Target only normal spiders (ignore CaveSpiders as requested)
        if (event.getEntity() instanceof Spider spider && !(event.getEntity() instanceof CaveSpider)) {
            
            // Avoid infinite loops when we spawn the extra spiders
            if (spider.getPersistentDataContainer().has(REALISTIC_KEY, PersistentDataType.BYTE)) {
                return;
            }
            
            if (EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) {
                
                // Modify the original spider
                makeRealistic(spider);
                
                // Spawn 4 to 9 additional spiders to make groups of 5 to 10
                int extraSpiders = random.nextInt(6) + 4; 
                
                for (int i = 0; i < extraSpiders; i++) {
                    spider.getWorld().spawn(spider.getLocation(), Spider.class, newSpider -> {
                        makeRealistic(newSpider);
                    });
                }
            }
        }
    }
    
    private void makeRealistic(Spider spider) {
        spider.getPersistentDataContainer().set(REALISTIC_KEY, PersistentDataType.BYTE, (byte) 1);
        
        // Custom Name
        spider.customName(Messenger.mini("&cAraña Realista"));
        spider.setCustomNameVisible(false);
        
        // Delay logic by 3 seconds (60 ticks) to overwrite any other datapacks/plugins modifying attributes on spawn
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!spider.isValid() || spider.isDead()) return;
            
            // Scale to half size
            if (spider.getAttribute(Attribute.SCALE) != null) {
                spider.getAttribute(Attribute.SCALE).setBaseValue(0.5);
            }
            
            // Reduce Health
            if (spider.getAttribute(Attribute.MAX_HEALTH) != null) {
                spider.getAttribute(Attribute.MAX_HEALTH).setBaseValue(8.0);
                if (spider.getHealth() > 8.0) {
                    spider.setHealth(8.0);
                }
            }
            
            // Increase Speed
            if (spider.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                spider.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.4);
            }
        }, 60L); // 3 seconds delay
    }
}

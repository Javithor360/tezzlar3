package com.panita.tezzlar3.difficulty.mobs;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

public class CustomMobManager {

    public interface CustomMobSpawner {
        void spawnManual(Location loc);
    }

    private static final Map<CustomMobType, CustomMobSpawner> spawners = new HashMap<>();
    
    public static void register(CustomMobType type, CustomMobSpawner spawner) {
        spawners.put(type, spawner);
    }

    public static void spawn(CustomMobType type, Location loc) {
        if (spawners.containsKey(type)) {
            EntityUtils.setForceSpawnReason(CreatureSpawnEvent.SpawnReason.CUSTOM);
            try {
                spawners.get(type).spawnManual(loc);
            } finally {
                EntityUtils.clearForceSpawnReason();
            }
        }
    }

    public static void tagCustomMob(LivingEntity entity, CustomMobType type) {
        NamespacedKey key = new NamespacedKey(Tezzlar.getInstance(), "custom_mob_id");
        entity.getPersistentDataContainer().set(key, PersistentDataType.STRING, type.name().toLowerCase());
    }
}

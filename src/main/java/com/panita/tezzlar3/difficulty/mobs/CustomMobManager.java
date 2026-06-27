package com.panita.tezzlar3.difficulty.mobs;

import org.bukkit.Location;

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
            spawners.get(type).spawnManual(loc);
        }
    }
}

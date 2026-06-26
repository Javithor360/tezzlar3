package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class VillagerProfessionLossMechanic extends DifficultyMechanic {

    public VillagerProfessionLossMechanic(JavaPlugin plugin) {
        super(plugin, 10); // Day 10
        
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            for (World world : Bukkit.getWorlds()) {
                for (Villager villager : world.getEntitiesByClass(Villager.class)) {
                    if (villager.getProfession() != Villager.Profession.NONE && villager.getProfession() != Villager.Profession.NITWIT) {
                        villager.setProfession(Villager.Profession.NONE);
                    }
                }
            }
        }, 100L, 200L); // Checks every 10s for already loaded ones
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!isActive()) return;
        
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Villager villager) {
                if (villager.getProfession() != Villager.Profession.NONE) {
                    villager.setProfession(Villager.Profession.NONE);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAcquireProfession(VillagerCareerChangeEvent event) {
        if (!isActive()) return;
        
        event.setCancelled(true);
    }
}

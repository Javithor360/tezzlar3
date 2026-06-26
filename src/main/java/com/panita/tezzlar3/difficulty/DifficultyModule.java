package com.panita.tezzlar3.difficulty;

import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.difficulty.mechanics.AngryWolfMechanic;
import com.panita.tezzlar3.difficulty.mechanics.AnimalOneShotMechanic;
import com.panita.tezzlar3.difficulty.mechanics.AcidRainMechanic;
import com.panita.tezzlar3.difficulty.mechanics.DifficultyMechanic;
import com.panita.tezzlar3.difficulty.mechanics.DoubleMobCapMechanic;
import com.panita.tezzlar3.difficulty.mechanics.DoubleFallDamageMechanic;
import com.panita.tezzlar3.difficulty.mechanics.FastDrowningMechanic;
import com.panita.tezzlar3.difficulty.mechanics.GigaMagmaCubeMechanic;
import com.panita.tezzlar3.difficulty.mechanics.GoatHornParalyzeMechanic;
import com.panita.tezzlar3.difficulty.mechanics.InfraredSkeletonMechanic;
import com.panita.tezzlar3.difficulty.mechanics.OverworldBedExplosionMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ParasiticSilverfishMechanic;
import com.panita.tezzlar3.difficulty.mechanics.PremiumArmorSetMechanic;
import com.panita.tezzlar3.difficulty.mechanics.LightningSkeletonMechanic;
import com.panita.tezzlar3.difficulty.mechanics.NaturalRavagerMechanic;
import com.panita.tezzlar3.difficulty.mechanics.PeacefulBiomeSpawnsMechanic;
import com.panita.tezzlar3.difficulty.mechanics.PlayerKillOnlyDropsMechanic;
import com.panita.tezzlar3.difficulty.mechanics.RandomMobGearMechanic;
import com.panita.tezzlar3.difficulty.mechanics.RandomMobSizeMechanic;
import com.panita.tezzlar3.difficulty.mechanics.RealisticSpiderMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ShinyPiglinMechanic;
import com.panita.tezzlar3.difficulty.mechanics.VillagerProfessionLossMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ZombieBeekeeperMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ZombieCavalryMechanic;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class DifficultyModule implements PluginModule {
    private boolean enabled;
    public static final String PACKAGE_NAME = "com.panita.tezzlar3.difficulty";

    @Override
    public String id() {
        return "difficulty";
    }

    @Override
    public String basePackage() {
        return PACKAGE_NAME;
    }

    @Override
    public void onEnable(JavaPlugin plugin) {
        List<DifficultyMechanic> mechanics = new ArrayList<>();
        
        // Day 1
        mechanics.add(new PremiumArmorSetMechanic(plugin));
        
        // Day 3
        mechanics.add(new DoubleFallDamageMechanic(plugin));
        mechanics.add(new AnimalOneShotMechanic(plugin));
        mechanics.add(new ZombieBeekeeperMechanic(plugin));
        
        // Day 4
        mechanics.add(new GoatHornParalyzeMechanic(plugin));
        mechanics.add(new InfraredSkeletonMechanic(plugin));
        mechanics.add(new RandomMobSizeMechanic(plugin));
        
        // Day 5
        mechanics.add(new OverworldBedExplosionMechanic(plugin));
        mechanics.add(new RandomMobGearMechanic(plugin));
        mechanics.add(new RealisticSpiderMechanic(plugin));
        
        // Day 6
        mechanics.add(new ZombieCavalryMechanic(plugin));
        mechanics.add(new ParasiticSilverfishMechanic(plugin));

        // Day 7
        mechanics.add(new ShinyPiglinMechanic(plugin));
        mechanics.add(new AngryWolfMechanic(plugin));
        
        // Day 8
        mechanics.add(new LightningSkeletonMechanic(plugin));
        mechanics.add(new FastDrowningMechanic(plugin));
        mechanics.add(new AcidRainMechanic(plugin));
        
        // Day 9
        mechanics.add(new NaturalRavagerMechanic(plugin));
        mechanics.add(new DoubleMobCapMechanic(plugin));
        
        // Day 10
        mechanics.add(new PlayerKillOnlyDropsMechanic(plugin));
        mechanics.add(new PeacefulBiomeSpawnsMechanic(plugin));
        mechanics.add(new VillagerProfessionLossMechanic(plugin));
        mechanics.add(new GigaMagmaCubeMechanic(plugin));
        
        for (DifficultyMechanic mechanic : mechanics) {
            plugin.getServer().getPluginManager().registerEvents(mechanic, plugin);
        }
        
        enabled = true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean value) {
        this.enabled = value;
    }
}

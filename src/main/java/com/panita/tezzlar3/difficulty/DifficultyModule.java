package com.panita.tezzlar3.difficulty;

import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.difficulty.mechanics.AngryWolfMechanic;
import com.panita.tezzlar3.difficulty.mechanics.SuffocationImmunityMechanic;
import com.panita.tezzlar3.difficulty.mechanics.AnimalOneShotMechanic;
import com.panita.tezzlar3.difficulty.mechanics.AcidRainMechanic;
import com.panita.tezzlar3.difficulty.mechanics.CreeperExplosionMechanic;
import com.panita.tezzlar3.difficulty.mechanics.GhastExplosionMechanic;
import com.panita.tezzlar3.difficulty.mechanics.NightRainMechanic;
import com.panita.tezzlar3.difficulty.mechanics.DifficultyMechanic;
import com.panita.tezzlar3.difficulty.mechanics.DeathTrainMechanic;
import com.panita.tezzlar3.difficulty.mechanics.DoubleFallDamageMechanic;
import com.panita.tezzlar3.difficulty.mechanics.EliteMobStatsMechanic;
import com.panita.tezzlar3.difficulty.mechanics.FastDrowningMechanic;
import com.panita.tezzlar3.difficulty.mechanics.FastStriderMechanic;
import com.panita.tezzlar3.difficulty.mechanics.GigaMagmaCubeMechanic;
import com.panita.tezzlar3.difficulty.mechanics.GoatHornParalyzeMechanic;
import com.panita.tezzlar3.difficulty.mechanics.InfraredSkeletonMechanic;
import com.panita.tezzlar3.difficulty.mechanics.LavaDepthStriderMechanic;
import com.panita.tezzlar3.difficulty.mechanics.NetherBlazeSpawnsMechanic;
import com.panita.tezzlar3.difficulty.mechanics.OverworldBedExplosionMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ParasiticSilverfishMechanic;
import com.panita.tezzlar3.difficulty.mechanics.PhantomRideMechanic;
import com.panita.tezzlar3.difficulty.mechanics.LightningSkeletonMechanic;
import com.panita.tezzlar3.difficulty.mechanics.NaturalRavagerMechanic;
import com.panita.tezzlar3.difficulty.mechanics.PlayerKillOnlyDropsMechanic;
import com.panita.tezzlar3.difficulty.mechanics.PremiumArmorSetMechanic;
import com.panita.tezzlar3.difficulty.mechanics.CopperDamageMechanic;
import com.panita.tezzlar3.difficulty.mechanics.RandomMobGearMechanic;
import com.panita.tezzlar3.difficulty.mechanics.RandomMobSizeMechanic;
import com.panita.tezzlar3.difficulty.mechanics.RealisticSpiderMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ShinyPiglinMechanic;
import com.panita.tezzlar3.difficulty.mechanics.VillagerProfessionLossMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ZombieBeekeeperMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ZombieCavalryMechanic;
import com.panita.tezzlar3.difficulty.mechanics.FastHungerMechanic;
import com.panita.tezzlar3.difficulty.mechanics.SlimeBanzaiMechanic;
import com.panita.tezzlar3.difficulty.mechanics.InvisibleChargedCreeperMechanic;
import com.panita.tezzlar3.difficulty.mechanics.CopperGolemLightningMechanic;
import com.panita.tezzlar3.difficulty.mechanics.WeavingSpiderMechanic;
import com.panita.tezzlar3.difficulty.mechanics.BuffedSnowGolemMechanic;
import com.panita.tezzlar3.difficulty.mechanics.PeruvianVindicatorMechanic;
import com.panita.tezzlar3.difficulty.mechanics.EternalFireMechanic;
import com.panita.tezzlar3.difficulty.mechanics.StrayFreezeMechanic;
import com.panita.tezzlar3.difficulty.mechanics.GlobalVariantsMechanic;
import com.panita.tezzlar3.difficulty.mechanics.WitherSkeletonEliteGearMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ToxicMeatMechanic;
import com.panita.tezzlar3.difficulty.mechanics.MutatedSpiderMechanic;
import com.panita.tezzlar3.difficulty.mechanics.EternalNightMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ZombieRatatouilleMechanic;
import com.panita.tezzlar3.difficulty.mechanics.IllagerGuardianMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ElderGuardianMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ExplosiveTotemMechanic;
import com.panita.tezzlar3.difficulty.mechanics.NetherRoofWitherMechanic;
import com.panita.tezzlar3.difficulty.mechanics.BabyMobRiderMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ChargedZombieMechanic;
import com.panita.tezzlar3.difficulty.mechanics.TeleportingSpawnsMechanic;
import com.panita.tezzlar3.difficulty.mechanics.BabyKillCurseMechanic;
import com.panita.tezzlar3.difficulty.mechanics.VehicleDismountMechanic;
import com.panita.tezzlar3.difficulty.mechanics.DoubleDamageMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ApocalypticZombieMechanic;
import com.panita.tezzlar3.difficulty.mechanics.OverworldToxicityMechanic;
import com.panita.tezzlar3.difficulty.mechanics.EnderGuardianMechanic;
import com.panita.tezzlar3.difficulty.mechanics.SpawnerMobBuffMechanic;
import com.panita.tezzlar3.difficulty.mechanics.MagmaDamageMechanic;
import com.panita.tezzlar3.difficulty.mechanics.DoubledArrowDamageMechanic;
import com.panita.tezzlar3.difficulty.mechanics.DoubleTotemMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ZombieCameramanMechanic;
import com.panita.tezzlar3.difficulty.mechanics.WrongToolDamageMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ColoredShulkerMechanic;
import com.panita.tezzlar3.difficulty.mechanics.DoubleFireLavaDamageMechanic;
import com.panita.tezzlar3.difficulty.mechanics.NetherOverworldSpawnsMechanic;
import com.panita.tezzlar3.difficulty.mechanics.PhantomRiderMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ZoglinNetherSpawnsMechanic;
import com.panita.tezzlar3.difficulty.mechanics.VampireBatMechanic;
import com.panita.tezzlar3.difficulty.mechanics.EndermanNerfMechanic;
import com.panita.tezzlar3.difficulty.mechanics.GhastDeflectOnlyMechanic;
import com.panita.tezzlar3.difficulty.mechanics.AncestralRemainsMechanic;
import com.panita.tezzlar3.difficulty.mechanics.CropGrowthMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ElytraNerfMechanic;
import com.panita.tezzlar3.difficulty.mechanics.PiglinDJMechanic;
import com.panita.tezzlar3.difficulty.mechanics.PyromaniacPiglinMechanic;
import com.panita.tezzlar3.difficulty.mechanics.HourlyPotionMechanic;
import com.panita.tezzlar3.difficulty.mechanics.ArabWanderingTraderMechanic;
import com.panita.tezzlar3.difficulty.mechanics.SpawnerRegenMechanic;
import com.panita.tezzlar3.difficulty.tasks.SpawnerRegenTask;
import com.panita.tezzlar3.difficulty.util.SpawnerRegenManager;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class DifficultyModule implements PluginModule {
    private boolean enabled;
    public static final String PACKAGE_NAME = "com.panita.tezzlar3.difficulty";
    
    private final List<DifficultyMechanic> mechanics = new ArrayList<>();
    private boolean initialized = false;
    
    private SpawnerRegenManager spawnerRegenManager;
    private SpawnerRegenTask spawnerRegenTask;

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
        if (!initialized) {
            spawnerRegenManager = new SpawnerRegenManager();
            
            // Day 1
            mechanics.add(new PremiumArmorSetMechanic(plugin));
        
            // Day 2
            mechanics.add(new DeathTrainMechanic(plugin));

            // Day 3
            mechanics.add(new DoubleFallDamageMechanic(plugin));
            mechanics.add(new AnimalOneShotMechanic(plugin));
            mechanics.add(new ZombieBeekeeperMechanic(plugin));
            mechanics.add(new SuffocationImmunityMechanic(plugin));

            // Day 4
            mechanics.add(new GoatHornParalyzeMechanic(plugin));
            mechanics.add(new InfraredSkeletonMechanic(plugin));
            mechanics.add(new RandomMobSizeMechanic(plugin));

            // Day 5
            mechanics.add(new OverworldBedExplosionMechanic(plugin));
            mechanics.add(new RandomMobGearMechanic(plugin));
            mechanics.add(new RealisticSpiderMechanic(plugin));
            mechanics.add(new PhantomRideMechanic(plugin));

            // Day 6
            mechanics.add(new ZombieCavalryMechanic(plugin));
            mechanics.add(new ParasiticSilverfishMechanic(plugin));

            // Day 7
            mechanics.add(new AngryWolfMechanic(plugin));
            mechanics.add(new ShinyPiglinMechanic(plugin));
            mechanics.add(new CreeperExplosionMechanic(plugin));

            // Day 8
            mechanics.add(new LightningSkeletonMechanic(plugin));
            mechanics.add(new FastDrowningMechanic(plugin));
            mechanics.add(new AcidRainMechanic(plugin));
            mechanics.add(new GhastExplosionMechanic(plugin));

            // Day 9
            mechanics.add(new NaturalRavagerMechanic(plugin));
            mechanics.add(new FastStriderMechanic(plugin));
            mechanics.add(new LavaDepthStriderMechanic(plugin));

            // Day 10
            mechanics.add(new PlayerKillOnlyDropsMechanic(plugin));
            mechanics.add(new VillagerProfessionLossMechanic(plugin));
            mechanics.add(new GigaMagmaCubeMechanic(plugin));
            mechanics.add(new DoubledArrowDamageMechanic(plugin));

            // Day 11
            mechanics.add(new NightRainMechanic(plugin));
            mechanics.add(new NetherBlazeSpawnsMechanic(plugin));
            mechanics.add(new CopperDamageMechanic(plugin));
            mechanics.add(new EliteMobStatsMechanic(plugin));
            mechanics.add(new SpawnerRegenMechanic(plugin, spawnerRegenManager));

            // Day 12
            mechanics.add(new FastHungerMechanic(plugin));
            mechanics.add(new SlimeBanzaiMechanic(plugin));
            mechanics.add(new InvisibleChargedCreeperMechanic(plugin));
            mechanics.add(new CopperGolemLightningMechanic(plugin));
            mechanics.add(new MagmaDamageMechanic(plugin));
            mechanics.add(new CropGrowthMechanic(plugin));

            // Day 13
            mechanics.add(new SlimeBanzaiMechanic(plugin));
            mechanics.add(new InvisibleChargedCreeperMechanic(plugin));
            mechanics.add(new CopperGolemLightningMechanic(plugin));
            mechanics.add(new MagmaDamageMechanic(plugin));

            // Day 13
            mechanics.add(new WeavingSpiderMechanic(plugin));
            mechanics.add(new BuffedSnowGolemMechanic(plugin));
            mechanics.add(new PeruvianVindicatorMechanic(plugin));

            // Day 14
            mechanics.add(new EternalFireMechanic(plugin));
            mechanics.add(new StrayFreezeMechanic(plugin));
            mechanics.add(new GlobalVariantsMechanic(plugin));
            mechanics.add(new WitherSkeletonEliteGearMechanic(plugin));

            // Day 15
            mechanics.add(new ToxicMeatMechanic(plugin));
            mechanics.add(new MutatedSpiderMechanic(plugin));
            mechanics.add(new EternalNightMechanic(plugin));
            mechanics.add(new ZombieRatatouilleMechanic(plugin));

            // Day 16
            mechanics.add(new IllagerGuardianMechanic(plugin));
            mechanics.add(new ElderGuardianMechanic(plugin));
            mechanics.add(new ExplosiveTotemMechanic(plugin));
            mechanics.add(new NetherRoofWitherMechanic(plugin));
            mechanics.add(new SpawnerMobBuffMechanic(plugin));

            // Day 17
            mechanics.add(new BabyMobRiderMechanic(plugin));
            mechanics.add(new ChargedZombieMechanic(plugin));
            mechanics.add(new TeleportingSpawnsMechanic(plugin));

            // Day 18
            mechanics.add(new BabyKillCurseMechanic(plugin));
            mechanics.add(new VehicleDismountMechanic(plugin));

            // Day 19
            mechanics.add(new DoubleDamageMechanic(plugin));
            mechanics.add(new ApocalypticZombieMechanic(plugin));

            // Day 20
            mechanics.add(new OverworldToxicityMechanic(plugin));
            mechanics.add(new EnderGuardianMechanic(plugin));

            // Day 21
            mechanics.add(new DoubleTotemMechanic(plugin));
            mechanics.add(new ZombieCameramanMechanic(plugin));

            // Day 22
            mechanics.add(new WrongToolDamageMechanic(plugin));
            mechanics.add(new ColoredShulkerMechanic(plugin));

            // Day 23
            mechanics.add(new DoubleFireLavaDamageMechanic(plugin));
            mechanics.add(new NetherOverworldSpawnsMechanic(plugin));
            mechanics.add(new PhantomRiderMechanic(plugin));

            // Day 24
            mechanics.add(new ZoglinNetherSpawnsMechanic(plugin));
            mechanics.add(new VampireBatMechanic(plugin));

            // Day 25
            mechanics.add(new EndermanNerfMechanic(plugin));
            mechanics.add(new GhastDeflectOnlyMechanic(plugin));
            mechanics.add(new AncestralRemainsMechanic(plugin));

            // Day 26
            mechanics.add(new ElytraNerfMechanic(plugin));
            mechanics.add(new PiglinDJMechanic(plugin));

            // Day 27
            mechanics.add(new PyromaniacPiglinMechanic(plugin));
            mechanics.add(new HourlyPotionMechanic(plugin));

            // Day 29
            mechanics.add(new ArabWanderingTraderMechanic(plugin));
            
            initialized = true;
        }
        
        for (DifficultyMechanic mechanic : mechanics) {
            plugin.getServer().getPluginManager().registerEvents(mechanic, plugin);
        }
        
        if (spawnerRegenTask != null) {
            spawnerRegenTask.cancel();
        }
        spawnerRegenTask = new SpawnerRegenTask(spawnerRegenManager, 20L);
        spawnerRegenTask.runTaskTimer(plugin, 20L, 20L);
        
        enabled = true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onDisable(JavaPlugin plugin) {
        for (DifficultyMechanic mechanic : mechanics) {
            HandlerList.unregisterAll(mechanic);
        }
        if (spawnerRegenTask != null) {
            spawnerRegenTask.cancel();
            spawnerRegenTask = null;
        }
        if (spawnerRegenManager != null) {
            spawnerRegenManager.save();
        }
        enabled = false;
    }

    @Override
    public void setEnabled(boolean value) {
        this.enabled = value;
    }
}

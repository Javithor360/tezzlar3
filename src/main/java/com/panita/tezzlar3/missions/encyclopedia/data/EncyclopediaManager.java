package com.panita.tezzlar3.missions.encyclopedia.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.panita.tezzlar3.Tezzlar;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.Mission;
import com.panita.tezzlar3.missions.util.MissionsConfigDefaults;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.missions.ui.MissionBossBarManager;

public class EncyclopediaManager {
    private static final List<EntityType> REQUIRED_MOB_NAMES = Arrays.asList(
            EntityType.ALLAY, EntityType.ARMADILLO, EntityType.AXOLOTL, EntityType.BAT, EntityType.CAMEL, EntityType.CAMEL_HUSK, EntityType.CAT, EntityType.CHICKEN,
            EntityType.COD, EntityType.COPPER_GOLEM, EntityType.COW, EntityType.DONKEY, EntityType.FROG, EntityType.GLOW_SQUID, EntityType.HAPPY_GHAST, EntityType.HORSE,
            EntityType.MOOSHROOM, EntityType.MULE, EntityType.OCELOT, EntityType.PARROT, EntityType.PIG, EntityType.RABBIT, EntityType.SALMON, EntityType.SHEEP,
            EntityType.SKELETON_HORSE, EntityType.SNIFFER, EntityType.SNOW_GOLEM, EntityType.SQUID, EntityType.STRIDER, EntityType.SULFUR_CUBE, EntityType.TADPOLE,
            EntityType.TROPICAL_FISH, EntityType.TURTLE, EntityType.VILLAGER, EntityType.WANDERING_TRADER, EntityType.ZOMBIE_HORSE, EntityType.BEE, EntityType.CAVE_SPIDER,
            EntityType.DOLPHIN, EntityType.DROWNED, EntityType.ENDERMAN, EntityType.FOX, EntityType.GOAT, EntityType.IRON_GOLEM, EntityType.LLAMA, EntityType.NAUTILUS,
            EntityType.PANDA, EntityType.PIGLIN, EntityType.POLAR_BEAR, EntityType.PUFFERFISH, EntityType.SPIDER, EntityType.TRADER_LLAMA, EntityType.WOLF, EntityType.ZOMBIE_NAUTILUS,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.BLAZE, EntityType.BOGGED, EntityType.BREEZE, EntityType.CREAKING, EntityType.CREEPER, EntityType.ELDER_GUARDIAN,
            EntityType.ENDERMITE, EntityType.EVOKER, EntityType.GHAST, EntityType.GUARDIAN, EntityType.HOGLIN, EntityType.HUSK, EntityType.MAGMA_CUBE, EntityType.PARCHED,
            EntityType.PHANTOM, EntityType.PIGLIN_BRUTE, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.SHULKER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME,
            EntityType.STRAY, EntityType.VEX, EntityType.VINDICATOR, EntityType.WARDEN, EntityType.WITCH, EntityType.WITHER_SKELETON, EntityType.ZOGLIN, EntityType.ZOMBIE,
            EntityType.ZOMBIE_VILLAGER, EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.ILLUSIONER
    );

    private final Set<EntityType> targetMobs;
    private final Map<EntityType, EncyclopediaRecord> completedMobs = new HashMap<>();
    private final File dataFile;
    private final Gson gson;

    public EncyclopediaManager(JavaPlugin plugin) {
        this.dataFile = new File(plugin.getDataFolder(), "mob_encyclopedia.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.targetMobs = new HashSet<>(REQUIRED_MOB_NAMES);
        loadData();
    }

    public boolean isTargetMob(EntityType type) {
        return targetMobs.contains(type);
    }

    public boolean isMobCompleted(EntityType type) {
        return completedMobs.containsKey(type) && completedMobs.get(type).getStatus() == EncyclopediaRecord.Status.APPROVED;
    }

    public EncyclopediaRecord getRecord(EntityType type) {
        return completedMobs.get(type);
    }
    
    public List<EntityType> getTargetMobs() {
        return new ArrayList<>(targetMobs);
    }

    /**
     * Checks if a player has already used a specific death method on any mob.
     */
    public boolean hasPlayerUsedMethod(String playerName, String deathMethod) {
        return completedMobs.values().stream()
                .filter(record -> record.getStatus() == EncyclopediaRecord.Status.APPROVED)
                .anyMatch(record -> record.getKillerName().equalsIgnoreCase(playerName) 
                                 && record.getDeathMethod().equalsIgnoreCase(deathMethod));
    }

    public void addRecord(EncyclopediaRecord record) {
        completedMobs.put(record.getMobType(), record);
        saveData();
    }

    public void removeRecord(EntityType type) {
        completedMobs.remove(type);
        saveData();
    }

    private void loadData() {
        if (!dataFile.exists()) return;
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<List<EncyclopediaRecord>>(){}.getType();
            List<EncyclopediaRecord> records = gson.fromJson(reader, type);
            if (records != null) {
                for (EncyclopediaRecord record : records) {
                    completedMobs.put(record.getMobType(), record);
                }
            }
        } catch (IOException e) {
            Tezzlar.getInstance().getLogger().severe("Could not load mob_encyclopedia.json!");
            e.printStackTrace();
        }
    }

    public void saveData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(new ArrayList<>(completedMobs.values()), writer);
            
            if (MissionsModule.getGlobalMissionManager() != null) {
                int count = getCompletedCount();
                int currentGlobal = MissionsModule.getGlobalMissionManager().getProgress("mob_encyclopedia");
                
                if (count != currentGlobal) {
                    MissionsModule.getGlobalMissionManager().setProgress("mob_encyclopedia", count);
                    
                    if (count >= getTotalCount() && currentGlobal < getTotalCount()) {
                        Mission mission = MissionsModule.getMissionManager().getMission("mob_encyclopedia");
                        if (mission != null) {
                            String msg = Tezzlar.getConfigManager().getString("missions.messages.group_completed", MissionsConfigDefaults.MISSIONS_MESSAGES_GROUP_COMPLETED);
                            msg = msg.replace("%mission%", mission.getName());
                            Messenger.prefixedBroadcast(msg);
                            MissionsModule.getDataManager().giveRewardToEveryone("mob_encyclopedia");
                        }
                    } else if (count > currentGlobal) {
                        MissionBossBarManager.forceShowMission(null, "mob_encyclopedia");
                    }
                }
            }
        } catch (IOException e) {
            Tezzlar.getInstance().getLogger().severe("Could not save mob_encyclopedia.json!");
            e.printStackTrace();
        }
    }

    public int getCompletedCount() {
        return (int) completedMobs.values().stream().filter(r -> r.getStatus() == EncyclopediaRecord.Status.APPROVED).count();
    }

    public int getTotalCount() {
        return targetMobs.size();
    }
}

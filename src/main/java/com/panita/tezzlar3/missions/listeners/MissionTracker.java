package com.panita.tezzlar3.missions.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.missions.MissionManager;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.Mission;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import com.panita.tezzlar3.missions.util.MissionsConfigDefaults;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import com.panita.tezzlar3.core.util.CraftingUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

import java.util.Map;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import org.bukkit.event.block.BlockBreakEvent;

import com.panita.tezzlar3.core.chat.actionbar.ActionBarManager;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarProvider;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.missions.ui.MissionBossBarManager;

public class MissionTracker implements Listener, ActionBarProvider {
    private final Map<UUID, Map<String, LinkedList<Long>>> timedKills = new HashMap<>();

    public MissionTracker() {
        if (ActionBarManager.getInstance() != null) {
            ActionBarManager.getInstance().registerProvider(this);
        }
        // Repeated task to check passive state missions (like wearing armor)
        Bukkit.getScheduler().runTaskTimer(Tezzlar.getInstance(), () -> {
            int currentDay = TimeManager.getCurrentDay();
            for (Map.Entry<String, Mission> entry : MissionsModule.getMissionManager().getLoadedMissions().entrySet()) {
                Mission mission = entry.getValue();
                if (currentDay < mission.getStartDay() || currentDay > mission.getEndDay()) continue;
                
                if (mission.getObjectiveType().equalsIgnoreCase("BRING_ENTITY") && mission.getObjectiveTarget().equalsIgnoreCase("WARDEN")) {
                    String locStr = mission.getObjectiveLocation();
                    if (locStr == null) continue;
                    
                    String[] parts = locStr.split(",");
                    if (parts.length < 4) continue;
                    
                    World world = Bukkit.getWorld(parts[0]);
                    if (world == null) continue;
                    
                    try {
                        double x = Double.parseDouble(parts[1]);
                        double y = Double.parseDouble(parts[2]);
                        double z = Double.parseDouble(parts[3]);
                        
                        Location targetLoc = new Location(world, x, y, z);
                        int radius = mission.getObjectiveRadius();
                        
                        for (Entity entity : world.getNearbyEntities(targetLoc, radius, radius, radius)) {
                            if (entity.getType() == EntityType.WARDEN) {
                                advanceProgress(null, mission.getId(), mission.getObjectiveAmount());
                                break;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                for (Map.Entry<String, Mission> entry : MissionsModule.getMissionManager().getLoadedMissions().entrySet()) {
                    Mission mission = entry.getValue();
                    if (currentDay < mission.getStartDay() || currentDay > mission.getEndDay()) continue;
                    
                    if (mission.getObjectiveType().equalsIgnoreCase("EQUIP_ARMOR")) {
                        if (mission.getObjectiveTarget().equalsIgnoreCase("DIAMOND_ARMOR_SET")) {
                            int count = 0;
                            if (player.getInventory().getHelmet().getType() == Material.DIAMOND_HELMET) count++;
                            if (player.getInventory().getChestplate().getType() == Material.DIAMOND_CHESTPLATE) count++;
                            if (player.getInventory().getLeggings().getType() == Material.DIAMOND_LEGGINGS) count++;
                            if (player.getInventory().getBoots().getType() == Material.DIAMOND_BOOTS) count++;
                            
                            if (count >= mission.getObjectiveAmount()) {
                                // Add the full target amount at once because it's a state, not cumulative
                                advanceProgress(player, mission.getId(), count);
                            }
                        }
                    }
                }
            }
        }, 20L, 100L); // Check every 5 seconds
    }

    private void advanceProgress(Player player, String missionId, int amount) {
        Mission mission = MissionsModule.getMissionManager().getMission(missionId);
        if (mission == null) return;
        
        int currentDay = TimeManager.getCurrentDay();
        if (currentDay < mission.getStartDay() || currentDay > mission.getEndDay()) return;
        
        boolean isGroup = mission.getScope().equalsIgnoreCase("GROUP");
        int targetAmount = mission.getObjectiveAmount();
        
        if (isGroup) {
            int currentProgress = MissionsModule.getGlobalMissionManager().getProgress(missionId);
            if (currentProgress >= targetAmount) return; // Already completed
            
            MissionsModule.getGlobalMissionManager().addProgress(missionId, amount);
            int newProgress = MissionsModule.getGlobalMissionManager().getProgress(missionId);
            
            if (newProgress >= targetAmount) {
                // Completed globally!
                String msg = Tezzlar.getConfigManager().getString("missions.messages.group_completed", MissionsConfigDefaults.MISSIONS_MESSAGES_GROUP_COMPLETED);
                msg = msg.replace("%mission%", mission.getName());
                Messenger.prefixedBroadcast(msg);
                
                MissionsModule.getDataManager().giveRewardToEveryone(missionId);
            } else {
                MissionBossBarManager.forceShowMission(null, missionId);
            }
        } else {
            if (player == null) return;
            PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
            if (data == null || data.hasCompleted(missionId)) return;
            
            int currentProgress = data.getProgress(missionId);
            int newProgress = currentProgress + amount;
            
            if (newProgress >= targetAmount) {
                data.addCompletedMission(missionId);
                if (!mission.getRewards().isEmpty()) {
                    data.addPendingReward(missionId);
                }
                
                String msg = Tezzlar.getConfigManager().getString("missions.messages.individual_completed", MissionsConfigDefaults.MISSIONS_MESSAGES_INDIVIDUAL_COMPLETED);
                msg = msg.replace("%player%", player.getName()).replace("%mission%", mission.getName());
                Messenger.prefixedBroadcast(msg);
            } else {
                data.setProgress(missionId, newProgress);
                MissionBossBarManager.forceShowMission(player, missionId);
            }
        }
    }

    private boolean hasActiveMissionObjective(String type) {
        int currentDay = TimeManager.getCurrentDay();
        for (Map.Entry<String, Mission> entry : MissionsModule.getMissionManager().getLoadedMissions().entrySet()) {
            Mission mission = entry.getValue();
            if (currentDay < mission.getStartDay() || currentDay > mission.getEndDay()) continue;
            if (mission.getObjectiveType().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    private void checkObjective(Player player, String type, String target, String environment, int amount, Entity targetEntity) {
        for (Map.Entry<String, Mission> entry : MissionsModule.getMissionManager().getLoadedMissions().entrySet()) {
            Mission mission = entry.getValue();
            if (mission.getObjectiveType().equalsIgnoreCase(type) && mission.getObjectiveTarget().equalsIgnoreCase(target)) {
                
                if (mission.getObjectiveEnvironment() != null && environment != null) {
                    if (!mission.getObjectiveEnvironment().equalsIgnoreCase(environment)) {
                        continue;
                    }
                }
                
                if (type.equalsIgnoreCase("KILL_ENTITY")) {
                    if (targetEntity != null) {
                        if (mission.getObjectiveMinHeight() != Integer.MIN_VALUE && targetEntity.getLocation().getY() < mission.getObjectiveMinHeight()) {
                            continue; // Mob is below required height
                        }
                    }
                    if (mission.getObjectivePotionEffect() != null && player != null) {
                        PotionEffectType reqEffect = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(mission.getObjectivePotionEffect().toLowerCase()));
                        if (reqEffect != null && !player.hasPotionEffect(reqEffect)) {
                            continue; // Player does not have required potion effect
                        }
                    }
                }
                
                advanceProgress(player, mission.getId(), amount);
            }
        }
    }

    private void checkObjective(Player player, String type, String target, int amount) {
        checkObjective(player, type, target, null, amount, null);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!hasActiveMissionObjective("KILL_ENTITY") && !hasActiveMissionObjective("KILL_ENTITY_TIMED")) return;
        
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            String target = event.getEntityType().name();
            String env = event.getEntity().getWorld().getEnvironment().name();
            
            checkObjective(player, "KILL_ENTITY", target, env, 1, event.getEntity());
            
            // Check for KILL_ENTITY_TIMED
            for (Map.Entry<String, Mission> entry : MissionsModule.getMissionManager().getLoadedMissions().entrySet()) {
                Mission mission = entry.getValue();
                if (mission.getObjectiveType().equalsIgnoreCase("KILL_ENTITY_TIMED") && mission.getObjectiveTarget().equalsIgnoreCase(target)) {
                    int currentDay = TimeManager.getCurrentDay();
                    if (currentDay < mission.getStartDay() || currentDay > mission.getEndDay()) continue;
                    
                    PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
                    if (data == null || data.hasCompleted(mission.getId())) continue;
                    
                    long now = System.currentTimeMillis();
                    long timeLimitMs = mission.getObjectiveTimeLimit() * 1000L;
                    
                    timedKills.putIfAbsent(player.getUniqueId(), new HashMap<>());
                    Map<String, LinkedList<Long>> playerMissions = timedKills.get(player.getUniqueId());
                    playerMissions.putIfAbsent(mission.getId(), new LinkedList<>());
                    
                    LinkedList<Long> kills = playerMissions.get(mission.getId());
                    
                    // If time from the FIRST kill exceeded the limit, reset the whole streak
                    if (!kills.isEmpty() && (now - kills.getFirst()) > timeLimitMs) {
                        kills.clear();
                    }
                    
                    kills.add(now);
                    
                    if (kills.size() >= mission.getObjectiveAmount()) {
                        advanceProgress(player, mission.getId(), mission.getObjectiveAmount());
                        kills.clear(); // Reset after completion
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (!hasActiveMissionObjective("SPAWN_ENTITY") && !hasActiveMissionObjective("SUMMON_ENTITY")) return;
        
        if (event.getEntityType() == EntityType.ENDER_DRAGON) {
            checkObjective(null, "SPAWN_ENTITY", "ENDER_DRAGON", 1);
        } else if (event.getEntityType() == EntityType.IRON_GOLEM) {
            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM) {
                checkObjective(null, "SUMMON_ENTITY", "IRON_GOLEM", 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        if (!hasActiveMissionObjective("OBTAIN_EXP")) return;
        
        if (event.getAmount() > 0) {
            checkObjective(event.getPlayer(), "OBTAIN_EXP", null, event.getAmount());
        }
    }

    @Override
    public String getId() {
        return "mission_timed_kill";
    }

    @Override
    public java.util.List<String> getTexts(Player player) {
        Map<String, LinkedList<Long>> playerMissions = timedKills.get(player.getUniqueId());
        if (playerMissions == null || playerMissions.isEmpty()) return null;

        long now = System.currentTimeMillis();
        int currentDay = TimeManager.getCurrentDay();
        
        for (Map.Entry<String, LinkedList<Long>> entry : playerMissions.entrySet()) {
            Mission mission = MissionsModule.getMissionManager().getMission(entry.getKey());
            if (mission == null) continue;
            if (currentDay < mission.getStartDay() || currentDay > mission.getEndDay()) continue;
            
            LinkedList<Long> kills = entry.getValue();
            long timeLimitMs = mission.getObjectiveTimeLimit() * 1000L;
            
            if (!kills.isEmpty()) {
                long oldest = kills.getFirst();
                long remainingMs = timeLimitMs - (now - oldest);
                
                if (remainingMs <= 0) {
                    kills.clear(); // Timer expired, reset
                    continue;
                }
                
                String timeStr = Global.formatTimeTicks((remainingMs / 1000) * 20L);
                
                int n = kills.size();
                int N = mission.getObjectiveAmount();
                
                return java.util.Collections.singletonList(String.format("<#FFB732>Racha en progreso: <#FF5555>%d<#FFB732>/<#FF5555>%d <#FFB732>(%s)</#FFB732>", n, N, timeStr));
            }
        }
        return null;
    }

    @Override
    public boolean isUrgent(Player player) {
        return getTexts(player) != null;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!hasActiveMissionObjective("EAT_ITEM")) return;
        
        Player player = event.getPlayer();
        String target = event.getItem().getType().name();
        checkObjective(player, "EAT_ITEM", target, 1);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!hasActiveMissionObjective("CRAFT_ITEM") && !hasActiveMissionObjective("CRAFT_MULTIPLE_ITEMS")) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        if (event.getRecipe() == null || event.getRecipe().getResult() == null) return;
        
        ItemStack result = event.getRecipe().getResult();
        String target = result.getType().name();
        
        if (result.hasItemMeta()) {
            NamespacedKey key = new NamespacedKey(Tezzlar.getInstance(), "custom_item_id");
            if (result.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                target = result.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
            }
        }
        
        int amount = CraftingUtils.getCraftedAmount(event);
        if (amount <= 0) return;
        
        int maxAddable = getMaxAddable(player, result, amount, event.isShiftClick());
        if (maxAddable <= 0) return;
        
        int recipeYield = result.getAmount();
        int actualCrafted = (maxAddable / recipeYield) * recipeYield;
        
        if (actualCrafted <= 0) return;
        
        checkObjective(player, "CRAFT_ITEM", target, actualCrafted);
        
        // Handle CRAFT_MULTIPLE_ITEMS
        for (Map.Entry<String, Mission> entry : MissionsModule.getMissionManager().getLoadedMissions().entrySet()) {
            Mission mission = entry.getValue();
            if (!mission.getObjectiveType().equalsIgnoreCase("CRAFT_MULTIPLE_ITEMS")) continue;
            
            int currentDay = TimeManager.getCurrentDay();
            if (currentDay < mission.getStartDay() || currentDay > mission.getEndDay()) continue;
            
            PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
            if (data == null || data.hasCompleted(mission.getId())) continue;

            Map<String, Integer> targets = mission.getObjectiveTargetsMap();
            if (targets == null || targets.isEmpty()) continue;

            for (Map.Entry<String, Integer> t : targets.entrySet()) {
                if (t.getKey().equalsIgnoreCase(target)) {
                    String subKey = mission.getId() + "_sub_" + t.getKey();
                    int currentSub = data.getProgress(subKey);
                    
                    if (currentSub < t.getValue()) {
                        data.setProgress(subKey, currentSub + actualCrafted);
                        MissionBossBarManager.forceShowMission(player, mission.getId());
                        
                        int completedTargets = 0;
                        for (Map.Entry<String, Integer> t2 : targets.entrySet()) {
                            if (data.getProgress(mission.getId() + "_sub_" + t2.getKey()) >= t2.getValue()) {
                                completedTargets++;
                            }
                        }
                        
                        if (completedTargets >= targets.size()) {
                            advanceProgress(player, mission.getId(), 1);
                        }
                    }
                    break;
                }
            }
        }
    }
    
    private int getMaxAddable(Player player, ItemStack item, int attemptAmount, boolean isShiftClick) {
        int maxStack = item.getMaxStackSize();
        int remaining = attemptAmount;

        if (!isShiftClick) {
            // Normal click: item goes to cursor
            ItemStack cursor = player.getOpenInventory().getCursor();
            if (cursor == null || cursor.getType() == Material.AIR) {
                remaining -= Math.min(remaining, maxStack);
            } else if (cursor.getType() == item.getType()) {
                remaining -= Math.min(remaining, maxStack - cursor.getAmount());
            }
        } else {
            // Shift click: items go directly to inventory (ignoring cursor)
            for (ItemStack invItem : player.getInventory().getContents()) {
                if (remaining <= 0) break;
                if (invItem == null || invItem.getType() == Material.AIR) {
                    remaining -= Math.min(remaining, maxStack);
                } else if (invItem.getType() == item.getType()) {
                    remaining -= Math.min(remaining, maxStack - invItem.getAmount());
                }
            }
        }

        return attemptAmount - remaining;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        event.getBlock().setMetadata("tezzlar_placed", new FixedMetadataValue(Tezzlar.getInstance(), true));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!hasActiveMissionObjective("MINE_BLOCKS")) return;
        if (event.getBlock().hasMetadata("tezzlar_placed")) return;
        
        Player player = event.getPlayer();
        String blockName = event.getBlock().getType().name();

        for (Map.Entry<String, Mission> entry : MissionsModule.getMissionManager().getLoadedMissions().entrySet()) {
            Mission mission = entry.getValue();
            if (!mission.getObjectiveType().equalsIgnoreCase("MINE_BLOCKS")) continue;
            
            int currentDay = TimeManager.getCurrentDay();
            if (currentDay < mission.getStartDay() || currentDay > mission.getEndDay()) continue;
            
            PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(player);
            if (data == null || data.hasCompleted(mission.getId())) continue;

            Map<String, Integer> targets = mission.getObjectiveTargetsMap();
            if (targets == null || targets.isEmpty()) continue;

            for (Map.Entry<String, Integer> target : targets.entrySet()) {
                List<String> validBlocks = Arrays.asList(target.getKey().split(","));
                if (validBlocks.contains(blockName)) {
                    String subKey = mission.getId() + "_sub_" + target.getKey();
                    int currentSub = data.getProgress(subKey);
                    
                    if (currentSub < target.getValue()) {
                        data.setProgress(subKey, currentSub + 1);
                        MissionBossBarManager.forceShowMission(player, mission.getId());
                        
                        // Check if all targets are now fulfilled
                        int completedTargets = 0;
                        for (Map.Entry<String, Integer> t : targets.entrySet()) {
                            if (data.getProgress(mission.getId() + "_sub_" + t.getKey()) >= t.getValue()) {
                                completedTargets++;
                            }
                        }
                        
                        if (completedTargets >= targets.size()) {
                            // All blocks mined, complete the mission
                            advanceProgress(player, mission.getId(), 1);
                        }
                    }
                    break;
                }
            }
        }
    }
}

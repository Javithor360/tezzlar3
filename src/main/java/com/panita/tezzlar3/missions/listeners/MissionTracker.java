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
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import com.panita.tezzlar3.core.util.CraftingUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class MissionTracker implements Listener {

    public MissionTracker() {
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
        if (!hasActiveMissionObjective("KILL_ENTITY")) return;
        
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            String target = event.getEntityType().name();
            String env = event.getEntity().getWorld().getEnvironment().name();
            
            checkObjective(player, "KILL_ENTITY", target, env, 1, event.getEntity());
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

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!hasActiveMissionObjective("EAT_ITEM")) return;
        
        Player player = event.getPlayer();
        String target = event.getItem().getType().name();
        checkObjective(player, "EAT_ITEM", target, 1);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!hasActiveMissionObjective("CRAFT_ITEM")) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        if (event.getRecipe() == null || event.getRecipe().getResult() == null) return;
        
        ItemStack result = event.getRecipe().getResult();
        String target = result.getType().name();
        
        int amount = CraftingUtils.getCraftedAmount(event);
        if (amount <= 0) return;
        
        int maxAddable = getMaxAddable(player, result, amount, event.isShiftClick());
        if (maxAddable <= 0) return;
        
        // Round down to the nearest multiple of the recipe's result amount
        // Because you can't craft a fraction of a recipe
        int recipeYield = result.getAmount();
        int actualCrafted = (maxAddable / recipeYield) * recipeYield;
        
        if (actualCrafted <= 0) return;
        
        checkObjective(player, "CRAFT_ITEM", target, actualCrafted);
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
}

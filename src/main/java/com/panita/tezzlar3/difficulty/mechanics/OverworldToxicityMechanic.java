package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.missions.MissionsModule;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarManager;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarProvider;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.timeline.util.TimeManager;

public class OverworldToxicityMechanic extends DifficultyMechanic implements ActionBarProvider {

    private final NamespacedKey TOXICITY_KEY;
    private static OverworldToxicityMechanic instance;

    public OverworldToxicityMechanic(JavaPlugin plugin) {
        super(plugin, 20);
        this.TOXICITY_KEY = new NamespacedKey(plugin, "overworld_toxicity");
        instance = this;
        
        if (ActionBarManager.getInstance() != null) {
            ActionBarManager.getInstance().registerProvider(this);
        }

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!PlayerUtils.isSurvival(player)) continue;
                
                boolean isContaminated = false;
                World.Environment env = player.getWorld().getEnvironment();
                if (env == World.Environment.NORMAL) {
                    isContaminated = true;
                } else if (env == World.Environment.THE_END && TimeManager.getCurrentDay() >= 25) {
                    isContaminated = true;
                }

                if (isContaminated) {
                    int seconds = player.getPersistentDataContainer().getOrDefault(TOXICITY_KEY, PersistentDataType.INTEGER, 0);
                    seconds++;
                    player.getPersistentDataContainer().set(TOXICITY_KEY, PersistentDataType.INTEGER, seconds);
                    
                    int remaining = 600 - seconds;
                    if (remaining <= 0) {
                        applyToxicEffects(player);
                    }
                } else {
                    // Reset toxicity when leaving contaminated dimensions
                    player.getPersistentDataContainer().set(TOXICITY_KEY, PersistentDataType.INTEGER, 0);
                }
            }
        }, 20L, 20L);
    }

    public static boolean isToxic(Player player) {
        if (instance == null || !instance.isActive()) return false;
        if (!PlayerUtils.isSurvival(player)) return false;
        
        World.Environment env = player.getWorld().getEnvironment();
        if (env == World.Environment.NORMAL) return true;
        if (env == World.Environment.THE_END && TimeManager.getCurrentDay() >= 25) return true;
        return false;
    }

    private void applyToxicEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 2, false, false, true)); // Mining Fatigue 3
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3, false, false, true)); // Slowness 4
        player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 60, 3, false, false, true)); // Hunger 4
        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1, false, false, true)); // Wither 2
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false, true));
    }
    
    @Override
    public String getId() {
        return "overworld_toxicity";
    }

    @Override
    public java.util.List<String> getTexts(Player player) {
        if (!isActive()) return null;
        if (!PlayerUtils.isSurvival(player)) return null;
        
        boolean isContaminated = false;
        World.Environment env = player.getWorld().getEnvironment();
        if (env == World.Environment.NORMAL) isContaminated = true;
        if (env == World.Environment.THE_END && TimeManager.getCurrentDay() >= 25) isContaminated = true;
        
        if (!isContaminated) return null;

        int seconds = player.getPersistentDataContainer().getOrDefault(TOXICITY_KEY, PersistentDataType.INTEGER, 0);
        int remaining = 600 - seconds;
        
        if (remaining > 0) {
            String color = "<#81C784>"; // Green
            if (remaining <= 300) color = "<#FFCA28>"; // Yellow
            if (remaining <= 60) color = "<#FF5252>"; // Red
            
            String timeStr = Global.formatTimeTicks(remaining * 20L);
            return java.util.Collections.singletonList("<gray>Contaminación en </gray>" + color + timeStr + "</" + color.substring(1,8) + ">");
        } else {
            return java.util.Collections.singletonList("<#FF5252><b>¡NIVELES DE CONTAMINACIÓN CRÍTICOS!</b></#FF5252>");
        }
    }

    @Override
    public boolean isUrgent(Player player) {
        if (!isActive() || !PlayerUtils.isSurvival(player)) return false;
        int seconds = player.getPersistentDataContainer().getOrDefault(TOXICITY_KEY, PersistentDataType.INTEGER, 0);
        return (600 - seconds) <= 0;
    }
}

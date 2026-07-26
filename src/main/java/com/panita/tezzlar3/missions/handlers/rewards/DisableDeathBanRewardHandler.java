package com.panita.tezzlar3.missions.handlers.rewards;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.difficulty.mechanics.DeathTrainMechanic;
import com.panita.tezzlar3.missions.handlers.RewardHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.time.Duration;

public class DisableDeathBanRewardHandler implements RewardHandler {
    @Override
    public String getId() {
        return "DISABLE_DEATH_BAN";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        // Enable global flags to disable death bans and the death train
        Tezzlar.getConfigManager().updateBoolean("hardcore.death_bans_disabled", true, null);
        Tezzlar.getConfigManager().updateBoolean("difficulty.death_train_disabled", true, null);
        
        if (DeathTrainMechanic.getInstance() != null) {
            DeathTrainMechanic.getInstance().setRemainingSeconds(0);
        }
        
        Messenger.broadcast("&a¡Se han deshabilitado los baneos por muerte y el Tren de la Muerte!");
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            Messenger.showTitle(
                p, 
                "&e&l¡Felicidades!", 
                "&bSe han librado de la muerte perpetua", 
                Duration.ofMillis(500), 
                Duration.ofSeconds(6), 
                Duration.ofMillis(1000)
            );
        }
        
        SoundUtils.playGlobal("ui.toast.challenge_complete", 1.0f, 1.0f);
        SoundUtils.playGlobal("entity.ender_dragon.death", 0.6f, 1.4f);
        SoundUtils.playGlobal("entity.wither.spawn", 0.5f, 0.7f);
    }
}

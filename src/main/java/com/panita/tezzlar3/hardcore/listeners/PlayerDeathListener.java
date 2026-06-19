package com.panita.tezzlar3.hardcore.listeners;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.hardcore.HardcoreModule;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.time.Duration;
import java.time.Instant;
import io.papermc.paper.ban.BanListType;
import com.destroystokyo.paper.profile.PlayerProfile;

public class PlayerDeathListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // 1. Increment and get deaths
        HardcoreDataManager.incrementDeaths(player.getUniqueId(), player.getName());
        int deaths = HardcoreDataManager.getDeaths(player.getUniqueId());

        // 2. Play dramatic global effects
        SoundUtils.playGlobal("entity.wither.spawn", 1.0f, 0.5f);
        SoundUtils.playGlobal("entity.lightning_bolt.thunder", 1.0f, 1.0f);
        
        try {
            player.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation(), 1);
        } catch (Exception e) {
            // Fallback for older versions if EXPLOSION_EMITTER doesn't exist
            player.getLocation().getWorld().spawnParticle(Particle.valueOf("EXPLOSION_HUGE"), player.getLocation(), 1);
        }

        // 3. Process penalty
        if (deaths <= 3) {
            // Kick without ban for the first 3 deaths
            String kickMsg = "<red>¡Has muerto!</red>\n<gray>Esta fue tu muerte #<yellow>" + deaths + "</yellow> de 3 permitidas libres.</gray>\n<yellow>Tómate un respiro antes de volver a entrar.</yellow>";
            HardcoreModule.getPendingKicks().put(player.getUniqueId(), kickMsg);
        } else {
            // Ban for 4th death and beyond
            int hoursToBan = 8;
            if (deaths >= 5) {
                hoursToBan = (deaths - 4) * 12;
            }
            
            Duration duration = Duration.ofHours(hoursToBan);
            Instant expirationInstant = Instant.now().plus(duration);
            
            String banReason = "<dark_red><bold>¡Has perdido una vida Hardcore!</bold></dark_red>\n<red>Muertes totales: <yellow>" + deaths + "</yellow></red>";
            String kickReason = banReason + "\n<gray>Estarás exiliado por <red>" + hoursToBan + " horas</red>.</gray>";

            // Store the ban in our custom data manager using UUID to completely bypass the Vanilla ban UI
            HardcoreDataManager.setBanExpiration(player.getUniqueId(), player.getName(), expirationInstant.toEpochMilli());
            
            // Queue for kick on immediate respawn with the full static text
            HardcoreModule.getPendingKicks().put(player.getUniqueId(), kickReason);
            
            // Broadcast the fall
            Messenger.broadcast("<dark_red><bold>[HARDCORE]</bold></dark_red> <red>El jugador <yellow>" + player.getName() + "</yellow> ha caído y ha sido exiliado por " + hoursToBan + " horas.</red>");
        }
    }
}

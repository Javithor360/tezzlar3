package com.panita.tezzlar3.hardcore.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.hardcore.HardcoreModule;
import com.panita.tezzlar3.hardcore.util.HardcoreConfigDefaults;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.time.Instant;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import com.panita.tezzlar3.hardcore.util.HardcoreMessageFormatter;

public class PlayerDeathListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // 1. Increment and get deaths
        HardcoreDataManager.incrementDeaths(player.getUniqueId(), player.getName());
        int deaths = HardcoreDataManager.getDeaths(player.getUniqueId());

        // 2. Play dramatic global effects
        List<String> sounds = Tezzlar.getConfigManager().getStringList("hardcore.deathSounds");
        if (sounds == null || sounds.isEmpty()) {
            sounds = HardcoreConfigDefaults.HARDCORE_DEATHSOUNDS;
        }
        
        for (String soundStr : sounds) {
            String[] parts = soundStr.split(";");
            if (parts.length > 0) {
                String soundName = parts[0];
                float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
                float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
                SoundUtils.playGlobal(soundName, volume, pitch);
            }
        }
        
        try {
            player.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation(), 1);
        } catch (Exception e) {
            // Fallback for older versions if EXPLOSION_EMITTER doesn't exist
            player.getLocation().getWorld().spawnParticle(Particle.valueOf("EXPLOSION_HUGE"), player.getLocation(), 1);
        }
        
        // 3. Place death structure
        boolean placeSkull = Tezzlar.getConfigManager().getBoolean("hardcore.placeSkullOnDeath", HardcoreConfigDefaults.HARDCORE_PLACESKULLONDEATH);
        if (placeSkull) {
            Location deathLoc = player.getLocation().getBlock().getLocation();
            
            Block bedrockBlock = deathLoc.clone().add(0, -1, 0).getBlock();
            bedrockBlock.setType(Material.BEDROCK);
            
            Block wallBlock = deathLoc.getBlock();
            wallBlock.setType(Material.NETHER_BRICK_FENCE);
            
            Block headBlock = deathLoc.clone().add(0, 1, 0).getBlock();
            headBlock.setType(Material.PLAYER_HEAD);
            
            if (headBlock.getState() instanceof Skull) {
                Skull skullState = (Skull) headBlock.getState();
                skullState.setProfile(ResolvableProfile.resolvableProfile(player.getPlayerProfile()));
                skullState.update();
            }
            
            if (headBlock.getBlockData() instanceof Rotatable) {
                Rotatable rotatable = (Rotatable) headBlock.getBlockData();
                // Face the skull towards the player's last looking direction (opposite face)
                rotatable.setRotation(player.getFacing().getOppositeFace());
                headBlock.setBlockData(rotatable);
            }
        }

        // 4. Process penalty
        if (deaths <= 3) {
            // Kick without ban for the first 3 deaths
            String rawWarnMsg = Tezzlar.getConfigManager().getString(
                    "hardcore.messages.warnKickMessage", 
                    HardcoreConfigDefaults.HARDCORE_WARNKICKMESSAGE
            );
            String kickMsg = HardcoreMessageFormatter.processPlaceholders(rawWarnMsg, player.getName(), deaths, null);
            HardcoreModule.getPendingKicks().put(player.getUniqueId(), kickMsg);
        } else {
            // Ban for 4th death and beyond
            int hoursToBan = 8;
            if (deaths >= 5) {
                hoursToBan = (deaths - 4) * 12;
            }
            
            long durationMillis = hoursToBan * 3600000L;
            Instant expirationInstant = Instant.now().plusMillis(durationMillis);
            
            // Format duration
            String formattedTime = Global.formatDuration(durationMillis);
            
            // Fetch message from config
            String rawKickReason = Tezzlar.getConfigManager().getString(
                    "hardcore.messages.kickMessage", 
                    HardcoreConfigDefaults.HARDCORE_KICKMESSAGE
            );
            
            String kickReason = HardcoreMessageFormatter.processPlaceholders(rawKickReason, player.getName(), deaths, formattedTime);

            // Store the ban in our custom data manager using UUID to completely bypass the Vanilla ban UI
            HardcoreDataManager.setBanExpiration(player.getUniqueId(), player.getName(), expirationInstant.toEpochMilli());
            
            // Queue for kick on immediate respawn with the full static text
            HardcoreModule.getPendingKicks().put(player.getUniqueId(), kickReason);
        }
        
        // 1. The Vanilla death message is already broadcasted automatically by Bukkit (event.deathMessage())

        // 2. Broadcast the generic death message
        String rawGeneric = Tezzlar.getConfigManager().getString(
                "hardcore.messages.genericDeathMessage",
                HardcoreConfigDefaults.HARDCORE_GENERICDEATHMESSAGE
        );
        String genericMsg = HardcoreMessageFormatter.processPlaceholders(rawGeneric, player.getName(), deaths, null);
        Messenger.broadcast(genericMsg);
        
        // 3. Broadcast the custom player death message
        String fallbackDefault = HardcoreConfigDefaults.HARDCORE_DEATHMESSAGES.getOrDefault(
                player.getName(), 
                HardcoreConfigDefaults.HARDCORE_DEATHMESSAGES.get("default")
        );
        String defaultCustomConfig = Tezzlar.getConfigManager().getString(
                "hardcore.messages.deathMessages.default", 
                fallbackDefault
        );
        String rawCustom = Tezzlar.getConfigManager().getString(
                "hardcore.messages.deathMessages." + player.getName(),
                defaultCustomConfig
        );
        String customMsg = HardcoreMessageFormatter.processPlaceholders(rawCustom, player.getName(), deaths, null);
        Messenger.broadcast(customMsg);
    }
}

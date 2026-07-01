package com.panita.tezzlar3.hardcore.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.hardcore.HardcoreModule;
import com.panita.tezzlar3.hardcore.util.HardcoreConfigDefaults;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import com.panita.tezzlar3.hardcore.util.HardcoreMessageFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.panita.tezzlar3.timeline.util.TimeManager;
import com.panita.tezzlar3.difficulty.mechanics.DeathTrainMechanic;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import io.papermc.paper.datacomponent.item.ResolvableProfile;

public class PlayerDeathListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        NamespacedKey KARMA_KEY = new NamespacedKey(Tezzlar.getInstance(), "pvp_karma_doomed");
        
        // If the player dies, the karma debt is considered paid
        if (player.getPersistentDataContainer().has(KARMA_KEY, PersistentDataType.BYTE)) {
            player.getPersistentDataContainer().remove(KARMA_KEY);
        }
        
        Player killer = player.getKiller();
        
        if (killer != null && TimeManager.getCurrentDay() >= 2) {
            killer.getPersistentDataContainer().set(KARMA_KEY, PersistentDataType.BYTE, (byte) 1);
            killer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
            killer.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 1));
            killer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 255));
            Messenger.prefixedSend(killer, "&cEl karma te hará pagar por tus pecados...");
            
            Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                if (killer.isOnline() && !killer.isDead()) {
                    killer.setHealth(0.0);
                }
            }, 100L); // 5 seconds
        }
        
        // 1. Get current state and update
        int lives = HardcoreDataManager.getLives(player.getUniqueId(), player.getName());
        int maxLives = HardcoreDataManager.getMaxLives(player.getUniqueId(), player.getName());
        int currentDeaths = HardcoreDataManager.getDeaths(player.getUniqueId(), player.getName());
        
        boolean isBanDeath = (lives <= 0);
        
        if (!isBanDeath) {
            lives--;
            HardcoreDataManager.setLives(player.getUniqueId(), player.getName(), lives);
        } else {
            currentDeaths++;
            HardcoreDataManager.incrementDeaths(player.getUniqueId(), player.getName());
        }

        try {
            player.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation(), 5);
        } catch (Exception e) {
            // Fallback for older versions if EXPLOSION_EMITTER doesn't exist
            player.getLocation().getWorld().spawnParticle(Particle.valueOf("EXPLOSION_HUGE"), player.getLocation(), 5);
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
        if (!isBanDeath) {
            // Kick without ban (lives lost)
            String rawWarnMsg = Tezzlar.getConfigManager().getString(
                    "hardcore.messages.warnKickMessage", 
                    HardcoreConfigDefaults.HARDCORE_WARNKICKMESSAGE
            );
            String kickMsg = HardcoreMessageFormatter.processPlaceholders(rawWarnMsg, player, null);
            HardcoreModule.getPendingKicks().put(player.getUniqueId(), kickMsg);
        } else {
            // Ban for deaths when lives are 0
            int banNumber = currentDeaths;
            int hoursToBan = (banNumber - 1) * 12;
            if (banNumber == 1) {
                hoursToBan = 8;
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
            
            String kickReason = HardcoreMessageFormatter.processPlaceholders(rawKickReason, player, formattedTime);

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
        String genericMsg = HardcoreMessageFormatter.processPlaceholders(rawGeneric, player, null);
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
        String customMsg = HardcoreMessageFormatter.processPlaceholders(rawCustom, player, null);
        Messenger.broadcast(customMsg);
        // 4. Show title and play dramatic sounds synchronously
        String rawTitle = Tezzlar.getConfigManager().getString(
                "hardcore.messages.deathTitle",
                HardcoreConfigDefaults.HARDCORE_DEATHTITLE
        );
        String rawSubtitle = Tezzlar.getConfigManager().getString(
                "hardcore.messages.deathSubtitle",
                HardcoreConfigDefaults.HARDCORE_DEATHSUBTITLE
        );
        
        String parsedTitle = HardcoreMessageFormatter.processPlaceholders(rawTitle, player, null);
        String parsedSub = HardcoreMessageFormatter.processPlaceholders(rawSubtitle, player, null);
        
        List<String> sounds = Tezzlar.getConfigManager().getStringList("hardcore.deathSounds");
        if (sounds == null || sounds.isEmpty()) {
            sounds = HardcoreConfigDefaults.HARDCORE_DEATHSOUNDS;
        }
        
        boolean showTitle = rawTitle != null && !rawTitle.trim().isEmpty() || rawSubtitle != null && !rawSubtitle.trim().isEmpty();
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (showTitle) {
                // Flash the title instantly (0 fade-in) for dramatic jumpscare sync
                Messenger.showTitle(
                        p, 
                        parsedTitle, 
                        parsedSub, 
                        Duration.ZERO, 
                        Duration.ofSeconds(5), 
                        Duration.ofMillis(1000)
                );
            }
        }
        
        // Play sounds globally using SoundUtils
        for (String soundStr : sounds) {
            String[] parts = soundStr.split(";");
            if (parts.length > 0) {
                String soundName = parts[0];
                float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
                float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
                SoundUtils.playGlobal(soundName, volume, pitch);
            }
        }
        
        if (DeathTrainMechanic.getInstance() != null) {
            DeathTrainMechanic.getInstance().addDeathTrainTime();
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NamespacedKey KARMA_KEY = new NamespacedKey(Tezzlar.getInstance(), "pvp_karma_doomed");
        
        if (player.getPersistentDataContainer().has(KARMA_KEY, PersistentDataType.BYTE)) {
            // PvP Karma evasion: The player disconnected to avoid the 5s death.
            // Consequence: Kill them upon rejoining.
            
            Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                if (player.isOnline() && !player.isDead()) {
                    player.setHealth(0.0);
                }
            }, 20L); // 1 second after joining to ensure systems load correctly
        }
    }
}

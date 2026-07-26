package com.panita.tezzlar3.bossfight.util;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.config.ConfigManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Sound;

import java.time.Duration;
import java.util.UUID;

public class BossManager {

    private static BossManager instance;
    
    private Player boss;
    private int currentPhase;
    private boolean fakeDeathState;
    private BossBar globalBossBar;
    private BukkitTask particleTask;
    private UUID pendingBossUuid; // Used when the boss is offline during load

    private BossManager() {
        // Private constructor
    }

    public static BossManager getInstance() {
        if (instance == null) {
            instance = new BossManager();
        }
        return instance;
    }

    public void startFight(Player player) {
        if (this.boss != null) {
            Messenger.prefixedSend(player, "&cYa hay una pelea de boss activa.");
            return;
        }

        this.boss = player;
        this.fakeDeathState = false;
        
        // Create global boss bar
        this.globalBossBar = BossBar.bossBar(
                MiniMessage.miniMessage().deserialize("<dark_red><bold>" + boss.getName() + " Desatado"),
                1.0f,
                BossBar.Color.RED,
                BossBar.Overlay.NOTCHED_10
        );
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showBossBar(this.globalBossBar);
            Messenger.showTitle(
                    p, 
                    "<color:#FF0055><bold>El Momento Ha Llegado</bold></color>", 
                    "<white>¡La batalla final ha comenzado!</white>", 
                    Duration.ofMillis(500), 
                    Duration.ofSeconds(4), 
                    Duration.ofMillis(1000)
            );
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
        }

        setPhase(1);
        saveState(Tezzlar.getConfigManager());
    }

    public void setPhase(int phase) {
        if (this.boss == null) return;
        
        if (this.currentPhase != 0 && this.currentPhase != phase) {
            String roman = phase == 1 ? "I" : phase == 2 ? "II" : phase == 3 ? "III" : "IV";
            for (Player p : Bukkit.getOnlinePlayers()) {
                Messenger.showTitle(
                        p, 
                        "<color:#FF0055><bold>Fase " + roman + "</bold></color>", 
                        "<white>" + boss.getName() + " se ha levantado nuevamente.</white>", 
                        Duration.ofMillis(500), 
                        Duration.ofSeconds(4), 
                        Duration.ofMillis(1000)
                );
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
            }
        }
        
        this.currentPhase = phase;
        this.fakeDeathState = false;
        
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }

        boss.setGameMode(GameMode.SURVIVAL);
        boss.setInvisible(false);
        
        AttributeInstance maxHealth = boss.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance scale = boss.getAttribute(Attribute.SCALE);
        AttributeInstance moveSpeed = boss.getAttribute(Attribute.MOVEMENT_SPEED);
        AttributeInstance blockRange = boss.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        AttributeInstance entityRange = boss.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        
        // Clear all potion effects
        for (PotionEffect effect : boss.getActivePotionEffects()) {
            boss.removePotionEffect(effect.getType());
        }

        switch (phase) {
            case 1:
                if (maxHealth != null) maxHealth.setBaseValue(500.0);
                if (scale != null) scale.setBaseValue(1.0);
                if (moveSpeed != null) moveSpeed.setBaseValue(0.1); // Default
                if (blockRange != null) blockRange.setBaseValue(50.0);
                if (entityRange != null) entityRange.setBaseValue(50.0);
                boss.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 3, false, false));
                globalBossBar.color(BossBar.Color.RED);
                break;
            case 2:
                if (maxHealth != null) maxHealth.setBaseValue(2000.0);
                if (scale != null) scale.setBaseValue(4.0);
                if (moveSpeed != null) moveSpeed.setBaseValue(0.15);
                if (blockRange != null) blockRange.setBaseValue(50.0);
                if (entityRange != null) entityRange.setBaseValue(50.0);
                globalBossBar.color(BossBar.Color.PURPLE);
                break;
            case 3:
                if (maxHealth != null) maxHealth.setBaseValue(4000.0);
                if (scale != null) scale.setBaseValue(8.0);
                if (moveSpeed != null) moveSpeed.setBaseValue(0.2);
                if (blockRange != null) blockRange.setBaseValue(50.0);
                if (entityRange != null) entityRange.setBaseValue(50.0);
                globalBossBar.color(BossBar.Color.PINK);
                break;
            case 4:
                if (maxHealth != null) maxHealth.setBaseValue(1000.0);
                if (scale != null) scale.setBaseValue(16.0);
                if (moveSpeed != null) moveSpeed.setBaseValue(0.25);
                if (blockRange != null) blockRange.setBaseValue(50.0);
                if (entityRange != null) entityRange.setBaseValue(50.0);
                globalBossBar.color(BossBar.Color.WHITE);
                break;
        }

        boss.setHealth(boss.getAttribute(Attribute.MAX_HEALTH).getValue());
        updateBossBar();
        BossItems.giveBossItems(boss);
    }

    public void updateBossBar() {
        if (boss == null || globalBossBar == null) return;
        
        AttributeInstance maxHealthAttr = boss.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr == null) return;
        
        double maxHealth = maxHealthAttr.getValue();
        double currentHealth = boss.getHealth();
        float progress = (float) (currentHealth / maxHealth);
        
        if (progress < 0f) progress = 0f;
        if (progress > 1f) progress = 1f;
        
        globalBossBar.progress(progress);
    }

    public void triggerFakeDeath() {
        if (boss == null || fakeDeathState) return;
        
        fakeDeathState = true;
        boss.setHealth(1.0); // Keep alive
        boss.setGameMode(GameMode.SPECTATOR); // Alternatively, invisible and invulnerable
        
        if (globalBossBar != null) {
            globalBossBar.progress(0f);
        }

        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss == null || !boss.isOnline() || !fakeDeathState) {
                    this.cancel();
                    return;
                }
                boss.getWorld().spawnParticle(Particle.PORTAL, boss.getLocation(), 50, 1.0, 1.0, 1.0, 0.1);
            }
        }.runTaskTimer(Tezzlar.getInstance(), 0L, 5L);
    }

    public void stopFight() {
        if (globalBossBar != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hideBossBar(globalBossBar);
            }
            globalBossBar = null;
        }

        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }

        if (boss != null) {
            boss.setGameMode(GameMode.SURVIVAL);
            boss.setInvisible(false);
            
            AttributeInstance maxHealth = boss.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) maxHealth.setBaseValue(20.0);
            
            AttributeInstance scale = boss.getAttribute(Attribute.SCALE);
            if (scale != null) scale.setBaseValue(1.0);
            
            AttributeInstance moveSpeed = boss.getAttribute(Attribute.MOVEMENT_SPEED);
            if (moveSpeed != null) moveSpeed.setBaseValue(0.1);
            
            AttributeInstance blockRange = boss.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
            if (blockRange != null) blockRange.setBaseValue(4.5);
            
            AttributeInstance entityRange = boss.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
            if (entityRange != null) entityRange.setBaseValue(3.0);
            
            boss.setHealth(20.0);
            
            for (PotionEffect effect : boss.getActivePotionEffects()) {
                boss.removePotionEffect(effect.getType());
            }
            
            BossItems.removeBossItems(boss);
        }

        this.boss = null;
        this.pendingBossUuid = null;
        this.currentPhase = 0;
        this.fakeDeathState = false;
        
        saveState(Tezzlar.getConfigManager());
    }

    public Player getBoss() {
        return boss;
    }

    public boolean isBoss(Player player) {
        return boss != null && boss.getUniqueId().equals(player.getUniqueId());
    }

    public int getCurrentPhase() {
        return currentPhase;
    }

    public boolean isFakeDeathState() {
        return fakeDeathState;
    }
    
    public BossBar getGlobalBossBar() {
        return globalBossBar;
    }

    // --- Persistence ---

    public void saveState(ConfigManager config) {
        boolean active = (boss != null || pendingBossUuid != null);
        config.updateBoolean("bossfight.active", active, null);
        
        if (active) {
            UUID uuid = boss != null ? boss.getUniqueId() : pendingBossUuid;
            config.updateString("bossfight.boss_uuid", uuid.toString(), null);
            config.updateInt("bossfight.current_phase", currentPhase, null);
            config.updateBoolean("bossfight.fake_death", fakeDeathState, null);
        } else {
            config.updateString("bossfight.boss_uuid", "", null);
        }
    }

    public void loadState(ConfigManager config) {
        boolean active = config.getBoolean("bossfight.active", false);
        if (!active) return;
        
        String uuidStr = config.getString("bossfight.boss_uuid", "");
        if (uuidStr.isEmpty()) return;
        
        this.pendingBossUuid = UUID.fromString(uuidStr);
        this.currentPhase = config.getInt("bossfight.current_phase", 1);
        this.fakeDeathState = config.getBoolean("bossfight.fake_death", false);
        
        Player p = Bukkit.getPlayer(pendingBossUuid);
        if (p != null && p.isOnline()) {
            resumeFight(p);
        }
    }

    public void resumeFight(Player player) {
        this.boss = player;
        this.pendingBossUuid = null;
        
        if (this.globalBossBar == null) {
            this.globalBossBar = BossBar.bossBar(
                    MiniMessage.miniMessage().deserialize("<dark_red><bold>" + boss.getName() + " Desatado"),
                    1.0f,
                    BossBar.Color.RED,
                    BossBar.Overlay.NOTCHED_10
            );
        }
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showBossBar(this.globalBossBar);
        }
        
        if (fakeDeathState) {
            triggerFakeDeath();
        } else {
            // Restore colors based on phase
            switch (currentPhase) {
                case 1: globalBossBar.color(BossBar.Color.RED); break;
                case 2: globalBossBar.color(BossBar.Color.PURPLE); break;
                case 3: globalBossBar.color(BossBar.Color.PINK); break;
                case 4: globalBossBar.color(BossBar.Color.WHITE); break;
            }
        }
        updateBossBar();
    }

    public void pauseFight() {
        if (globalBossBar != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hideBossBar(globalBossBar);
            }
        }
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
        
        if (boss != null) {
            this.pendingBossUuid = boss.getUniqueId();
            this.boss = null;
        }
    }

    public UUID getPendingBossUuid() {
        return pendingBossUuid;
    }
}

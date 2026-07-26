package com.panita.tezzlar3.bossfight.util;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
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

public class BossManager {

    private static BossManager instance;
    
    private Player boss;
    private int currentPhase;
    private boolean fakeDeathState;
    private BossBar globalBossBar;
    private BukkitTask particleTask;

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
                MiniMessage.miniMessage().deserialize("<dark_red><bold>Javithor360 Desatado"),
                1.0f,
                BossBar.Color.RED,
                BossBar.Overlay.NOTCHED_10
        );
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showBossBar(this.globalBossBar);
        }

        setPhase(1);
    }

    public void setPhase(int phase) {
        if (this.boss == null) return;
        
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
        
        // Clear all potion effects
        for (PotionEffect effect : boss.getActivePotionEffects()) {
            boss.removePotionEffect(effect.getType());
        }

        switch (phase) {
            case 1:
                if (maxHealth != null) maxHealth.setBaseValue(500.0);
                if (scale != null) scale.setBaseValue(1.0);
                boss.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 3, false, false));
                globalBossBar.color(BossBar.Color.RED);
                break;
            case 2:
                if (maxHealth != null) maxHealth.setBaseValue(2000.0);
                if (scale != null) scale.setBaseValue(4.0);
                globalBossBar.color(BossBar.Color.PURPLE);
                break;
            case 3:
                if (maxHealth != null) maxHealth.setBaseValue(4000.0);
                if (scale != null) scale.setBaseValue(8.0);
                globalBossBar.color(BossBar.Color.PINK);
                break;
            case 4:
                if (maxHealth != null) maxHealth.setBaseValue(1000.0);
                if (scale != null) scale.setBaseValue(16.0);
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
            
            boss.setHealth(20.0);
            
            for (PotionEffect effect : boss.getActivePotionEffects()) {
                boss.removePotionEffect(effect.getType());
            }
            
            BossItems.removeBossItems(boss);
        }

        this.boss = null;
        this.currentPhase = 0;
        this.fakeDeathState = false;
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
}

package com.panita.tezzlar3.missions.encyclopedia.listeners;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.Mission;
import com.panita.tezzlar3.missions.encyclopedia.data.EncyclopediaManager;
import com.panita.tezzlar3.missions.encyclopedia.data.EncyclopediaRecord;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.timeline.util.TimeManager;

public class EncyclopediaDeathListener implements Listener {
    private final JavaPlugin plugin;
    private final EncyclopediaManager manager;

    public EncyclopediaDeathListener(JavaPlugin plugin, EncyclopediaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Mission mission = MissionsModule.getMissionManager().getMission("mob_encyclopedia");
        if (mission == null) return;
        int currentDay = TimeManager.getCurrentDay();
        if (currentDay < mission.getStartDay() || currentDay > mission.getEndDay()) {
            return;
        }

        EntityType type = event.getEntity().getType();
        if (!manager.isTargetMob(type)) return;
        
        if (manager.isMobCompleted(type)) return;

        EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();
        if (lastDamage == null) return;

        Player killer = event.getEntity().getKiller();
        
        // If the player didn't deal the final blow directly, killer is null.
        // For environmental kills (fire, fall, drowning, etc), we attribute the kill to the nearest player within 15 blocks.
        if (killer == null) {
            // If it's direct entity damage (e.g. mob vs mob), we ignore it to prevent false positives.
            if (lastDamage instanceof EntityDamageByEntityEvent) {
                return;
            }
            
            double closestDist = Double.MAX_VALUE;
            Player closest = null;
            for (Player p : event.getEntity().getWorld().getPlayers()) {
                double dist = p.getLocation().distanceSquared(event.getEntity().getLocation());
                if (dist <= 225 && dist < closestDist) { // 15 blocks squared
                    closestDist = dist;
                    closest = p;
                }
            }
            killer = closest;
        }

        if (killer == null) return;

        String deathMethod = getDeathMethod(lastDamage, killer);
        if (deathMethod == null) return;

        // Validate teamwork: the killer cannot repeat the same death method
        if (manager.hasPlayerUsedMethod(killer.getName(), deathMethod)) {
            // Already used this death method
            return;
        }

        // Register record
        EncyclopediaRecord record = new EncyclopediaRecord(
                type,
                deathMethod,
                killer.getName(),
                System.currentTimeMillis(),
                event.getEntity().getWorld().getEnvironment().name(),
                EncyclopediaRecord.Status.APPROVED
        );

        manager.addRecord(record);

        // Broadcast success
        Messenger.prefixedBroadcast("&a¡" + killer.getName() + " ha registrado un(a) &e" + type.name() + "&a usando &e" + deathMethod + "&a para la Enciclopedia!");
        Messenger.prefixedBroadcast("&7Progreso: &e" + manager.getCompletedCount() + "&7/&e" + manager.getTotalCount());
    }

    private String getDeathMethod(EntityDamageEvent event, Player killer) {
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            Material weapon = killer.getInventory().getItemInMainHand().getType();
            if (weapon == Material.AIR) {
                return "ATTACK_FIST";
            }
            return "ATTACK_" + weapon.name();
        }

        if (cause == EntityDamageEvent.DamageCause.PROJECTILE && event instanceof EntityDamageByEntityEvent damageByEntity) {
            if (damageByEntity.getDamager() instanceof Projectile proj) {
                return "PROJECTILE_" + proj.getType().name();
            }
            return "PROJECTILE_UNKNOWN";
        }

        if (cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION && event instanceof EntityDamageByEntityEvent damageByEntity) {
            return "EXPLOSION_" + damageByEntity.getDamager().getType().name();
        }

        return "CAUSE_" + cause.name();
    }
}

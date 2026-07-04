package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.minievents.MiniEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class OppositeHourEvent implements MiniEvent, Listener {

    private final Map<PotionEffectType, PotionEffectType> opposites = new HashMap<>();
    private final ThreadLocal<Boolean> processing = ThreadLocal.withInitial(() -> false);

    public OppositeHourEvent() {
        // Bi-directional mappings
        addMapping(PotionEffectType.REGENERATION, PotionEffectType.WITHER);
        addMapping(PotionEffectType.INSTANT_HEALTH, PotionEffectType.INSTANT_DAMAGE);
        addMapping(PotionEffectType.STRENGTH, PotionEffectType.WEAKNESS);
        addMapping(PotionEffectType.HASTE, PotionEffectType.MINING_FATIGUE);
        addMapping(PotionEffectType.NIGHT_VISION, PotionEffectType.BLINDNESS);
        addMapping(PotionEffectType.WATER_BREATHING, PotionEffectType.FIRE_RESISTANCE);
        addMapping(PotionEffectType.SPEED, PotionEffectType.SLOWNESS);
        addMapping(PotionEffectType.HERO_OF_THE_VILLAGE, PotionEffectType.RAID_OMEN);
    }

    private void addMapping(PotionEffectType type1, PotionEffectType type2) {
        if (type1 != null && type2 != null) {
            opposites.put(type1, type2);
            opposites.put(type2, type1);
        }
    }

    @Override
    public void start(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop(JavaPlugin plugin) {
        HandlerList.unregisterAll(this);
    }

    @Override
    public String getId() {
        return "opposite_hour";
    }

    @Override
    public String getDisplayName() {
        return "<#8A2BE2>La Hora Opuesta</#8A2BE2>";
    }

    @Override
    public String getDescription() {
        return "\n&7Durante las próximas &b4 horas&7, la lógica se invierte.\n\n&3- &7Varios efectos de pociones ahora aplicarán su contraparte opuesta. ¡Cuidado con lo que bebes!";
    }

    @Override
    public long getDurationTicks() {
        return 4 * 60 * 60 * 20L; // 4 hours
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!PlayerUtils.isSurvival(player)) return;
        
        // Prevent infinite loops when we apply the opposite effect
        if (processing.get()) return;

        EntityPotionEffectEvent.Action action = event.getAction();
        if (action == EntityPotionEffectEvent.Action.ADDED || action == EntityPotionEffectEvent.Action.CHANGED) {
            PotionEffect newEffect = event.getNewEffect();
            if (newEffect == null) return;

            PotionEffectType oppositeType = opposites.get(newEffect.getType());
            if (oppositeType != null) {
                // Cancel the original effect application
                event.setCancelled(true);
                
                // Apply the opposite effect
                PotionEffect oppositeEffect = new PotionEffect(
                        oppositeType, 
                        newEffect.getDuration(), 
                        newEffect.getAmplifier(), 
                        newEffect.isAmbient(), 
                        newEffect.hasParticles(), 
                        newEffect.hasIcon()
                );

                processing.set(true);
                try {
                    player.addPotionEffect(oppositeEffect);
                } finally {
                    processing.set(false);
                }
            }
        }
    }
}

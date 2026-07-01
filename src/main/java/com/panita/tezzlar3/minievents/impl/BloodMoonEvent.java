package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.minievents.MiniEvent;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class BloodMoonEvent implements MiniEvent, Listener {

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
        return "blood_moon";
    }

    @Override
    public String getDisplayName() {
        return "<#821008>Luna de Sangre</#821008>";
    }

    @Override
    public String getDescription() {
        return "\n&7Durante las próximas &b2 horas&7, los mobs hostiles portarán cabeza de calabaza. El poder de la luna les otorga:\n\n&3- &7Salud aumentada al triple.\n&3- &7Daño de ataque triple.\n&3- &7Efectos de poción para combate.\n\n&7Por lo tanto, no se recomienda permanecer en solitario.";
    }

    @Override
    public long getDurationTicks() {
        return 2 * 60 * 60 * 20L; // 2 hours
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Monster monster) {
            
            // Pumpkin Head
            if (monster.getEquipment() != null) {
                if (Math.random() < 0.10) {
                    monster.getEquipment().setHelmet(new ItemStack(Material.JACK_O_LANTERN));
                } else {
                    monster.getEquipment().setHelmet(new ItemStack(Material.CARVED_PUMPKIN));
                }
                monster.getEquipment().setHelmetDropChance(0.0f);
            }
            
            // Triple Max Health
            AttributeInstance maxHp = monster.getAttribute(Attribute.MAX_HEALTH);
            if (maxHp != null) {
                maxHp.setBaseValue(maxHp.getBaseValue() * 3.0);
                monster.setHealth(maxHp.getBaseValue());
            }
            
            // Triple Attack Damage
            AttributeInstance damage = monster.getAttribute(Attribute.ATTACK_DAMAGE);
            if (damage != null) {
                damage.setBaseValue(damage.getBaseValue() * 3.0);
            }
            
            // Random Normal Potion Effect
            PotionEffectType[] normalEffects = {
                PotionEffectType.SPEED, PotionEffectType.RESISTANCE, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.REGENERATION
            };
            
            Random rnd = new Random();
            PotionEffectType chosenNormal = normalEffects[rnd.nextInt(normalEffects.length)];
            
            monster.addPotionEffect(new PotionEffect(chosenNormal, Integer.MAX_VALUE, 0));
            
            // Random Annoying Effect
            EntityUtils.applyAnnoyingSpecialEffect(monster);
        }
    }
}

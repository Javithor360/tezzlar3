package com.panita.tezzlar3.minievents.impl;

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
        return "<dark_red><b>Luna de Sangre</b></dark_red>";
    }

    @Override
    public String getDescription() {
        return "<gray>Los monstruos aparecerán con cabeza de calabaza, vida y daño x3, junto a poderosas pociones permanentes.</gray>";
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
            
            // Random Potion Effect (1 of each list)
            PotionEffectType[] normalEffects = {
                PotionEffectType.SPEED, PotionEffectType.RESISTANCE, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.REGENERATION
            };
            PotionEffectType[] specialEffects = {
                PotionEffectType.OOZING, PotionEffectType.WEAVING, PotionEffectType.INFESTED
            };
            
            java.util.Random rnd = new java.util.Random();
            PotionEffectType chosenNormal = normalEffects[rnd.nextInt(normalEffects.length)];
            PotionEffectType chosenSpecial = specialEffects[rnd.nextInt(specialEffects.length)];
            
            monster.addPotionEffect(new PotionEffect(chosenNormal, Integer.MAX_VALUE, 0));
            monster.addPotionEffect(new PotionEffect(chosenSpecial, Integer.MAX_VALUE, 0));
        }
    }
}

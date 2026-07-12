package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.core.util.MobGearUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarManager;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarProvider;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import org.bukkit.Location;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZombieCameramanMechanic extends DifficultyMechanic implements Listener, ActionBarProvider {

    private final NamespacedKey CAMERAMAN_KEY;
    private final Random random = new Random();
    
    // Map of player UUID to a list of active effects
    private final Map<UUID, List<CameraEffect>> activeEffects = new ConcurrentHashMap<>();

    private enum CameraAttribute {
        ARMOR(Attribute.ARMOR, 10.0),
        ARMOR_TOUGHNESS(Attribute.ARMOR_TOUGHNESS, 4.0),
        ATTACK_DAMAGE(Attribute.ATTACK_DAMAGE, 5.0),
        ATTACK_KNOCKBACK(Attribute.ATTACK_KNOCKBACK, 2.0),
        ATTACK_SPEED(Attribute.ATTACK_SPEED, 4.0),
        BLOCK_BREAK_SPEED(Attribute.BLOCK_BREAK_SPEED, 3.0),
        BLOCK_INTERACTION_RANGE(Attribute.BLOCK_INTERACTION_RANGE, 4.5),
        BOUNCINESS(Attribute.BOUNCINESS, 10.0),
        CAMERA_DISTANCE(Attribute.CAMERA_DISTANCE, 10.0),
        FALL_DAMAGE_MULTIPLIER(Attribute.FALL_DAMAGE_MULTIPLIER, 4.0),
        GRAVITY(Attribute.GRAVITY, 0.08),
        JUMP_STRENGTH(Attribute.JUMP_STRENGTH, 0.42),
        KNOCKBACK_RESISTANCE(Attribute.KNOCKBACK_RESISTANCE, 0.2),
        MAX_HEALTH(Attribute.MAX_HEALTH, 10.0),
        MINING_EFFICIENCY(Attribute.MINING_EFFICIENCY, 5.0),
        SPEED(Attribute.MOVEMENT_SPEED, 0.7),
        SCALE(Attribute.SCALE, 0.5);

        private final Attribute attribute;
        private final double maxOffset;

        CameraAttribute(Attribute attribute, double maxOffset) {
            this.attribute = attribute;
            this.maxOffset = maxOffset;
        }

        public Attribute getAttribute() { return attribute; }
        public double getMaxOffset() { return maxOffset; }
    }

    private static class CameraEffect {
        final CameraAttribute type;
        final double value;
        final long expirationTime;
        final AttributeModifier modifier;

        CameraEffect(CameraAttribute type, double value, long durationMs, AttributeModifier modifier) {
            this.type = type;
            this.value = value;
            this.expirationTime = System.currentTimeMillis() + durationMs;
            this.modifier = modifier;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() >= expirationTime;
        }
        
        long getRemainingSeconds() {
            return Math.max(0, (expirationTime - System.currentTimeMillis()) / 1000);
        }
    }

    public ZombieCameramanMechanic(JavaPlugin plugin) {
        super(plugin, 21);
        CAMERAMAN_KEY = new NamespacedKey(plugin, "is_cameraman");
        CustomMobManager.register(CustomMobType.ZOMBIE_CAMERAMAN, this::spawnManual);
        
        if (ActionBarManager.getInstance() != null) {
            ActionBarManager.getInstance().registerProvider(this);
        }
        
        // Task to clean up expired effects
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isActive()) return;
            
            for (Map.Entry<UUID, List<CameraEffect>> entry : activeEffects.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                List<CameraEffect> effects = entry.getValue();
                
                Iterator<CameraEffect> it = effects.iterator();
                while (it.hasNext()) {
                    CameraEffect effect = it.next();
                    if (effect.isExpired()) {
                        if (player != null && player.isOnline()) {
                            AttributeInstance inst = player.getAttribute(effect.type.getAttribute());
                            if (inst != null) {
                                inst.removeModifier(effect.modifier);
                            }
                        }
                        it.remove();
                    }
                }
                
                if (effects.isEmpty()) {
                    activeEffects.remove(entry.getKey());
                }
            }
        }, 20L, 20L);
    }

    public void spawnManual(Location loc) {
        Zombie zombie = (Zombie) EntityUtils.spawnNatural(loc, EntityType.ZOMBIE);
        transform(zombie);
    }

    private void transform(Zombie zombie) {
        zombie.getPersistentDataContainer().set(CAMERAMAN_KEY, PersistentDataType.BYTE, (byte) 1);
        EntityUtils.setCustomName(zombie, "&aZombie Camarógrafo");
        
        // Base 40 HP
        AttributeInstance healthAttr = zombie.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(40.0);
            zombie.setHealth(40.0);
        }
        
        // Equip with standard gear first
        MobGearUtils.equipRandomGear(zombie);

        // Generate custom head
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            try {
                // Get the URL from the base64 value
                textures.setSkin(new URL("http://textures.minecraft.net/texture/14422a82c899a9c1454384d32cc54c4ae7a1c4d72430e6e446d53b8b385e330"));
                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            head.setItemMeta(meta);
        }
        
        if (zombie.getEquipment() != null) {
            zombie.getEquipment().setHelmet(head);
            zombie.getEquipment().setHelmetDropChance(0.0f);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onZombieSpawn(org.bukkit.event.entity.CreatureSpawnEvent event) {
        if (!isActive()) return;
        
        if (event.getEntity() instanceof Zombie zombie) {
            if (EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) {
                // 10% chance to become a cameraman
                if (random.nextDouble() < 0.10) {
                    transform(zombie);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        
        if (event.getDamager() instanceof Zombie zombie && event.getEntity() instanceof Player player) {
            if (zombie.getPersistentDataContainer().has(CAMERAMAN_KEY, PersistentDataType.BYTE)) {
                
                CameraAttribute[] values = CameraAttribute.values();
                CameraAttribute chosen = values[random.nextInt(values.length)];
                
                // + or -
                double offset = chosen.getMaxOffset();
                if (random.nextBoolean()) {
                    offset = -offset;
                }
                
                AttributeInstance inst = player.getAttribute(chosen.getAttribute());
                if (inst != null) {
                    NamespacedKey modKey = new NamespacedKey(plugin, "cameraman_" + UUID.randomUUID().toString());
                    AttributeModifier modifier = new AttributeModifier(modKey, offset, AttributeModifier.Operation.ADD_NUMBER);
                    
                    inst.addModifier(modifier);
                    
                    CameraEffect effect = new CameraEffect(chosen, offset, 3 * 60 * 1000, modifier);
                    
                    activeEffects.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(effect);
                }
            }
        }
    }

    @Override
    public String getId() {
        return "zombie_cameraman_effects";
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!isActive()) return;
        
        Player player = event.getPlayer();
        List<CameraEffect> effects = activeEffects.remove(player.getUniqueId());
        if (effects != null) {
            for (CameraEffect effect : effects) {
                AttributeInstance inst = player.getAttribute(effect.type.getAttribute());
                if (inst != null) {
                    inst.removeModifier(effect.modifier);
                }
            }
        }
    }

    @Override
    public List<String> getTexts(Player player) {
        if (!isActive()) return null;
        List<CameraEffect> effects = activeEffects.get(player.getUniqueId());
        if (effects == null || effects.isEmpty()) return null;
        
        List<String> msgs = new ArrayList<>();
        for (CameraEffect e : effects) {
            long secs = e.getRemainingSeconds();
            String time = Global.formatTimeTicks(secs * 20L);
            String sign = e.value > 0 ? "+" : "";
            
            // Format name to Title Case
            String rawName = e.type.name().toLowerCase().replace('_', ' ');
            StringBuilder titleCase = new StringBuilder();
            for (String word : rawName.split(" ")) {
                if (word.length() > 0) {
                    titleCase.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
                }
            }
            String formattedName = titleCase.toString().trim();
            
            msgs.add("<gray>Atributo: <#BABABA>" + formattedName + " " + sign + e.value + "</#BABABA> (" + time + ")</gray>");
        }
        return msgs;
    }
}

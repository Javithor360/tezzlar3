package com.panita.tezzlar3.difficulty.mechanics;

import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.util.PlayerUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;
import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;

public class ApocalypticZombieMechanic extends DifficultyMechanic {

    public ApocalypticZombieMechanic(JavaPlugin plugin) {
        super(plugin, 19);
        CustomMobManager.register(CustomMobType.APOCALYPTIC_ZOMBIE, this::spawnManual);
    }

    public void spawnManual(Location loc) {
        org.bukkit.entity.Zombie zombie = loc.getWorld().spawn(loc, org.bukkit.entity.Zombie.class);
        transform(zombie);
    }

    private void transform(LivingEntity entity) {
        // Strength II
        entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, false));

        // Glowing Gold
        EntityUtils.setColoredGlowing(entity, NamedTextColor.GOLD);

        // Scale 1.6
        AttributeInstance scale = entity.getAttribute(Attribute.SCALE);
        if (scale != null) {
            scale.setBaseValue(1.6);
        }

        // Equip Custom Banner
        ItemStack banner = new ItemStack(Material.BLACK_BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        if (meta != null) {
            meta.addPattern(new Pattern(DyeColor.GREEN, PatternType.CURLY_BORDER));
            meta.addPattern(new Pattern(DyeColor.GREEN, PatternType.CROSS));
            meta.addPattern(new Pattern(DyeColor.GREEN, PatternType.TRIANGLE_TOP));
            meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
            meta.addPattern(new Pattern(DyeColor.WHITE, PatternType.TRIANGLES_BOTTOM));
            meta.addPattern(new Pattern(DyeColor.GREEN, PatternType.BORDER));
            banner.setItemMeta(meta);
        }
        
        if (entity.getEquipment() != null) {
            entity.getEquipment().setHelmet(banner);
            entity.getEquipment().setHelmetDropChance(0.5f); // 50% drop chance
        }

        // Tag it
        NamespacedKey key = new NamespacedKey(plugin, "apocalyptic_zombie");
        entity.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        EntityUtils.setCustomName(entity, "<gradient:#DB7A56:#F84E49>Zombie Apocalíptico</gradient>");

        // Reinforcements check task
        new BukkitRunnable() {
            boolean spawnedReinforcements = false;

            @Override
            public void run() {
                if (!entity.isValid() || entity.isDead()) {
                    this.cancel();
                    return;
                }

                if (!spawnedReinforcements) {
                    Player nearest = EntityUtils.getNearestPlayer(entity.getLocation(), 5.0);
                    if (nearest != null && PlayerUtils.isSurvival(nearest)) {
                        spawnedReinforcements = true;
                        int count = 5 + (int) (Math.random() * 6); // 5 to 10
                        for (int i = 0; i < count; i++) {
                            EntityUtils.spawnNatural(entity.getLocation(), EntityType.ZOMBIE);
                        }
                        nearest.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 2.0f, 0.5f);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onZombieSpawn(CreatureSpawnEvent event) {
        if (!isActive()) return;
        if (!EntityUtils.isValidNaturalSpawn(event.getSpawnReason())) return;

        EntityType type = event.getEntityType();
        if (type == EntityType.ZOMBIE || type == EntityType.ZOMBIE_VILLAGER || type == EntityType.HUSK || type == EntityType.DROWNED) {
            // 0.2% probability
            if (Math.random() < 0.002) {
                transform(event.getEntity());
            }
        }
    }
}

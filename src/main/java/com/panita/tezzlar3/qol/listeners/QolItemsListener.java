package com.panita.tezzlar3.qol.listeners;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.util.Vector;

import java.util.*;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.inventory.meta.ItemMeta;

public class QolItemsListener implements Listener {

    private final Map<UUID, Long> hornCooldowns = new HashMap<>();
    private final Map<UUID, Long> hornVulnerability = new HashMap<>();

    public QolItemsListener() {
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (CustomItemManager.isCustomItem(item, "copper_apple")) {
            int preFood = player.getFoodLevel();
            float preSat = player.getSaturation();
            
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 60, 0, false, true, true));
            
            // Revert food and saturation given by the vanilla apple behavior
            Bukkit.getScheduler().runTask(Tezzlar.getInstance(), () -> {
                player.setFoodLevel(preFood);
                player.setSaturation(preSat);
            });
        } else if (CustomItemManager.isCustomItem(item, "manzanium_apple")) {
            // Apply 20 food and 20 saturation 1 tick later
            Bukkit.getScheduler().runTask(Tezzlar.getInstance(), () -> {
                player.setFoodLevel(20);
                player.setSaturation(20f);
            });
        } else if (CustomItemManager.isCustomItem(item, "copper_carrot")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60, 0, false, true, true));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHornInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.GOAT_HORN) return;

        if (CustomItemManager.isCustomItem(item, "amethyst_horn")) {
            Player player = event.getPlayer();

            long now = System.currentTimeMillis();
            if (hornCooldowns.containsKey(player.getUniqueId())) {
                if (now - hornCooldowns.get(player.getUniqueId()) < 20000) {
                    return; // Prevent execution spam
                }
            }
            hornCooldowns.put(player.getUniqueId(), now);
            hornVulnerability.put(player.getUniqueId(), now + 8000);

            // Set vanilla visual cooldown to 20 seconds (400 ticks)
            player.setCooldown(Material.GOAT_HORN, 400);
            
            player.setFoodLevel(Math.max(0, player.getFoodLevel() - 4));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 160, 0));

            int count = 0;
            Random random = new Random();
            double[] fractions = {0.5, 0.33, 0.25, 0.2};

            for (Entity entity : player.getNearbyEntities(20, 20, 20)) {
                if (entity instanceof Mob mob) {
                    if (mob.getPersistentDataContainer().has(new NamespacedKey(Tezzlar.getInstance(), "giga_magma_cube"), PersistentDataType.BYTE)) continue;
                    
                    if (mob.getType() == EntityType.PHANTOM) {
                        Vector dir = mob.getLocation().toVector().subtract(player.getLocation().toVector());
                        if (dir.lengthSquared() < 0.0001) dir = player.getLocation().getDirection();
                        Vector push = dir.normalize().multiply(3.5).setY(1.0);
                        mob.setVelocity(push);
                        mob.getWorld().spawnParticle(Particle.SONIC_BOOM, mob.getLocation().add(0, 1, 0), 1);
                        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);
                        
                        double maxHealth = 20.0;
                        if (mob.getAttribute(Attribute.MAX_HEALTH) != null) {
                            maxHealth = mob.getAttribute(Attribute.MAX_HEALTH).getValue();
                        }
                        double damage = maxHealth * fractions[random.nextInt(fractions.length)];
                        mob.damage(damage, player);
                    } else {
                        double distance = player.getLocation().distance(mob.getLocation());
                        if (distance <= 5.0) {
                            Vector dir = mob.getLocation().toVector().subtract(player.getLocation().toVector());
                            if (dir.lengthSquared() < 0.0001) dir = player.getLocation().getDirection();
                            Vector push = dir.normalize().multiply(1.5).setY(0.5);
                            mob.setVelocity(push);
                            mob.getWorld().spawnParticle(Particle.SONIC_BOOM, mob.getLocation().add(0, 1, 0), 1);
                            mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);
                        }
                    }

                    mob.setAware(false);
                    EntityUtils.setColoredGlowing(mob, NamedTextColor.DARK_BLUE);

                    new BukkitRunnable() {
                        int passedTicks = 0;

                        @Override
                        public void run() {
                            if (!mob.isValid() || mob.isDead() || passedTicks >= 160) {
                                if (mob.isValid() && !mob.isDead()) {
                                    mob.setAware(true);
                                    EntityUtils.removeColoredGlowing(mob);
                                }
                                this.cancel();
                                return;
                            }
                            mob.getWorld().spawnParticle(Particle.CRIT, mob.getLocation().add(0, 2, 0), 5, 0.4, 0.4, 0.4, 0.0);
                            passedTicks += 10;
                        }
                    }.runTaskTimer(Tezzlar.getInstance(), 0L, 10L); // 8 seconds duration (runs every 0.5s)

                    count++;
                }
            }

            if (count > 0) {
                Messenger.prefixedSend(player, "<gold>¡Has paralizado a <white>" + count + " <gold>mobs por 8 segundos! <red>Eres vulnerable durante este tiempo.</red>");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamageHorn(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hornVulnerability.containsKey(player.getUniqueId())) {
                if (System.currentTimeMillis() < hornVulnerability.get(player.getUniqueId())) {
                    event.setDamage(event.getDamage() * 1.5);
                } else {
                    hornVulnerability.remove(player.getUniqueId());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHeartInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) return;

        if (CustomItemManager.isCustomItem(item, "tezzlar_heart")) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            
            // Add 1 heart container (+2 max health)
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                // Consume item
                item.setAmount(item.getAmount() - 1);
                
                EntityUtils.trySetAttribute(player, Attribute.MAX_HEALTH, maxHealth.getBaseValue() + 2.0);
                player.setHealth(Math.min(player.getHealth() + 2.0, maxHealth.getBaseValue()));
                
                SoundUtils.play(player, "entity.player.levelup", 1.0f, 1.2f);
                SoundUtils.play(player, "entity.wither.spawn", 0.5f, 2.0f);
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
                
                Messenger.prefixedSend(player, "<red>¡Has consumido un Corazón de Tezzlar y tu salud máxima ha aumentado!</red>");
            }
        } else if (CustomItemManager.isCustomItem(item, "tezzlar_heartnt")) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            
            // Remove 1 heart container (-2 max health)
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null && maxHealth.getBaseValue() > 2.0) {
                // Consume item
                item.setAmount(item.getAmount() - 1);
                
                EntityUtils.trySetAttribute(player, Attribute.MAX_HEALTH, maxHealth.getBaseValue() - 2.0);
                if (player.getHealth() > maxHealth.getBaseValue()) {
                    player.setHealth(maxHealth.getBaseValue());
                }
                
                SoundUtils.play(player, "entity.wither.hurt", 1.0f, 0.8f);
                player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
                
                Messenger.prefixedSend(player, "<dark_red>¡Has consumido un Corazón Maldito y tu salud máxima ha disminuido!</dark_red>");
            } else if (maxHealth != null) {
                Messenger.prefixedSend(player, "<red>No puedes reducir más tu salud máxima.</red>");
            }
        } else if (CustomItemManager.isCustomItem(item, "life_saver")) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            
            int currentLives = HardcoreDataManager.getLives(player.getUniqueId(), player.getName());
            int maxLives = HardcoreDataManager.getMaxLives(player.getUniqueId(), player.getName());
            
            if (currentLives < maxLives) {
                item.setAmount(item.getAmount() - 1);
                HardcoreDataManager.setLives(player.getUniqueId(), player.getName(), currentLives + 1);
                
                SoundUtils.play(player, "entity.player.levelup", 1.0f, 1.2f);
                SoundUtils.play(player, "entity.illusioner.prepare_mirror", 0.5f, 1.5f);
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
                
                Messenger.prefixedSend(player, "<green>¡Has consumido un Salva Vidas y has recuperado una vida!</green>");
            } else {
                Messenger.prefixedSend(player, "<red>Tus vidas ya están al máximo. No puedes consumir esto, pero puedes dárselo a quién lo necesite.</red>");
            }
        } else if (CustomItemManager.isCustomItem(item, "memory_evoker")) {
            event.setCancelled(true);
            Player player = event.getPlayer();

            String evokerWorldStr = Tezzlar.getConfigManager().getString("qol.memory_evoker.world", "world");
            int evokerX = Tezzlar.getConfigManager().getInt("qol.memory_evoker.x", 0);
            int evokerY = Tezzlar.getConfigManager().getInt("qol.memory_evoker.y", 64);
            int evokerZ = Tezzlar.getConfigManager().getInt("qol.memory_evoker.z", 0);
            double radius = Tezzlar.getConfigManager().getDouble("qol.memory_evoker.radius", 50.0);

            World evokerWorld = Bukkit.getWorld(evokerWorldStr);
            if (evokerWorld == null || !player.getWorld().equals(evokerWorld)) {
                Messenger.prefixedSend(player, "<red>Debes estar en el mundo correcto para usar el Memory Evoker.</red>");
                return;
            }

            Location center = new Location(evokerWorld, evokerX, evokerY, evokerZ);
            if (player.getLocation().distance(center) > radius) {
                Messenger.prefixedSend(player, "<red>Debes estar cerca del altar para utilizar el Memory Evoker.</red>");
                return;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) {
                Messenger.prefixedSend(player, "<red>Debes renombrar este ítem en un yunque con el nombre del jugador caído.</red>");
                return;
            }

            String targetName = PlainTextComponentSerializer.plainText().serialize(meta.displayName()).trim();

            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (target == null || !target.hasPlayedBefore()) {
                Messenger.prefixedSend(player, "<red>El jugador <yellow>" + targetName + "</yellow> no existe o no se ha unido al servidor.</red>");
                return;
            }

            long banExpiration = HardcoreDataManager.getBanExpiration(target.getUniqueId(), target.getName());
            if (banExpiration <= System.currentTimeMillis()) {
                Messenger.prefixedSend(player, "<red>El jugador <yellow>" + target.getName() + "</yellow> no está baneado actualmente por muertes.</red>");
                return;
            }

            item.setAmount(item.getAmount() - 1);
            spawnMemoryEvokerWave(player.getLocation(), target);
            
            Messenger.prefixedSend(player, "<gold>Has evocado los recuerdos de <yellow>" + target.getName() + "</yellow>. ¡Prepárate para luchar!</gold>");
            SoundUtils.play(player, "entity.wither.spawn", 1.0f, 0.5f);
        }
    }

    private void spawnMemoryEvokerWave(Location loc, OfflinePlayer target) {
        String waveId = UUID.randomUUID().toString();
        EntityType[] mobTypes = {EntityType.ZOMBIE, EntityType.HUSK, EntityType.SKELETON, EntityType.STRAY,
                EntityType.BOGGED, EntityType.PARCHED, EntityType.VINDICATOR, EntityType.PILLAGER, EntityType.EVOKER, EntityType.RAVAGER};
        Random random = new Random();
        
        int numMobs = 4;
        int keyholderIndex = random.nextInt(numMobs);
        String customName = "<gold>Evoca-recuerdos de " + target.getName() + "</gold>";
        
        for (int i = 0; i < numMobs; i++) {
            EntityType type = mobTypes[random.nextInt(mobTypes.length)];
            
            double offsetX = (random.nextDouble() * 10) - 5;
            double offsetZ = (random.nextDouble() * 10) - 5;
            Location spawnLoc = loc.clone().add(offsetX, 0, offsetZ);
            spawnLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);
            
            Mob mob = EntityUtils.spawnNatural(spawnLoc, type);
            if (mob == null) continue;
            
            EntityUtils.setCustomName(mob, customName, true);
            EntityUtils.setColoredGlowing(mob, NamedTextColor.LIGHT_PURPLE);
            mob.setRemoveWhenFarAway(false);
            
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            skullMeta.setOwningPlayer(target);
            head.setItemMeta(skullMeta);
            
            EntityEquipment equip = mob.getEquipment();
            if (equip != null) {
                equip.setHelmet(head);
                equip.setHelmetDropChance(0.0f);
            }
            
            mob.getPersistentDataContainer().set(new NamespacedKey(Tezzlar.getInstance(), "is_memory_mob"), PersistentDataType.STRING, waveId);
            mob.getPersistentDataContainer().set(new NamespacedKey(Tezzlar.getInstance(), "memory_target"), PersistentDataType.STRING, target.getUniqueId().toString());
            
            if (i == keyholderIndex) {
                mob.getPersistentDataContainer().set(new NamespacedKey(Tezzlar.getInstance(), "is_memory_keyholder"), PersistentDataType.BYTE, (byte) 1);
            }
            
            mob.getWorld().spawnParticle(Particle.SOUL, mob.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInfiniteBagClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        
        // Case 1: Click with an item (cursor) on the bundle (current)
        if (current != null && CustomItemManager.isCustomItem(current, "infinite_bag")) {
            if (cursor != null && !cursor.getType().isAir()) {
                event.setCancelled(true);
                event.getView().setCursor(null); // Delete item from cursor
                
                SoundUtils.play(player, "entity.item.pickup", 0.5f, 0.5f);
                Bukkit.getScheduler().runTask(Tezzlar.getInstance(), player::updateInventory);
            }
        }
        // Case 2: Pass the bundle (cursor) over an item (current)
        else if (cursor != null && CustomItemManager.isCustomItem(cursor, "infinite_bag")) {
            if (current != null && !current.getType().isAir()) {
                ItemStack bag = cursor.clone(); // Save a copy of the bundle
                
                // Clear any items that might have been visually stored in the bundle
                if (bag.getItemMeta() instanceof BundleMeta meta) {
                    meta.setItems(Collections.emptyList());
                    bag.setItemMeta(meta);
                }
                
                event.setCancelled(true);
                
                // Delete item from slot directly
                if (event.getClickedInventory() != null) {
                    event.getClickedInventory().setItem(event.getSlot(), null);
                }
                
                // Force the bundle back to the cursor
                event.getView().setCursor(bag);
                
                SoundUtils.play(player, "entity.item.pickup", 0.5f, 0.5f);
                Bukkit.getScheduler().runTask(Tezzlar.getInstance(), player::updateInventory);
            }
        }
    }
}

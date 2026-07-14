package com.panita.tezzlar3.missions.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.Mission;
import com.panita.tezzlar3.timeline.util.TimeManager;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.panita.tezzlar3.core.config.CustomConfig;
import com.panita.tezzlar3.core.util.EntityUtils;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class CTMEscortListener implements Listener {

    private final String MISSION_ID = "conquer_dungeon";
    private final List<String> WOOL_COLORS = Arrays.asList("orange", "red", "blue", "lime", "yellow", "purple");
    private final NamespacedKey CHOSEN_COLOR_KEY;
    private final NamespacedKey ARMOR_MOD_KEY;
    private final NamespacedKey HEALTH_BUFF_KEY;
    
    // We will force max health to 2.0 (1 heart) directly on the base value, so we save the original.
    private final Map<UUID, Double> originalHealth = new HashMap<>();
    private final Set<UUID> activeCarriers = new HashSet<>();
    private final Set<UUID> activeEscorts = new HashSet<>();

    public CTMEscortListener() {
        this.CHOSEN_COLOR_KEY = new NamespacedKey(Tezzlar.getInstance(), "ctm_chosen_color");
        this.ARMOR_MOD_KEY = new NamespacedKey(Tezzlar.getInstance(), "ctm_escort_armor");
        this.HEALTH_BUFF_KEY = new NamespacedKey(Tezzlar.getInstance(), "ctm_escort_health_buff");
        
        Bukkit.getScheduler().runTaskTimer(Tezzlar.getInstance(), this::tick, 10L, 10L);
    }

    private boolean isMissionActive() {
        Mission mission = MissionsModule.getMissionManager().getMission(MISSION_ID);
        if (mission == null) return false;
        int currentDay = TimeManager.getCurrentDay();
        return currentDay >= mission.getStartDay() && currentDay <= mission.getEndDay();
    }

    private String getWoolColor(ItemStack item) {
        if (!CustomItemManager.isCustomItem(item)) return null;
        for (String color : WOOL_COLORS) {
            if (CustomItemManager.isCustomItem(item, "monument_wool_" + color)) {
                return color;
            }
        }
        return null;
    }

    private void tick() {
        if (!isMissionActive()) return;

        Set<UUID> currentCarriers = new HashSet<>();
        Set<UUID> currentEscorts = new HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean isCarrier = false;
            String carriedColor = null;

            ItemStack mainHand = player.getInventory().getItemInMainHand();
            String mainHandColor = getWoolColor(mainHand);
            
            // Validate all other items in inventory
            boolean failed = false;
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                if (i == player.getInventory().getHeldItemSlot()) continue;
                ItemStack item = player.getInventory().getItem(i);
                if (getWoolColor(item) != null) {
                    player.getInventory().setItem(i, null);
                    failed = true;
                }
            }
            if (getWoolColor(player.getItemOnCursor()) != null) {
                player.setItemOnCursor(null);
                failed = true;
            }
            
            if (failed) {
                if (mainHandColor != null) {
                    player.getInventory().setItemInMainHand(null);
                }
                failEscort(player, "La lana debe permanecer en la mano principal en todo momento. Intento fallido.");
                continue;
            }

            if (mainHandColor != null) {
                // Check if they are allowed to carry this color
                String chosenColor = player.getPersistentDataContainer().get(CHOSEN_COLOR_KEY, PersistentDataType.STRING);
                if (chosenColor != null && !chosenColor.equals(mainHandColor)) {
                    player.getInventory().setItemInMainHand(null);
                    failEscort(player, "Ya fuiste portador de otro color (" + chosenColor + "). No puedes llevar la lana " + mainHandColor + ".");
                    continue;
                } else if (chosenColor == null) {
                    player.getPersistentDataContainer().set(CHOSEN_COLOR_KEY, PersistentDataType.STRING, mainHandColor);
                }
                
                isCarrier = true;
                carriedColor = mainHandColor;
                currentCarriers.add(player.getUniqueId());
            }

            if (isCarrier) {
                applyCarrierDebuffs(player, carriedColor);
                
                // Find escorts
                for (Entity entity : player.getNearbyEntities(25, 25, 25)) {
                    if (entity instanceof Player escort && !currentCarriers.contains(escort.getUniqueId())) {
                        currentEscorts.add(escort.getUniqueId());
                        applyEscortBuffs(escort);
                    }
                }
            }
        }

        // Cleanup players who are no longer carriers
        for (UUID uuid : activeCarriers) {
            if (!currentCarriers.contains(uuid)) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) removeCarrierDebuffs(p);
            }
        }
        activeCarriers.clear();
        activeCarriers.addAll(currentCarriers);

        // Cleanup players who are no longer escorts
        for (UUID uuid : activeEscorts) {
            if (!currentEscorts.contains(uuid)) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) removeEscortBuffs(p);
            }
        }
        activeEscorts.clear();
        activeEscorts.addAll(currentEscorts);
    }

    private void applyCarrierDebuffs(Player player, String color) {
        AttributeInstance healthInst = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthInst != null && healthInst.getBaseValue() != 2.0) {
            originalHealth.put(player.getUniqueId(), healthInst.getBaseValue());
            healthInst.setBaseValue(2.0);
        }

        AttributeInstance armorInst = player.getAttribute(Attribute.ARMOR);
        if (armorInst != null) {
            boolean hasMod = false;
            for (AttributeModifier mod : armorInst.getModifiers()) {
                if (mod.getKey().equals(ARMOR_MOD_KEY)) hasMod = true;
            }
            if (!hasMod) {
                armorInst.addModifier(new AttributeModifier(ARMOR_MOD_KEY, -1.0, AttributeModifier.Operation.ADD_SCALAR));
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 5, false, false, true));
        EntityUtils.setColoredGlowing(player, getGlowColor(color));
    }

    private NamedTextColor getGlowColor(String color) {
        switch (color) {
            case "orange": return NamedTextColor.GOLD;
            case "red": return NamedTextColor.RED;
            case "blue": return NamedTextColor.BLUE;
            case "lime": return NamedTextColor.GREEN;
            case "yellow": return NamedTextColor.YELLOW;
            case "purple": return NamedTextColor.DARK_PURPLE;
            default: return NamedTextColor.WHITE;
        }
    }

    private void removeCarrierDebuffs(Player player) {
        AttributeInstance healthInst = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthInst != null && originalHealth.containsKey(player.getUniqueId())) {
            healthInst.setBaseValue(originalHealth.remove(player.getUniqueId()));
        }

        AttributeInstance armorInst = player.getAttribute(Attribute.ARMOR);
        if (armorInst != null) {
            for (AttributeModifier mod : armorInst.getModifiers()) {
                if (mod.getKey().equals(ARMOR_MOD_KEY)) {
                    armorInst.removeModifier(mod);
                }
            }
        }
        
        EntityUtils.removeColoredGlowing(player);
    }

    private void applyEscortBuffs(Player player) {
        AttributeInstance healthInst = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthInst != null) {
            boolean hasMod = false;
            for (AttributeModifier mod : healthInst.getModifiers()) {
                if (mod.getKey().equals(HEALTH_BUFF_KEY)) hasMod = true;
            }
            if (!hasMod) {
                healthInst.addModifier(new AttributeModifier(HEALTH_BUFF_KEY, 6.0, AttributeModifier.Operation.ADD_NUMBER));
            }
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 0, false, false, true));
    }

    private void removeEscortBuffs(Player player) {
        AttributeInstance healthInst = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthInst != null) {
            for (AttributeModifier mod : healthInst.getModifiers()) {
                if (mod.getKey().equals(HEALTH_BUFF_KEY)) {
                    healthInst.removeModifier(mod);
                }
            }
        }
    }

    private void failEscort(Player trigger, String reason) {
        localBroadcast(trigger.getLocation(), "&c" + trigger.getName() + " ha fallado la escolta: " + reason);
        trigger.playSound(trigger.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
    }

    private void localBroadcast(Location center, String message) {
        for (Entity entity : center.getWorld().getNearbyEntities(center, 30, 30, 30)) {
            if (entity instanceof Player p) {
                Messenger.prefixedSend(p, message);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (!isMissionActive()) return;
        ItemStack dropped = event.getItemDrop().getItemStack();
        String color = getWoolColor(dropped);
        if (color != null) {
            event.getItemDrop().remove(); // Destroy it
            failEscort(event.getPlayer(), "La lana fue dropeada al suelo.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!isMissionActive()) return;
        Player player = event.getEntity();
        boolean hadWool = false;
        
        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack drop = iterator.next();
            if (getWoolColor(drop) != null) {
                iterator.remove();
                hadWool = true;
            }
        }
        
        if (hadWool) {
            failEscort(player, "El portador de la lana ha muerto en combate.");
        }
        
        removeCarrierDebuffs(player);
        removeEscortBuffs(player);
        originalHealth.remove(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removeCarrierDebuffs(player);
        removeEscortBuffs(player);
        originalHealth.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isMissionActive()) return;
        
        ItemStack placedItem = event.getItemInHand();
        String color = getWoolColor(placedItem);
        if (color == null) return;
        
        Player player = event.getPlayer();
        Mission mission = MissionsModule.getMissionManager().getMission(MISSION_ID);
        if (mission == null) return;
        
        // Ensure this player is the original carrier for this color
        String chosenColor = player.getPersistentDataContainer().get(CHOSEN_COLOR_KEY, PersistentDataType.STRING);
        if (chosenColor == null || !chosenColor.equals(color)) {
            event.setCancelled(true);
            Messenger.prefixedSend(player, "&cNo eres digno de colocar esta lana, solo el portador original (" + color + ") puede hacerlo.");
            return;
        }

        // Let's read directly from missions.yml using CustomConfig
        CustomConfig missionsConfig = new CustomConfig(Tezzlar.getInstance(), "", "missions.yml");
        String requiredLocStr = missionsConfig.getConfig().getString("missions." + MISSION_ID + ".objective.monuments." + color);
        
        boolean coordsMatch = false;
        if (requiredLocStr != null) {
            String[] parts = requiredLocStr.split(",");
            if (parts.length >= 4) {
                String w = parts[0];
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                
                Location blockLoc = event.getBlock().getLocation();
                if (blockLoc.getWorld().getName().equals(w) && blockLoc.getBlockX() == x && blockLoc.getBlockY() == y && blockLoc.getBlockZ() == z) {
                    coordsMatch = true;
                }
            }
        }
        
        if (!coordsMatch) {
            event.setCancelled(true);
            Messenger.prefixedSend(player, "&cEste no es el altar correcto para la lana " + color + ".");
            return;
        }
        
        // Success!
        player.getInventory().setItemInMainHand(null);
        removeCarrierDebuffs(player);
        activeCarriers.remove(player.getUniqueId());
        
        Location loc = event.getBlock().getLocation();
        localBroadcast(loc, "&a¡" + player.getName() + " ha colocado con éxito la lana " + color + " en el monumento!");
        
        loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc.clone().add(0.5, 1, 0.5), EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(Color.WHITE).with(FireworkEffect.Type.BALL_LARGE).build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
        
        // Advance mission progress globally
        MissionsModule.getGlobalMissionManager().addProgress(MISSION_ID, 1);
        int currentProgress = MissionsModule.getGlobalMissionManager().getProgress(MISSION_ID);
        if (currentProgress >= mission.getObjectiveAmount()) {
            Messenger.prefixedBroadcast("&6&l¡EL MONUMENTO HA SIDO COMPLETADO!");
            MissionsModule.getDataManager().giveRewardToEveryone(MISSION_ID);
        }
    }
}

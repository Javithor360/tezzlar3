package com.panita.tezzlar3.missions.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.Mission;
import com.panita.tezzlar3.timeline.util.TimeManager;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.*;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
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
    private final Map<String, Location> monumentLocations = new HashMap<>();
    private final Set<UUID> activeEscorts = new HashSet<>();
    private final Set<Location> analyzingBlocks = new HashSet<>();
    private Location exitDestination = null;
    private Location exitRegionMin = null;
    private Location exitRegionMax = null;

    public CTMEscortListener() {
        this.CHOSEN_COLOR_KEY = new NamespacedKey(Tezzlar.getInstance(), "ctm_chosen_color");
        this.ARMOR_MOD_KEY = new NamespacedKey(Tezzlar.getInstance(), "ctm_escort_armor");
        this.HEALTH_BUFF_KEY = new NamespacedKey(Tezzlar.getInstance(), "ctm_escort_health_buff");
        
        Bukkit.getScheduler().runTaskTimer(Tezzlar.getInstance(), this::tick, 10L, 10L);
    }

    private void loadMissionData() {
        if (!monumentLocations.isEmpty() && exitDestination != null) return;
        CustomConfig missionsConfig = new CustomConfig(Tezzlar.getInstance(), "", "missions.yml");
        for (String color : WOOL_COLORS) {
            String requiredLocStr = missionsConfig.getConfig().getString("missions." + MISSION_ID + ".objective.monuments." + color);
            if (requiredLocStr != null) {
                String[] parts = requiredLocStr.split(",");
                if (parts.length >= 4) {
                    World w = Bukkit.getWorld(parts[0]);
                    if (w != null) {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        int z = Integer.parseInt(parts[3]);
                        monumentLocations.put(color, new Location(w, x, y, z));
                    }
                }
            }
        }
        
        String destStr = missionsConfig.getConfig().getString("missions." + MISSION_ID + ".objective.dungeonExitPortal.destination");
        if (destStr != null) {
            String[] parts = destStr.split(",");
            if (parts.length >= 4) {
                World w = Bukkit.getWorld(parts[0]);
                if (w != null) {
                    exitDestination = new Location(w, Double.parseDouble(parts[1]) + 0.5, Double.parseDouble(parts[2]), Double.parseDouble(parts[3]) + 0.5);
                }
            }
        }
        
        String regionStr = missionsConfig.getConfig().getString("missions." + MISSION_ID + ".objective.dungeonExitPortal.region");
        if (regionStr != null) {
            String[] locs = regionStr.split(";");
            if (locs.length >= 2) {
                String[] p1 = locs[0].split(",");
                String[] p2 = locs[1].split(",");
                if (p1.length >= 4 && p2.length >= 4) {
                    World w1 = Bukkit.getWorld(p1[0]);
                    World w2 = Bukkit.getWorld(p2[0]);
                    if (w1 != null && w2 != null && w1.equals(w2)) {
                        int x1 = Integer.parseInt(p1[1]);
                        int y1 = Integer.parseInt(p1[2]);
                        int z1 = Integer.parseInt(p1[3]);
                        
                        int x2 = Integer.parseInt(p2[1]);
                        int y2 = Integer.parseInt(p2[2]);
                        int z2 = Integer.parseInt(p2[3]);
                        
                        exitRegionMin = new Location(w1, Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
                        exitRegionMax = new Location(w1, Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
                    }
                }
            }
        }
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
        if (!isMissionActive()) {
            monumentLocations.clear();
            exitDestination = null;
            return;
        }

        loadMissionData();
        for (Map.Entry<String, Location> entry : monumentLocations.entrySet()) {
            Location loc = entry.getValue();
            String color = entry.getKey();
            
            if (loc.getWorld() != null && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                if (analyzingBlocks.contains(loc)) {
                    continue;
                }
                
                if (!loc.getBlock().getType().name().endsWith("_WOOL")) {
                    loc.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.01);
                } else {
                    loc.getWorld().spawnParticle(Particle.ENCHANT, loc.clone().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.1);
                    
                    Particle.DustOptions dust = new Particle.DustOptions(getDustColor(color), 3.0F);
                    for (int y = loc.getBlockY() + 1; y <= 320; y += 2) {
                        loc.getWorld().spawnParticle(Particle.DUST, loc.getBlockX() + 0.5, y, loc.getBlockZ() + 0.5, 2, 0.1, 0, 0.1, 0, dust);
                    }
                }
            }
        }

        if (exitRegionMin != null && exitRegionMax != null) {
            World w = exitRegionMin.getWorld();
            if (w != null && w.isChunkLoaded(exitRegionMin.getBlockX() >> 4, exitRegionMin.getBlockZ() >> 4)) {
                for (int x = exitRegionMin.getBlockX(); x <= exitRegionMax.getBlockX(); x++) {
                    for (int y = exitRegionMin.getBlockY(); y <= exitRegionMax.getBlockY(); y++) {
                        for (int z = exitRegionMin.getBlockZ(); z <= exitRegionMax.getBlockZ(); z++) {
                            if (Math.random() < 0.3) {
                                w.spawnParticle(Particle.REVERSE_PORTAL, x + 0.5, y + 0.5, z + 0.5, 2, 0.2, 0.2, 0.2, 0.05);
                                w.spawnParticle(Particle.WITCH, x + 0.5, y + 0.5, z + 0.5, 1, 0.2, 0.2, 0.2, 0);
                            }
                        }
                    }
                }
            }
        }

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
                    failEscort(player, "Ya fuiste portador de otro color (" + translateColor(chosenColor) + "). No puedes llevar la lana " + translateColor(mainHandColor) + ".");
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
                
                drawEmeraldDome(player, 25.0);
            }
        }

        // Cleanup players who are no longer carriers
        for (UUID uuid : activeCarriers) {
            if (!currentCarriers.contains(uuid)) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) removeCarrierDebuffs(p);
            }
        }
        
        // Alert new carriers
        for (UUID uuid : currentCarriers) {
            if (!activeCarriers.contains(uuid)) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    String color = getWoolColor(p.getInventory().getItemInMainHand());
                    if (color != null) {
                        alertNewCarrier(p, color);
                    }
                }
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
    
    private void alertNewCarrier(Player carrier, String color) {
        String portalStr = "???";
        if (exitRegionMin != null && exitRegionMax != null) {
            int px = (exitRegionMin.getBlockX() + exitRegionMax.getBlockX()) / 2;
            int py = (exitRegionMin.getBlockY() + exitRegionMax.getBlockY()) / 2;
            int pz = (exitRegionMin.getBlockZ() + exitRegionMax.getBlockZ()) / 2;
            portalStr = px + " " + py + " " + pz;
        }

        String monumentStr = "???";
        Location mLoc = monumentLocations.get(color);
        if (mLoc != null) {
            monumentStr = mLoc.getBlockX() + " " + mLoc.getBlockY() + " " + mLoc.getBlockZ();
        }

        String woolName = translateColor(color);

        String carrierMsg = "&bHas agarrado la lana " + woolName + "&b, huye lo más pronto que puedas de esta isla yendo al &ePortal del Aether &bubicado en &e" + portalStr + "&b. Luego, dirígete al &eMonumento de las Lanas &bubicado en &e" + monumentStr + "&b.";
        
        String escortMsg = "&bSe ha agarrado la lana " + woolName + "&b, huye lo más pronto que puedas de esta isla con el portador yendo al &ePortal del Aether &bubicado en &e" + portalStr + "&b. Luego, dirígete al &eMonumento de las Lanas &bubicado en &e" + monumentStr + "&b.";

        Messenger.prefixedSend(carrier, carrierMsg);
        carrier.playSound(carrier.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.2f);
        
        for (Entity entity : carrier.getNearbyEntities(25, 25, 25)) {
            if (entity instanceof Player escort && !escort.equals(carrier)) {
                Messenger.prefixedSend(escort, escortMsg);
                escort.playSound(escort.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.2f);
            }
        }
    }
    
    private String translateColor(String color) {
        switch(color) {
            case "orange": return "&6Naranja";
            case "red": return "&cRoja";
            case "blue": return "&9Azul";
            case "lime": return "&aVerde";
            case "yellow": return "&eAmarilla";
            case "purple": return "&5Morada";
            default: return color;
        }
    }

    private org.bukkit.Color getDustColor(String color) {
        switch (color) {
            case "orange": return org.bukkit.Color.fromRGB(255, 127, 0);
            case "red": return org.bukkit.Color.fromRGB(255, 0, 0);
            case "blue": return org.bukkit.Color.fromRGB(0, 0, 255);
            case "lime": return org.bukkit.Color.fromRGB(50, 255, 50);
            case "yellow": return org.bukkit.Color.fromRGB(255, 255, 0);
            case "purple": return org.bukkit.Color.fromRGB(128, 0, 128);
            default: return org.bukkit.Color.WHITE;
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

    private void drawEmeraldDome(Player center, double radius) {
        Location loc = center.getLocation();
        World world = loc.getWorld();
        
        // Spawn roughly 400 particles randomly on the hemisphere
        for (int i = 0; i < 400; i++) {
            double phi = Math.random() * 2 * Math.PI;
            double costheta = Math.random(); // 0 to 1 for upper hemisphere
            double theta = Math.acos(costheta);
            
            double x = radius * Math.sin(theta) * Math.cos(phi);
            double z = radius * Math.sin(theta) * Math.sin(phi);
            double y = radius * Math.cos(theta);
            
            world.spawnParticle(Particle.HAPPY_VILLAGER, loc.clone().add(x, y, z), 1, 0, 0, 0, 0);
        }
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

    private void sendFakeBlocks(Location center, Material material, int durationTicks) {
        Map<Location, org.bukkit.block.data.BlockData> originalBlocks = new HashMap<>();
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Location loc = center.clone().add(dx, -1, dz);
                originalBlocks.put(loc, loc.getBlock().getBlockData());
                
                org.bukkit.block.data.BlockData fakeData = Bukkit.createBlockData(material);
                for (Entity entity : center.getWorld().getNearbyEntities(center, 30, 30, 30)) {
                    if (entity instanceof Player p) {
                        p.sendBlockChange(loc, fakeData);
                    }
                }
            }
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, org.bukkit.block.data.BlockData> entry : originalBlocks.entrySet()) {
                    Location loc = entry.getKey();
                    if (loc.getWorld() == null) continue;
                    for (Entity entity : loc.getWorld().getNearbyEntities(loc, 30, 30, 30)) {
                        if (entity instanceof Player p) {
                            p.sendBlockChange(loc, entry.getValue());
                        }
                    }
                }
            }
        }.runTaskLater(Tezzlar.getInstance(), durationTicks);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isMissionActive()) return;
        if (exitRegionMin == null || exitRegionMax == null || exitDestination == null) return;
        
        Location to = event.getTo();
        if (to == null) return;
        
        if (to.getWorld() != null && to.getWorld().equals(exitRegionMin.getWorld())) {
            if (to.getX() >= exitRegionMin.getX() && to.getX() <= exitRegionMax.getX() + 1 &&
                to.getY() >= exitRegionMin.getY() && to.getY() <= exitRegionMax.getY() + 1 &&
                to.getZ() >= exitRegionMin.getZ() && to.getZ() <= exitRegionMax.getZ() + 1) {
                
                event.getPlayer().teleport(exitDestination);
                event.getPlayer().playSound(exitDestination, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                Messenger.prefixedSend(event.getPlayer(), "&a¡Has escapado de la dungeon exitosamente!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isMissionActive()) return;
        
        Location blockLoc = event.getBlock().getLocation();
        if (analyzingBlocks.contains(blockLoc)) {
            event.setCancelled(true);
            return;
        }
        
        loadMissionData();
        for (Location loc : monumentLocations.values()) {
            if (loc.getWorld() != null && loc.getWorld().equals(blockLoc.getWorld()) &&
                loc.getBlockX() == blockLoc.getBlockX() &&
                loc.getBlockY() == blockLoc.getBlockY() &&
                loc.getBlockZ() == blockLoc.getBlockZ()) {
                
                if (loc.getBlock().getType().name().endsWith("_WOOL")) {
                    event.setCancelled(true);
                    Messenger.prefixedSend(event.getPlayer(), "&cEsta lana es sagrada y ya no puede ser extraída.");
                }
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isMissionActive()) return;
        
        Location blockLoc = event.getBlock().getLocation();
        if (analyzingBlocks.contains(blockLoc)) {
            event.setCancelled(true);
            return;
        }
        
        loadMissionData();
        
        String targetColor = null;
        for (Map.Entry<String, Location> entry : monumentLocations.entrySet()) {
            Location expectedLoc = entry.getValue();
            if (expectedLoc != null && 
                blockLoc.getWorld().equals(expectedLoc.getWorld()) &&
                blockLoc.getBlockX() == expectedLoc.getBlockX() &&
                blockLoc.getBlockY() == expectedLoc.getBlockY() &&
                blockLoc.getBlockZ() == expectedLoc.getBlockZ()) {
                
                targetColor = entry.getKey();
                break;
            }
        }
        
        if (targetColor == null) return; // Not placed on any monument location, allow it

        ItemStack placedItem = event.getItemInHand();
        final String placedColor = getWoolColor(placedItem);
        final String fTargetColor = targetColor;
        
        final Player player = event.getPlayer();
        final Mission mission = MissionsModule.getMissionManager().getMission(MISSION_ID);
        if (mission == null) return;

        final String chosenColor = player.getPersistentDataContainer().get(CHOSEN_COLOR_KEY, PersistentDataType.STRING);
        final ItemStack finalPlacedItem = placedItem.clone();
        finalPlacedItem.setAmount(1);

        analyzingBlocks.add(blockLoc);
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks++;
                if (ticks <= 4) {
                    blockLoc.getWorld().playSound(blockLoc, Sound.BLOCK_BEACON_AMBIENT, 1.0f, 2.0f);
                    blockLoc.getWorld().spawnParticle(Particle.ENCHANT, blockLoc.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1);
                    return;
                }
                
                this.cancel();
                analyzingBlocks.remove(blockLoc);
                
                if (placedColor == null || !placedColor.equals(fTargetColor) || (chosenColor != null && !chosenColor.equals(placedColor))) {
                    blockLoc.getWorld().playSound(blockLoc, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    blockLoc.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, blockLoc.clone().add(0.5, 0.5, 0.5), 15, 0.3, 0.3, 0.3);
                    
                    Material type = blockLoc.getBlock().getType();
                    blockLoc.getBlock().setType(Material.AIR);
                    if (placedColor != null) {
                        blockLoc.getWorld().dropItemNaturally(blockLoc, finalPlacedItem);
                    } else {
                        blockLoc.getWorld().dropItemNaturally(blockLoc, new ItemStack(type));
                    }
                    
                    sendFakeBlocks(blockLoc, Material.RED_STAINED_GLASS, 40);
                    
                    if (placedColor == null || !placedColor.equals(fTargetColor)) {
                        if (placedColor != null) {
                            Messenger.prefixedSend(player, "&cEste no es el altar correcto para la lana " + translateColor(placedColor) + ".");
                        } else {
                            Messenger.prefixedSend(player, "&cEste altar solo acepta la lana " + translateColor(fTargetColor) + " sagrada.");
                        }
                    } else {
                        Messenger.prefixedSend(player, "&cNo eres digno de colocar esta lana, solo el portador original (" + translateColor(placedColor) + ") puede hacerlo.");
                    }
                } else {
                    blockLoc.getWorld().playSound(blockLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    
                    Firework fw = (Firework) blockLoc.getWorld().spawnEntity(blockLoc.clone().add(0.5, 1, 0.5), EntityType.FIREWORK_ROCKET);
                    FireworkMeta meta = fw.getFireworkMeta();
                    meta.addEffect(FireworkEffect.builder().withColor(Color.WHITE).with(FireworkEffect.Type.BALL_LARGE).build());
                    meta.setPower(1);
                    fw.setFireworkMeta(meta);
                    
                    sendFakeBlocks(blockLoc, Material.LIME_STAINED_GLASS, 40);
                    
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        Messenger.showTitle(
                            p,
                            "&a&l¡LANA COLOCADA!", 
                            "&7" + player.getName() + " ha colocado la lana " + translateColor(placedColor), 
                            Duration.ofMillis(500),
                            Duration.ofMillis(3500), 
                            Duration.ofMillis(1000)
                        );
                    }
                    
                    MissionsModule.getGlobalMissionManager().addProgress(MISSION_ID, 1);
                    int currentProgress = MissionsModule.getGlobalMissionManager().getProgress(MISSION_ID);
                    if (currentProgress >= mission.getObjectiveAmount()) {
                        Messenger.prefixedBroadcast("&6&l¡EL MONUMENTO HA SIDO COMPLETADO!");
                        MissionsModule.getDataManager().giveRewardToEveryone(MISSION_ID);
                    }
                }
            }
        }.runTaskTimer(Tezzlar.getInstance(), 10L, 10L);
    }
}

package com.panita.tezzlar3.inventory.tasks;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.inventory.util.InventoryConfigDefaults;
import com.panita.tezzlar3.inventory.util.GravesDataManager;
import com.panita.tezzlar3.inventory.util.InventorySerializer;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarManager;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarProvider;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import com.destroystokyo.paper.profile.PlayerProfile;

import java.util.*;

public class RevenantTrackerTask implements Runnable, ActionBarProvider {

    private final NamespacedKey pdcKey;
    private final Random random = new Random();
    private final Map<String, Integer> presenceCounter = new HashMap<>();
    private final Map<UUID, Integer> playerSecondsLeft = new HashMap<>();

    public RevenantTrackerTask() {
        this.pdcKey = new NamespacedKey(Tezzlar.getInstance(), "revenant_inventory");
        if (ActionBarManager.getInstance() != null) {
            ActionBarManager.getInstance().registerProvider(this);
        }
    }

    @Override
    public String getId() {
        return "grave_revive";
    }

    @Override
    public java.util.List<String> getTexts(Player player) {
        Integer seconds = playerSecondsLeft.get(player.getUniqueId());
        if (seconds != null) {
            return java.util.Collections.singletonList("<red><b>¡El Vestigio revivirá en " + seconds + " segundos!</b></red>");
        }
        return null;
    }

    @Override
    public boolean isUrgent(Player player) {
        return playerSecondsLeft.containsKey(player.getUniqueId());
    }

    @Override
    public void run() {
        if (!Tezzlar.getConfigManager().getBoolean("inventory.enabled", InventoryConfigDefaults.INVENTORY_ENABLED)) return;
        if (!Tezzlar.getConfigManager().getBoolean("inventory.revenantZombie", InventoryConfigDefaults.INVENTORY_REVENANTZOMBIE)) return;

        Map<String, ConfigurationSection> graves = GravesDataManager.getActiveGraves();
        if (graves.isEmpty()) {
            playerSecondsLeft.clear();
            return;
        }

        Set<UUID> activeTrackedPlayers = new HashSet<>();
        for (Map.Entry<String, ConfigurationSection> entry : graves.entrySet()) {
            String id = entry.getKey();
            ConfigurationSection section = entry.getValue();

            World world = Bukkit.getWorld(section.getString("world"));
            if (world == null) continue;

            Location graveLoc = new Location(world, section.getInt("x"), section.getInt("y"), section.getInt("z"));

            UUID playerUUID = UUID.fromString(section.getString("playerUUID"));

            // Find the grave owner nearby
            Player owner = null;
            for (Player p : world.getPlayers()) {
                if (p.getGameMode() == GameMode.SURVIVAL && p.getUniqueId().equals(playerUUID) && p.getLocation().distance(graveLoc) <= 10.0) {
                    owner = p;
                    break;
                }
            }

            if (owner != null) {
                boolean isSoulbound = section.getBoolean("isSoulbound", false);
                if (isSoulbound) {
                    Location pLoc = graveLoc.clone().add(0.5, 1.5, 0.5);
                    world.spawnParticle(Particle.SOUL, pLoc, 50, 0.5, 0.5, 0.5, 0.1);
                    world.spawnParticle(Particle.ENCHANT, pLoc, 100, 1.0, 1.0, 1.0, 0.1);
                    SoundUtils.play(owner, "block.amethyst_block.chime", 1.0f, 0.8f);
                    Messenger.prefixedSend(owner, "<aqua>¡El eco de tu alma descansa en paz!</aqua>");
                    
                    GravesDataManager.removeGrave(id);
                    continue;
                }

                int count = presenceCounter.getOrDefault(id, 0);
                
                if (count == 0) {
                    spawnMinions(graveLoc);
                }
                
                count++;
                presenceCounter.put(id, count);
                activeTrackedPlayers.add(owner.getUniqueId());
                
                if (count < 4) {
                    int secondsLeft = 4 - count;
                    playerSecondsLeft.put(owner.getUniqueId(), secondsLeft);
                    
                    // Spawn flame circle at the edge of the 10 block radius
                    double radius = 10.0;
                    for (int i = 0; i < 360; i += 5) {
                        double angle = Math.toRadians(i);
                        double px = graveLoc.getX() + radius * Math.cos(angle);
                        double pz = graveLoc.getZ() + radius * Math.sin(angle);
                        
                        Location pLoc1 = new Location(world, px, graveLoc.getY() + 0.5, pz);
                        Location pLoc2 = new Location(world, px, graveLoc.getY() + 1.5, pz);
                        Location pLoc3 = new Location(world, px, graveLoc.getY() + 2.5, pz);
                        
                        world.spawnParticle(Particle.FLAME, pLoc1, 4, 0.2, 0.2, 0.2, 0.03);
                        world.spawnParticle(Particle.FLAME, pLoc2, 4, 0.2, 0.2, 0.2, 0.03);
                        world.spawnParticle(Particle.FLAME, pLoc3, 4, 0.2, 0.2, 0.2, 0.03);
                    }
                    SoundUtils.play(owner, "block.fire.ambient", 1.0f, 1.0f);
                }
                
                if (count >= 4) { // 4 seconds delay
                    spawnRevenantOnly(graveLoc, id, section, owner);
                    GravesDataManager.removeGrave(id);
                    presenceCounter.remove(id);
                    playerSecondsLeft.remove(owner.getUniqueId());
                }
            } else {
                // Reset counter if player leaves the area
                presenceCounter.put(id, 0);
            }
        }
        
        // Remove players from playerSecondsLeft that are no longer tracked
        playerSecondsLeft.keySet().removeIf(uuid -> !activeTrackedPlayers.contains(uuid));
    }

    private void spawnRevenantOnly(Location loc, String id, ConfigurationSection section, Player owner) {
        String base64 = section.getString("itemsBase64");
        String playerName = section.getString("playerName");

        // Spawn Zombie
        Zombie zombie = (Zombie) loc.getWorld().spawnEntity(loc.clone().add(0, 1, 0), EntityType.ZOMBIE);
        // Paper supports Adventure components natively
        zombie.customName(Messenger.mini("<b><gradient:#E17F00:#E17F00>Vest</gradient><gradient:#E17F00:#04B784>igio Errante de " + playerName + "</gradient></b>"));
        zombie.setCustomNameVisible(true);
        zombie.setPersistent(true);
        zombie.setRemoveWhenFarAway(false);
        EntityUtils.setColoredGlowing(zombie, NamedTextColor.RED);

        // Health
        double customHealth = Tezzlar.getConfigManager().getDouble("inventory.revenant.health", InventoryConfigDefaults.REVENANT_HEALTH);
        EntityUtils.trySetAttribute(zombie, Attribute.MAX_HEALTH, customHealth);
        try {
            zombie.setHealth(customHealth);
        } catch(Exception ignored) {}

        // Helmet
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        // Use the live profile from the online player, guaranteeing the texture is loaded!
        PlayerProfile profile = owner.getPlayerProfile();
        skull.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(profile));

        zombie.getEquipment().setHelmet(skull);
        zombie.getEquipment().setHelmetDropChance(0.0f); // DO NOT DROP THE HELMET

        // Equip armor and sword from drops
        ItemStack[] items = InventorySerializer.fromBase64(base64);
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;
            String typeName = item.getType().name();

            if (typeName.endsWith("_CHESTPLATE") && zombie.getEquipment().getChestplate().isEmpty()) {
                zombie.getEquipment().setChestplate(item);
                zombie.getEquipment().setChestplateDropChance(0.0f);
            } else if (typeName.endsWith("_LEGGINGS") && zombie.getEquipment().getLeggings().isEmpty()) {
                zombie.getEquipment().setLeggings(item);
                zombie.getEquipment().setLeggingsDropChance(0.0f);
            } else if (typeName.endsWith("_BOOTS") && zombie.getEquipment().getBoots().isEmpty()) {
                zombie.getEquipment().setBoots(item);
                zombie.getEquipment().setBootsDropChance(0.0f);
            } else if (typeName.endsWith("_SWORD") && zombie.getEquipment().getItemInMainHand().isEmpty()) {
                zombie.getEquipment().setItemInMainHand(item);
                zombie.getEquipment().setItemInMainHandDropChance(0.0f);
            }
        }

        // Store inventory in PDC
        zombie.getPersistentDataContainer().set(pdcKey, PersistentDataType.STRING, base64);
        
        SoundUtils.playGlobal("entity.zombie.break_wooden_door", 1.0f, 0.5f);
    }
    
    private void spawnMinions(Location loc) {
        List<String> minions = Tezzlar.getConfigManager().getStringList("inventory.revenant.minions");
        if (minions == null || minions.isEmpty()) minions = InventoryConfigDefaults.REVENANT_MINIONS;
        
        int count = Tezzlar.getConfigManager().getInt("inventory.revenant.minionCount", InventoryConfigDefaults.REVENANT_MINION_COUNT);

        for (int i = 0; i < count; i++) {
            if (minions.isEmpty()) break;
            String typeStr = minions.get(random.nextInt(minions.size()));
            try {
                EntityType type = EntityType.valueOf(typeStr.toUpperCase());
                Entity minion = loc.getWorld().spawnEntity(loc.clone().add(random.nextInt(3) - 1, 1, random.nextInt(3) - 1), type);
                if (minion instanceof LivingEntity) {
                    ((LivingEntity) minion).setRemoveWhenFarAway(false);
                }
            } catch (Exception ignored) {
            }
        }
        
        // Play an initial eerie sound to warn the player
        SoundUtils.playGlobal("entity.skeleton_horse.death", 1.0f, 2.0f);
    }
}

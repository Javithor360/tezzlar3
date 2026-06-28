package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.core.util.ItemUtils;
import com.panita.tezzlar3.minievents.MiniEvent;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SpecialDropMobEvent implements MiniEvent, Listener {

    private final Random random = new Random();
    private FeverDropProfile activeProfile;

    public SpecialDropMobEvent() {
        activeProfile = FeverDropProfile.POOL.get(random.nextInt(FeverDropProfile.POOL.size()));
    }

    @Override
    public void start(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop(JavaPlugin plugin) {
        HandlerList.unregisterAll(this);
        // Reshuffle for next time it might be started
        activeProfile = FeverDropProfile.POOL.get(random.nextInt(FeverDropProfile.POOL.size()));
    }

    @Override
    public String getId() {
        return "special_drop_mob";
    }

    @Override
    public String getDisplayName() {
        return "<#B8FA82><b>Fiebre de " + activeProfile.prizeDisplayName() + "</b></#B8FA82>";
    }

    @Override
    public String getGenericName() {
        return "<#B8FA82><b>Fiebre de ítems</b></#B8FA82>";
    }

    @Override
    public String getDescription() {
        String base = "<gray>¡Mata " + activeProfile.mobDisplayName() + "s para conseguir " + activeProfile.prizeDisplayName() + "!</gray>\n";
        String prob = activeProfile.descriptionFragment();
        return base + prob + "\n<dark_gray><i>* El encantamiento de Botín (Looting) mejora los resultados.</i></dark_gray>";
    }

    @Override
    public long getDurationTicks() {
        return 30 * 60 * 20L; // 30 minutes
    }

    @Override
    public Map<String, Object> serializeExtraData() {
        Map<String, Object> map = new HashMap<>();
        map.put("active_profile_id", activeProfile.id());
        return map;
    }

    @Override
    public void loadExtraData(Map<String, Object> data) {
        if (data.containsKey("active_profile_id")) {
            String loadedId = (String) data.get("active_profile_id");
            for (FeverDropProfile profile : FeverDropProfile.POOL) {
                if (profile.id().equals(loadedId)) {
                    activeProfile = profile;
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntityType() == activeProfile.mobType() && event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();

            int absoluteLootingLevel = 0;
            ItemStack hand = killer.getInventory().getItemInMainHand();
            if (hand != null && hand.containsEnchantment(Enchantment.LOOTING)) {
                absoluteLootingLevel = hand.getEnchantmentLevel(Enchantment.LOOTING);
            }

            // Calculate final chance
            double chance = activeProfile.baseChance() + (absoluteLootingLevel * activeProfile.lootingChanceBonus());

            if (random.nextDouble() < chance) {
                if (activeProfile.dropItem() == null) {
                    // It's an XP reward
                    double finalMultiplier = activeProfile.expMultiplier() + absoluteLootingLevel; // Looting adds +1x to multiplier
                    event.setDroppedExp((int) (event.getDroppedExp() * finalMultiplier));
                } else {
                    // It's an Item reward
                    int amount = activeProfile.minAmount();
                    if (activeProfile.maxAmount() > activeProfile.minAmount()) {
                        amount += random.nextInt((activeProfile.maxAmount() - activeProfile.minAmount()) + 1);
                    }
                    
                    // Looting adds max items (only if maxAmount > 1, so Totems aren't duplicated)
                    if (activeProfile.maxAmount() > 1 && absoluteLootingLevel > 0) {
                        amount += random.nextInt(absoluteLootingLevel + 1);
                    }
                    
                    event.getDrops().add(new ItemStack(activeProfile.dropItem(), amount));
                }
            }
        }
    }
}

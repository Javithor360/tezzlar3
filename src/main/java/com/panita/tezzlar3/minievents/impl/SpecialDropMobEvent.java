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
        return "<#5E62F2>Fiebre de " + activeProfile.prizeDisplayName() + "</#5E62F2>";
    }

    @Override
    public String getGenericName() {
        return "<#5E62F2>Fiebre de ítems</#5E62F2>";
    }

    @Override
    public String getDescription() {
        String base = "\n&7Se ha reportado la aparición de un drop único durante los próximos &b30 minutos&7.\n\n";
        String prob = activeProfile.descriptionFragment();
        return base + prob + "\n\n&7<i>* Tener en cuenta que el encantamiento de &bLooting &7incrementa aún más las probabilidades de obtener drops.</i>";
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
                    
                    while (amount > 0) {
                        int stackSize = Math.min(64, amount);
                        event.getDrops().add(new ItemStack(activeProfile.dropItem(), stackSize));
                        amount -= stackSize;
                    }
                }
            }
        }
    }
}

package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.core.util.BlockUtils;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarManager;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarProvider;
import com.panita.tezzlar3.minievents.MiniEvent;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WrongToolDamageEvent implements MiniEvent, Listener, ActionBarProvider {

    private boolean active = false;
    private final Map<UUID, Long> messageExpiry = new HashMap<>();

    @Override
    public String getId() {
        return "wrong_tool_damage";
    }

    @Override
    public String getDisplayName() {
        return "<#16A0F0>Maldición del Obrero</#16A0F0>";
    }

    @Override
    public String getDescription() {
        return "\n&7Se nos informa que durante las próximas &b4 horas&7, hay una condición especial activada.\n\n&3- &7Romper bloques con la &bherramienta equivocada &7te provocará daño severo.\n\n&7Por lo tanto, se recomienda fijarse antes de romper cualquier bloque.";
    }

    @Override
    public long getDurationTicks() {
        return 288000L; // 4 hours
    }

    @Override
    public boolean canExecute() {
        return TimeManager.getCurrentDay() < 22;
    }

    @Override
    public void start(JavaPlugin plugin) {
        active = true;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        if (ActionBarManager.getInstance() != null) {
            ActionBarManager.getInstance().registerProvider(this);
        }
    }

    @Override
    public void stop(JavaPlugin plugin) {
        active = false;
        HandlerList.unregisterAll(this);
        messageExpiry.clear();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!active) return;
        
        Player player = event.getPlayer();
        if (!PlayerUtils.isSurvival(player)) return;

        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (BlockUtils.isInstaBreak(block.getType())) return; // Ignore flowers, tall grass, torches...
        
        if (!BlockUtils.isOptimalTool(block.getType(), tool.getType())) {
            // Wrong tool! Apply Instant Damage IV (amplifier 3)
            player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 3));
            messageExpiry.put(player.getUniqueId(), System.currentTimeMillis() + 3000L);
        }
    }

    @Override
    public boolean isUrgent(Player player) {
        if (!active || !PlayerUtils.isSurvival(player)) return false;
        return messageExpiry.containsKey(player.getUniqueId()) && System.currentTimeMillis() < messageExpiry.get(player.getUniqueId());
    }

    @Override
    public String getText(Player player) {
        if (isUrgent(player)) {
            return "<red>¡Usa la herramienta correcta!</red>";
        }
        return null;
    }
}

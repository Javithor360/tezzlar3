package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.event.entity.PlayerDeathEvent;
import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.core.util.BlockUtils;

public class WrongToolDamageMechanic extends DifficultyMechanic {

    public WrongToolDamageMechanic(JavaPlugin plugin) {
        super(plugin, 22);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isActive()) return;

        Player player = event.getPlayer();
        if (!PlayerUtils.isSurvival(player)) return;

        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (BlockUtils.isInstaBreak(block.getType())) return;

        if (!BlockUtils.isOptimalTool(block.getType(), tool.getType())) {
            // Apply Instant Damage 4 (amplifier 3)
            player.setMetadata("wrong_tool_damage", new FixedMetadataValue(plugin, System.currentTimeMillis()));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 3));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!isActive()) return;
        Player player = event.getEntity();
        
        if (player.hasMetadata("wrong_tool_damage")) {
            long time = player.getMetadata("wrong_tool_damage").get(0).asLong();
            // If they died within 100ms (basically the same tick or next tick) of breaking the block
            if (System.currentTimeMillis() - time <= 100) {
                event.setDeathMessage(player.getName() + " murió por romper un bloque con la herramienta equivocada.");
            }
            player.removeMetadata("wrong_tool_damage", plugin);
        }
    }
}

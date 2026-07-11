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
            // Apply Instant Damage 6 (amplifier 5)
            player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 5));
        }
    }
}

package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.core.util.PlayerUtils;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarManager;
import com.panita.tezzlar3.core.chat.actionbar.ActionBarProvider;
import com.panita.tezzlar3.minievents.MiniEvent;
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
        
        if (isInstaBreak(block.getType())) return; // Ignore flowers, tall grass, torches...
        
        if (!isOptimalTool(block.getType(), tool.getType())) {
            // Wrong tool! Apply Instant Damage IV (amplifier 3)
            player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 3));
            messageExpiry.put(player.getUniqueId(), System.currentTimeMillis() + 3000L);
        }
    }

    private boolean isInstaBreak(Material material) {
        String name = material.name();
        return name.contains("FLOWER") || name.contains("TALL_GRASS") || name.contains("FERN") ||
               name.contains("TORCH") || name.contains("MUSHROOM") || name.contains("SAPLING") ||
               name.contains("SUGAR_CANE") || name.contains("KELP") || name.contains("SEAGRASS") ||
               name.contains("DEAD_BUSH") || name.contains("VINE") || name.contains("LILY_PAD") ||
               name.contains("CORAL") || name.contains("SCAFFOLDING") || name.contains("SLIME_BLOCK") ||
               name.contains("HONEY_BLOCK") || name.contains("GLASS");
    }

    private boolean isOptimalTool(Material block, Material tool) {
        String bName = block.name();
        String tName = tool.name();
        
        // Pickaxe
        if (bName.endsWith("_ORE") || bName.endsWith("_STONE") || bName.contains("STONE") ||
            bName.contains("GRANITE") || bName.contains("DIORITE") || bName.contains("ANDESITE") ||
            bName.contains("DEEPSLATE") || bName.contains("BASALT") || bName.contains("BRICK") ||
            bName.contains("TERRACOTTA") || bName.contains("CONCRETE") || bName.contains("IRON") ||
            bName.contains("GOLD") || bName.contains("DIAMOND") || bName.contains("EMERALD") ||
            bName.contains("NETHERITE") || bName.equals("NETHERRACK") || bName.contains("_NETHER") ||
            bName.contains("LAPIS") || bName.contains("COAL") || bName.contains("RESIN") ||
            bName.contains("OBSIDIAN") || bName.contains("QUARTZ") || bName.contains("PRISMARINE") ||
            bName.contains("COPPER") || bName.contains("AMETHYST") || bName.contains("TUFF") ||
            bName.contains("PACKED_") || bName.contains("CINNABAR") || bName.contains("SULFUR") ||
            bName.contains("BLACKSTONE") || bName.contains("PURPUR") || bName.contains("END") ||
            bName.contains("SHULKER") || bName.contains("ICE") || bName.contains("CALCITE") ||
            bName.contains("DRIPSTONE") || bName.contains("NYLIUM") || bName.contains("BONE_BLOCK") ||
            bName.contains("CORAL") || bName.contains("ANCIENT") || bName.contains("FURNACE") ||
            bName.contains("SPAWNER") || bName.contains("ANVIL")) {
            return tName.endsWith("_PICKAXE");
        }
        
        // Axe
        if (bName.endsWith("_LOG") || bName.endsWith("_WOOD") || bName.endsWith("_PLANKS") ||
            bName.endsWith("_STEM") || bName.endsWith("MUSHROOM") || bName.endsWith("BED") ||
            bName.endsWith("HONEY") || bName.endsWith("TABLE") || bName.endsWith("COMPOSTER") ||
            bName.endsWith("LADDER") || bName.endsWith("SCAFFOLDING") || bName.endsWith("LOOM") ||
            bName.endsWith("FRAME") || bName.endsWith("PAINTING") || bName.endsWith("JACK") ||
            bName.contains("FENCE") || bName.contains("DOOR") || bName.contains("TRAPDOOR") ||
            bName.contains("SIGN") || bName.contains("BOOKSHELF") || bName.contains("CHEST") ||
            bName.contains("BARREL") || bName.contains("CAMPFIRE") || bName.contains("PUMPKIN") ||
            bName.contains("MELON") || bName.contains("BAMBOO") || bName.contains("CRAFTING_TABLE") ||
            bName.contains("JUKEBOX") || bName.contains("NOTE_BLOCK") || bName.contains("BANNER")) {
            return tName.endsWith("_AXE");
        }
        
        // Shovel
        if (bName.contains("DIRT") || bName.contains("GRASS_BLOCK") || bName.contains("PODZOL") ||
            bName.contains("MYCELIUM") || bName.contains("SAND") || bName.contains("GRAVEL") ||
            bName.contains("CLAY") || bName.contains("SNOW") || bName.equals("MUD") ||
            bName.contains("SOUL_SAND") || bName.contains("SOUL_SOIL") ||  bName.endsWith("FARMLAND"))  {
            return tName.endsWith("_SHOVEL");
        }
        
        // Hoe / Shears
        if (bName.contains("LEAVES") || bName.contains("WART_BLOCK") || bName.contains("SPONGE") ||
            bName.contains("HAY_BLOCK") || bName.contains("TARGET") || bName.contains("SHROOMLIGHT") ||
            bName.contains("SCULK") || bName.contains("WOOL") || bName.contains("MOSS")) {
            return tName.endsWith("_HOE") || tName.endsWith("_SHEARS");
        }
        
        // For anything else (like crops, cobweb, bed, etc), assume it's fine.
        return true;
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

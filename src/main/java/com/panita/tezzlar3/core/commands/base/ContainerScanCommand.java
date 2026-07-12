package com.panita.tezzlar3.core.commands.base;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.BlockUtils;
import com.panita.tezzlar3.core.util.CommandUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SubCommandSpec(
        parent = "tezzlar",
        name = "containerscan",
        description = "Escanea contenedores y spawners en un radio",
        syntax = "/tezzlar containerscan <radius> [page]",
        permission = "tezzlar.command.containerscan"
)
public class ContainerScanCommand implements AdvancedCommand, TabSuggestingCommand {

    private static final int ITEMS_PER_PAGE = 10;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Messenger.prefixedSend(sender, "&cEste comando solo puede ser ejecutado por un jugador.");
            return;
        }

        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, getClass())) {
            return;
        }

        int radius;
        try {
            radius = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            Messenger.prefixedSend(player, "&cEl radio debe ser un número válido.");
            return;
        }

        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Messenger.prefixedSend(player, "&cLa página debe ser un número válido.");
                return;
            }
        }

        if (radius > 150) {
            Messenger.prefixedSend(player, "&cEl radio máximo permitido es 150.");
            return;
        }

        Location center = player.getLocation();
        List<Block> foundBlocks = scanBlocks(center, radius);

        if (foundBlocks.isEmpty()) {
            Messenger.prefixedSend(player, "&eNo se encontraron contenedores ni spawners en un radio de " + radius + " bloques.");
            return;
        }

        // Sort blocks by proximity (closest to furthest)
        foundBlocks.sort(Comparator.comparingDouble(b -> b.getLocation().distanceSquared(center)));

        int totalItems = foundBlocks.size();
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);

        Messenger.prefixedSend(player, "&6Resultados del escaneo (Página " + page + "/" + totalPages + "):");

        // Highlight all blocks found in the radius
        for (Block block : foundBlocks) {
            highlightBlock(block);
        }

        for (int i = startIndex; i < endIndex; i++) {
            Block block = foundBlocks.get(i);
            String materialName = block.getType().name();
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            String tpButton = "<click:run_command:'/tp " + x + " " + y + " " + z + "'><hover:show_text:'Teletransportarse a " + x + ", " + y + ", " + z + "'><dark_gray>[<green><bold>TP</bold></green>]</dark_gray></hover></click>";
            String line = "<gray>" + (i + 1) + ". <aqua>" + materialName + " <gray>en <yellow>" + x + ", " + y + ", " + z + " " + tpButton;
            Messenger.send(player, line);
        }

        // Pagination footer
        if (totalPages > 1) {
            String prevButton = "<dark_gray>[<gray><</gray>]</dark_gray>";
            if (page > 1) {
                prevButton = "<click:run_command:'/tezzlar containerscan " + radius + " " + (page - 1) + "'><hover:show_text:'<yellow>Página anterior'><dark_gray>[<yellow><</yellow>]</dark_gray></hover></click>";
            }

            String nextButton = "<dark_gray>[<gray>></gray>]</dark_gray>";
            if (page < totalPages) {
                nextButton = "<click:run_command:'/tezzlar containerscan " + radius + " " + (page + 1) + "'><hover:show_text:'<yellow>Siguiente página'><dark_gray>[<yellow>></yellow>]</dark_gray></hover></click>";
            }

            String footer = "<dark_gray><strikethrough>---------------</strikethrough></dark_gray>  " + prevButton + "   " + nextButton + "  <dark_gray><strikethrough>---------------</strikethrough></dark_gray>";
            Messenger.send(player, footer);
        }
    }

    private List<Block> scanBlocks(Location center, int radius) {
        List<Block> blocks = new ArrayList<>();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        World world = center.getWorld();

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = cy - radius; y <= cy + radius; y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material type = block.getType();
                    if (type == Material.CHEST || type == Material.BARREL || type == Material.TRAPPED_CHEST || type == Material.SPAWNER) {
                        BlockState state = block.getState();
                        if (state instanceof Container container) {
                            // Ignore if it is NOT empty
                            if (!container.getInventory().isEmpty()) {
                                continue;
                            }
                            // Ignore if it has JustLootIt metadata
                            NamespacedKey jliKey = new NamespacedKey("justlootit", "id");
                            if (container.getPersistentDataContainer().getKeys().contains(jliKey)) {
                                continue;
                            }
                        } else if (state instanceof CreatureSpawner spawner) {
                            // Ignore if it has a mob assigned
                            if (spawner.getSpawnedType() != null) {
                                continue;
                            }
                        }
                        blocks.add(block);
                    }
                }
            }
        }
        return blocks;
    }

    private void highlightBlock(Block block) {
        BlockDisplay display = (BlockDisplay) block.getWorld().spawnEntity(block.getLocation(), EntityType.BLOCK_DISPLAY);
        display.setBlock(block.getBlockData());
        display.setGlowing(true);
        display.setGlowColorOverride(BlockUtils.getContainerColor(block.getType()));
        display.setPersistent(false);

        // Remove the block display after 60 seconds (1200 ticks)
        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), display::remove, 1200L);
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> List.of("10", "20", "30", "50", "100", "150"));
    }
}

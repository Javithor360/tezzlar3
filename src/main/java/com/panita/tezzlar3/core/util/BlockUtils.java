package com.panita.tezzlar3.core.util;

import org.bukkit.Color;
import org.bukkit.Material;

public class BlockUtils {

    public static Color getContainerColor(Material material) {
        return switch (material) {
            case CHEST -> Color.YELLOW;
            case BARREL -> Color.fromRGB(139, 69, 19); // Brown
            case SPAWNER -> Color.GRAY;
            case TRAPPED_CHEST -> Color.RED;
            default -> Color.WHITE;
        };
    }

    public static boolean isInstaBreak(Material material) {
        String name = material.name();
        return name.contains("FLOWER") || name.contains("TALL_GRASS") || name.contains("FERN") ||
               name.contains("TORCH") || name.contains("MUSHROOM") || name.contains("SAPLING") ||
               name.contains("SUGAR_CANE") || name.contains("KELP") || name.contains("SEAGRASS") ||
               name.contains("DEAD_BUSH") || name.contains("VINE") || name.contains("LILY_PAD") ||
               name.contains("CORAL") || name.contains("SCAFFOLDING") || name.contains("SLIME_BLOCK") ||
               name.contains("HONEY_BLOCK") || name.contains("GLASS");
    }

    public static boolean isOptimalTool(Material block, Material tool) {
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
            bName.contains("SOUL_SAND") || bName.contains("SOUL_SOIL") || bName.endsWith("FARMLAND")) {
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
}

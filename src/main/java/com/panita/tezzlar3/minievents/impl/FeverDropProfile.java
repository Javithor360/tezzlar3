package com.panita.tezzlar3.minievents.impl;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.List;

public record FeverDropProfile(
        String id,
        EntityType mobType,
        String mobDisplayName,
        String prizeDisplayName,
        Material dropItem, // If null, the reward is an XP multiplier
        double expMultiplier,
        double baseChance,
        double lootingChanceBonus,
        int minAmount,
        int maxAmount,
        String descriptionFragment
) {
    /**
     * Pre-configured pool of fever drop profiles.
     */
    public static final List<FeverDropProfile> POOL = List.of(
            new FeverDropProfile(
                    "zombie_gapple",
                    EntityType.ZOMBIE,
                    "Zombie",
                    "<#F2F05E>Manzanas Doradas</#F2F05E>",
                    Material.GOLDEN_APPLE,
                    0,
                    0.08,
                    0.02,
                    1,
                    3,
                    "&3- &7Los &bZombies &7tienen una probabilidad del &b8% &7de soltar <#F2F05E>Manzanas Doradas</#F2F05E> &7al morir."
            ),

            new FeverDropProfile(
                    "wither_skull",
                    EntityType.WITHER_SKELETON,
                    "Wither Skeleton",
                    "<#949492>Calaveras de Wither</#949492>",
                    Material.WITHER_SKELETON_SKULL,
                    0,
                    0.1,
                    0.05,
                    1,
                    1,
                    "&3- &7Los &bWither Skeletons &7tienen una probabilidad del &b10% &7de soltar <#949492>Calaveras de Wither</#949492> &7al morir."
            ),

            new FeverDropProfile(
                    "glowsquid_gapple",
                    EntityType.GLOW_SQUID,
                    "Glowsquid",
                    "<#F2F05E>Manzanas Doradas</#F2F05E>",
                    Material.GOLDEN_APPLE,
                    0,
                    0.15,
                    0.04,
                    2,
                    5,
                    "&3- &7Los &bGlowsquids &7tienen una probabilidad del &b15% &7de soltar <#F2F05E>Manzanas Doradas</#F2F05E> &7al morir."
            ),

            new FeverDropProfile(
                    "creeper_totem",
                    EntityType.CREEPER,
                    "Creeper",
                    "<#FAC650>Tótems</#FAC650>",
                    Material.TOTEM_OF_UNDYING,
                    0,
                    0.03,
                    0.05,
                    1,
                    1,
                    "&3- &7Los &bCrepers &7tienen una probabilidad del &b5% &7de soltar <#FAC650>Tótems de la Inmortalidad</#FAC650> &7al morir."
            ),

            new FeverDropProfile(
                    "phantom_totem",
                    EntityType.PHANTOM,
                    "Phantom",
                    "<#FAC650>Tótems</#FAC650>",
                    Material.TOTEM_OF_UNDYING,
                    0,
                    0.12,
                    0.04,
                    1,
                    2,
                    "&3- &7Los &bPhantoms &7tienen una probabilidad del &b12% &7de soltar <#FAC650>Tótems de la Inmortalidad</#FAC650> &7al morir."
            ),
            new FeverDropProfile(
                    "sulfur_totem",
                    EntityType.SULFUR_CUBE,
                    "Sulfur Cube",
                    "<#FAC650>Tótems</#FAC650>",
                    Material.TOTEM_OF_UNDYING,
                    0,
                    0.15,
                    0.04,
                    1,
                    1,
                    "&3- &7Los &bSulfur Cubes &7tienen una probabilidad del &b15% &7de soltar <#FAC650>Tótems de la Inmortalidad</#FAC650> &7al morir."
            ),

            new FeverDropProfile(
                    "skeleton_gold",
                    EntityType.SKELETON,
                    "Esqueleto",
                    "<#F7A31E>Oro</#F7A31E>",
                    Material.RAW_GOLD,
                    0,
                    0.25,
                    0.05,
                    1,
                    10,
                    "&3- &7Los &bEsqueletos &7tienen una probabilidad del &b25% &7de soltar <#FAC650>Oro Crudo</#FAC650> &7al morir."
            ),

            new FeverDropProfile(
                    "piglin_gold",
                    EntityType.PIGLIN,
                    "Piglin",
                    "<#F7A31E>Pepitas de Oro</#F7A31E>",
                    Material.GOLD_NUGGET,
                    0,
                    0.50,
                    0.001,
                    63,
                    126,
                    "&3- &7Los &bPiglins &7tienen una probabilidad del &b50% &7de soltar <#FAC650>Pepitas de Oro</#FAC650> &7al morir."
            ),

            new FeverDropProfile(
                    "pigman_gold",
                    EntityType.ZOMBIFIED_PIGLIN,
                    "Zombified Piglin",
                    "<#F7A31E>Oro</#F7A31E>",
                    Material.RAW_GOLD,
                    0,
                    0.25,
                    0.05,
                    1,
                    5,
                    "&3- &7Los &bZombified Piglins &7tienen una probabilidad del &b25% &7de soltar <#FAC650>Oro Crudo</#FAC650> &7al morir."
            ),

            new FeverDropProfile(
                    "vex_diamond",
                    EntityType.VEX,
                    "Vex",
                    "<#5FDAF5>Diamantes</#5FDAF5>",
                    Material.DIAMOND,
                    0,
                    0.20,
                    0.5,
                    1,
                    8,
                    "&3- &7Los &bVex &7tienen una probabilidad del &b20% &7de soltar <#5FDAF5>Diamantes</#5FDAF5> &7al morir."
            ),

            new FeverDropProfile(
                    "dolphin_diamond",
                    EntityType.DOLPHIN,
                    "Delfín",
                    "<#5FDAF5>Diamantes</#5FDAF5>",
                    Material.DIAMOND,
                    0,
                    0.80,
                    0.5,
                    5,
                    15,
                    "&3- &7Los &bDelfínes &7tienen una probabilidad del &b80% &7de soltar <#5FDAF5>Diamantes</#5FDAF5> &7al morir."
            ),

            new FeverDropProfile(
                    "cave_diamond",
                    EntityType.CAVE_SPIDER,
                    "Araña de Cueva",
                    "<#5FDAF5>Diamantes</#5FDAF5>",
                    Material.DIAMOND,
                    0,
                    0.28,
                    0.2,
                    2,
                    6,
                    "&3- &7Las &bArañas de Cueva &7tienen una probabilidad del &b28% &7de soltar <#5FDAF5>Diamantes</#5FDAF5> &7al morir."
            ),


            new FeverDropProfile(
                    "magma_netherite",
                    EntityType.MAGMA_CUBE,
                    "Magma Cube",
                    "<#7D4A16>Netherite</#7D4A16>",
                    Material.NETHERITE_SCRAP,
                    0,
                    0.15,
                    0.5,
                    1,
                    3,
                    "&3- &7Los &bMagma Cubes &7tienen una probabilidad del &b15% &7de soltar <#7D4A16>Netherite</#7D4A16> &7al morir."
            ),

            new FeverDropProfile(
                    "strider_netherite",
                    EntityType.STRIDER,
                    "Strider",
                    "<#7D4A16>Netherite</#7D4A16>",
                    Material.NETHERITE_SCRAP,
                    0,
                    0.8,
                    0.5,
                    1,
                    3,
                    "&3- &7Los &bStriders &7tienen una probabilidad del &b8% &7de soltar <#7D4A16>Netherite</#7D4A16> &7al morir."
            ),

            new FeverDropProfile(
                    "pigman_netherite",
                    EntityType.ZOMBIFIED_PIGLIN,
                    "Zombified Piglin",
                    "<#7D4A16>Lingotes de Netherite</#7D4A16>",
                    Material.NETHERITE_INGOT,
                    0,
                    0.1,
                    0.1,
                    1,
                    1,
                    "&3- &7Los &bZombified Piglins &7tienen una probabilidad del &b1% &7de soltar <#7D4A16>Lingotes de Netherite</#7D4A16> &7al morir."
            ),

            new FeverDropProfile(
                    "spider_exp",
                    EntityType.SPIDER,
                    "Araña",
                    "&aExperiencia",
                    null,
                    4,
                    1,
                    0.03,
                    0,
                    0,
                    "&3- &7Las &bArañas &7sueltan &bx4 de experiencia &7al morir."
            ),

            new FeverDropProfile(
                    "silverfish_exp",
                    EntityType.SPIDER,
                    "Lepisma",
                    "&aExperiencia",
                    null,
                    5,
                    1,
                    0.001,
                    0,
                    0,
                    "&3- &7Los &bLepismas &7sueltan &bx5 de experiencia &7al morir."
            ),

            new FeverDropProfile(
                    "pigman_xp",
                    EntityType.ZOMBIFIED_PIGLIN,
                    "Zombified Piglin",
                    "&aExperiencia",
                    null,
                    5,
                    1,
                    0.001,
                    0,
                    0,
                    "&3- &7Los &bZombified Piglins &7sueltan &bx5 de experiencia &7al morir."
            )
    );
}

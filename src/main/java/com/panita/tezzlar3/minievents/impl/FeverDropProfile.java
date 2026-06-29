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
                    "Zombies",
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
            )
    );
}

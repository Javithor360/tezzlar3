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
                    "Zombi",
                    "<#F5E727>Manzanas Doradas</#F5E727>",
                    Material.GOLDEN_APPLE,
                    0,
                    0.08,
                    0.02,
                    1,
                    2,
                    "<yellow>Probabilidad: 8% | Cantidad: 1-2</yellow>"
            ),

            new FeverDropProfile(
                    "creeper_totem",
                    EntityType.CREEPER,
                    "Creeper",
                    "<#FADC82>Tótems</#FADC82>",
                    Material.TOTEM_OF_UNDYING,
                    0,
                    0.03,
                    0.01,
                    1,
                    1,
                    "<yellow>Probabilidad: 3% | Cantidad: 1</yellow>"
            ),

            new FeverDropProfile(
                    "skeleton_gold",
                    EntityType.SKELETON,
                    "Esqueleto",
                    "<#F7B52D>Oro</#F7B52D>",
                    Material.GOLD_BLOCK,
                    0,
                    0.15,
                    0.05,
                    2,
                    5,
                    "<yellow>Probabilidad: 15% | Cantidad: 2-5</yellow>"
            ),

            new FeverDropProfile(
                    "spider_exp",
                    EntityType.SPIDER,
                    "Araña",
                    "<#1C9C23>Experiencia</#1C9C23>",
                    null,
                    10.0,
                    0.10,
                    0.03,
                    0,
                    0,
                    "<yellow>Probabilidad: 10% | Multiplicador de XP: x10</yellow>"
            )
    );
}

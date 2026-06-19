package com.panita.tezzlar3.inventory.util;

import java.util.Arrays;
import java.util.List;

public class InventoryConfigDefaults {
    public static final boolean INVENTORY_ENABLED = true;
    public static final boolean INVENTORY_REVENANTZOMBIE = true;
    public static final List<String> REVENANT_MINIONS = Arrays.asList("ZOMBIE", "SKELETON", "CAVE_SPIDER", "SLIME", "PARCHED", "BOGGED", "DROWNED");
    public static final int REVENANT_MINION_COUNT = 3;
    public static final String DEATH_COORDS_REMINDER = "\n <dark_red><bold>¡CUIDADO!</bold></dark_red>\n <gray>El zombi vengativo aguarda en el lugar de tu muerte.\n <gray>Tus objetos te esperan en las coordenadas:\n <red><bold>X:</bold> {x}  <bold>Y:</bold> {y}  <bold>Z:</bold> {z}</red>\n <gray>Dimensión: <gold>{dimension}</gold>\n";
}

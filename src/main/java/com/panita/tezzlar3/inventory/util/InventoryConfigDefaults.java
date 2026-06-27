package com.panita.tezzlar3.inventory.util;

import java.util.Arrays;
import java.util.List;

public class InventoryConfigDefaults {
    public static final boolean INVENTORY_ENABLED = true;
    public static final boolean INVENTORY_REVENANTZOMBIE = true;
    public static final List<String> REVENANT_MINIONS = Arrays.asList("ZOMBIE", "SKELETON", "CAVE_SPIDER", "SLIME", "PARCHED", "BOGGED", "DROWNED");
    public static final int REVENANT_MINION_COUNT = 3;
    public static final double REVENANT_HEALTH = 50.0;
    public static final String DEATH_COORDS_REMINDER = "<gray>Parece que tienes una tumba activa en <red><bold>X:</bold> {x}  <bold>Y:</bold> {y}  <bold>Z:</bold> {z} ({dimension})</red>. Se te recomienda pedir ayuda a un compañero para recuperar tus cosas.</gray>";
}

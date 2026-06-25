package com.panita.tezzlar3.missions.util;

import java.util.Map;

public class MissionsConfigDefaults {
    public static final String MISSIONS_MESSAGES_INDIVIDUAL_COMPLETED = "&aEl jugador &e%player% &acompletó la misión: &6%mission%";
    public static final String MISSIONS_MESSAGES_GROUP_COMPLETED = "&a¡Todos han completado la misión grupal: &6%mission%!";
    public static final String MISSIONS_MESSAGES_REWARD_CLAIMED = "&eHas reclamado las recompensas de la misión: &6%mission%";
    public static final String MISSIONS_MESSAGES_PUNISHMENT_WARNING = "&c[!] Tienes castigos activos. Usa /punishments para verlos.";
    public static final String MISSIONS_MESSAGES_REWARDS_EMPTY = "&cNo tienes recompensas pendientes por reclamar.";
    public static final String MISSIONS_MESSAGES_PUNISHMENTS_EMPTY = "&aNo tienes castigos activos.";
    
    public static final Map<String, String> MISSIONS_PUNISHMENTS_DICTIONARY = Map.of(
            "DIAMOND_BAN", "No puedes guardar diamantes en tu inventario.",
            "DAMAGE_MULTIPLIER", "Recibes 1.5x de daño de todas las fuentes.",
            "HOSTILE_IRON_GOLEMS", "Los Gólems de Hierro te atacarán naturalmente.",
            "REGENERATION_TO_WITHER", "Recibes Wither II en lugar de Regeneración.",
            "WITHER_SKELETON_SPAWN_ON_DAMAGE", "Posibilidad de invocar Esqueletos Wither al recibir daño en el Nether."
    );
}

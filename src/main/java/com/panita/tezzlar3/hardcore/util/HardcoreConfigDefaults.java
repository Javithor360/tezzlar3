package com.panita.tezzlar3.hardcore.util;

import java.util.Arrays;
import java.util.List;

public class HardcoreConfigDefaults {
    public static final boolean HARDCORE_ENABLED = true;
    public static final String HARDCORE_KICKMESSAGE = "<dark_red><bold>¡Has perdido una vida Hardcore!</bold></dark_red><newline><red>Muertes totales: <yellow>%deaths%</yellow></red><newline><gray>Estarás exiliado por <red>%ban_duration%</red>.</gray>";
    public static final String HARDCORE_BANMESSAGE = "<dark_red><bold>¡Has perdido una vida Hardcore!</bold></dark_red><newline><red>Muertes totales: <yellow>%deaths%</yellow></red><newline><gray>Tu exilio terminará en <red>%ban_duration%</red>.</gray>";
    public static final String HARDCORE_WARNKICKMESSAGE = "<red>¡Has muerto!</red><newline><gray>Esta fue tu muerte #<yellow>%deaths%</yellow> de 3 permitidas libres.</gray><newline><yellow>Tómate un respiro antes de volver a entrar.</yellow>";
    public static final String HARDCORE_BROADCASTDEATHMESSAGE = "<dark_red><bold>[HARDCORE]</bold></dark_red> <red>El jugador <yellow>%player_name%</yellow> ha caído (Muerte #%deaths%).</red>";

    public static final List<String> HARDCORE_DEATHSOUNDS = Arrays.asList(
            "entity.wither.spawn;1.0;0.5",
            "entity.lightning_bolt.thunder;1.0;1.0"
    );
}

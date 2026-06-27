package com.panita.tezzlar3.hardcore.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HardcoreConfigDefaults {
    public static final boolean HARDCORE_ENABLED = true;
    public static final boolean HARDCORE_PLACESKULLONDEATH = true;
    public static final String HARDCORE_KICKMESSAGE = "<aqua><bold>TEZZLAR</bold></aqua><newline><newline><red>¡Acabas de morir y perder una vida en modo Hardcore!</red><newline><red>Pero no te preocupes que no has sido baneado aún.</red><newline><yellow>Se te recomienda tomar un breve respiro antes de volver a entrar.</yellow><newline><newline><gray>Vidas utilizadas: <aqua>%tezzlar_consumedlives%</aqua>/<aqua>%tezzlar_maxlives%</aqua></gray>";
    public static final String HARDCORE_BANMESSAGE = "<aqua><bold>TEZZLAR</bold></aqua><newline><newline><red>¡Acabas de morir y perder tu <yellow>%tezzlar_totaldeaths%°</yellow> vida en modo Hardcore!</red><newline><red>Por lo tanto se te ordena que tomes un descanso.</red><newline><newline><gray>Podrás volver a jugar dentro de <red>%ban_duration%</red>.</gray>";
    public static final String HARDCORE_WARNKICKMESSAGE = HARDCORE_KICKMESSAGE;

    public static final List<String> HARDCORE_DEATHSOUNDS = Arrays.asList(
            "entity.wither.spawn;1.0;0.5",
            "entity.lightning_bolt.thunder;1.0;1.0"
    );

    public static final String HARDCORE_GENERICDEATHMESSAGE = "<dark_red>Las fuerzas del mal en este mundo han sumido en las tinieblas a <yellow>%player_name%</yellow> y su suplicio han iniciado una tormenta maligna</dark_red>";
    
    public static final String HARDCORE_DEATHTITLE = "<dark_red><bold>¡HAS MUERTO!</bold></dark_red>";
    public static final String HARDCORE_DEATHSUBTITLE = "<gray>El alma de <yellow>%player_name%</yellow> pertenece ahora al vacío...</gray>";
    
    public static final Map<String, String> HARDCORE_DEATHMESSAGES = new HashMap<String, String>() {{
        put("default", "<gray>El alma de <yellow>%player_name%</yellow> se ha perdido en el vacío.</gray>");
    }};
}

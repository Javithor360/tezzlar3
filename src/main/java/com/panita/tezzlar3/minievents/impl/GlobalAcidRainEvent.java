package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.difficulty.mechanics.AcidRainMechanic;
import com.panita.tezzlar3.minievents.MiniEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GlobalAcidRainEvent implements MiniEvent {

    @Override
    public String getId() {
        return "acid_rain_global";
    }

    @Override
    public String getDisplayName() {
        return "<#81C784>Lluvia Ácida XL</#81C784>";
    }

    @Override
    public String getDescription() {
        return "\n&7Durante las próximas &b2 horas&7, un fenómeno climatológico altamente peligroso invadirá el terreno del Overworld.\n\n&3- &7Permanecer bajo la lluvia te resta salud periódicamente.\n\n&7Por lo tanto, no se recomienda permanecer en la superficie durante periodos de tiempo extendidos.";
    }

    @Override
    public long getDurationTicks() {
        return 144000L; // 2 hours in real time (72000 * 2)
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public void start(JavaPlugin plugin) {
        AcidRainMechanic mechanic = AcidRainMechanic.getInstance();
        if (mechanic != null) {
            mechanic.forceAcidRain();
        }
    }

    @Override
    public void stop(JavaPlugin plugin) {
        AcidRainMechanic mechanic = AcidRainMechanic.getInstance();
        if (mechanic != null) {
            mechanic.stopForcedAcidRain();
        }
    }
}

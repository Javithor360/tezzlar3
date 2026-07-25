package com.panita.tezzlar3.missions.handlers.punishments;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.missions.handlers.PunishmentHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class GlobalContaminationPunishmentHandler implements PunishmentHandler {
    @Override
    public String getId() {
        return "GLOBAL_CONTAMINATION";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        NamespacedKey globalKey = new NamespacedKey(Tezzlar.getInstance(), "global_contamination");
        player.getPersistentDataContainer().set(globalKey, PersistentDataType.BYTE, (byte) 1);
    }
}

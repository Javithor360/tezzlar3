package com.panita.tezzlar3.qol.listeners;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.qol.util.QolConfigDefaults;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;

public class TotemConsumeAlert implements Listener {
    @EventHandler
    public void onTotemConsume(EntityResurrectEvent event) {
        if (event.isCancelled()) return;

        if (!(event.getEntity() instanceof Player player)) return;

        if (!(Tezzlar.getConfigManager()
                .getBoolean("quality-of-life.totems.alert", QolConfigDefaults.QOL_TOTEMS_ALERT))) return;

        Messenger.prefixedPlaceholderBroadcast(player, Tezzlar.getConfigManager()
                .getString("quality-of-life.totems.message", QolConfigDefaults.QOL_TOTEMS_MESSAGE));

        if (!Tezzlar.getConfigManager()
                .getBoolean("quality-of-life.totems.playSound", QolConfigDefaults.QOL_TOTEMS_PLAYSOUND)) return;

        String soundKey = Tezzlar.getConfigManager()
                .getString("quality-of-life.totems.soundName", QolConfigDefaults.QOL_TOTEMS_SOUNDNAME);
        SoundUtils.playGlobal(soundKey, 1.0f, 1.0f);
    }
}

package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.util.EntityUtils;
import com.panita.tezzlar3.minievents.MiniEvent;
import com.panita.tezzlar3.minievents.gui.RouletteGUI;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.SoundUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RussianRouletteEvent implements MiniEvent, Listener {

    private WanderingTrader npc;
    private final Set<UUID> playedPlayers = new HashSet<>();
    private final NamespacedKey npcKey;

    public RussianRouletteEvent() {
        this.npcKey = new NamespacedKey(Tezzlar.getInstance(), "is_roulette_npc");
    }

    @Override
    public void start(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        playedPlayers.clear();

        int x = Tezzlar.getConfigManager().getInt("missions.refuge.x", 1000);
        int y = Tezzlar.getConfigManager().getInt("missions.refuge.y", 64);
        int z = Tezzlar.getConfigManager().getInt("missions.refuge.z", 1000);
        
        World world = Bukkit.getWorld(Tezzlar.getConfigManager().getString("worldName", "world"));
        if (world != null) {
            Location spawnLoc = new Location(world, x + 0.5, y + 1.0, z + 0.5);
            spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);
            
            npc = (WanderingTrader) world.spawnEntity(spawnLoc, EntityType.WANDERING_TRADER);
            npc.setAI(false);
            npc.setInvulnerable(true);
            npc.setRemoveWhenFarAway(false);
            npc.setSilent(true);
            
            EntityUtils.setCustomName(npc, "<red><bold>Ruleta Rusa</bold></red>", true);
            npc.getPersistentDataContainer().set(npcKey, PersistentDataType.BYTE, (byte) 1);
            
            EntityUtils.setColoredGlowing(npc, NamedTextColor.RED);
        }
    }

    @Override
    public void stop(JavaPlugin plugin) {
        HandlerList.unregisterAll(this);
        playedPlayers.clear();
        
        if (npc != null && npc.isValid()) {
            EntityUtils.removeColoredGlowing(npc);
            npc.remove();
            npc = null;
        }
    }

    @Override
    public String getId() {
        return "russian_roulette";
    }

    @Override
    public String getDisplayName() {
        return "<red><bold>Ruleta Rusa</bold></red>";
    }

    @Override
    public String getDescription() {
        int x = Tezzlar.getConfigManager().getInt("missions.refuge.x", 1000);
        int y = Tezzlar.getConfigManager().getInt("missions.refuge.y", 64);
        int z = Tezzlar.getConfigManager().getInt("missions.refuge.z", 1000);
        return "\n&7Durante los próximos &b60 minutos&7, un mercader siniestro aparecerá en el Refugio.\n\n&3- &7Puedes visitarlo para jugar a la Ruleta Rusa.\n&3- &7Toma el riesgo: Tienes 70% de probabilidad de ganar una vida y 30% de perderla instantáneamente.\n&3- &7Solo puedes jugar una vez por evento.\n\n&6Si quieres jugar, ve a &e" + x + " " + y + " " + z + " &6para probar tu suerte.";
    }

    @Override
    public long getDurationTicks() {
        return 60 * 60 * 20L;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof WanderingTrader clicked) {
            if (clicked.getPersistentDataContainer().has(npcKey, PersistentDataType.BYTE)) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                
                int currentLives = HardcoreDataManager.getLives(player.getUniqueId(), player.getName());
                int maxLives = HardcoreDataManager.getMaxLives(player.getUniqueId(), player.getName());
                
                if (currentLives >= maxLives) {
                    Messenger.prefixedSend(player, "<red>El Mercader te rechaza: 'Tus vidas ya están al máximo. No puedes tentar más a la suerte.'</red>");
                    return;
                }
                
                if (playedPlayers.contains(player.getUniqueId())) {
                    Messenger.prefixedSend(player, "<red>El Mercader se ríe a carcajadas. ¡Ya jugaste en este evento! Vuelve la próxima vez.</red>");
                    SoundUtils.play(player, "entity.witch.celebrate", 1.0f, 1.0f);
                    return;
                }
                
                playedPlayers.add(player.getUniqueId());
                new RouletteGUI(player).open();
            }
        }
    }
}

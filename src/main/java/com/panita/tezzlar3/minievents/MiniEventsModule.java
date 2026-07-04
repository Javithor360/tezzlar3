package com.panita.tezzlar3.minievents;

import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.minievents.impl.*;
import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniEventsModule implements PluginModule {
    
    private boolean enabled;
    public static final String PACKAGE_NAME = "com.panita.tezzlar3.minievents";
    private static MiniEventsModule instance;
    private MiniEventManager manager;

    public MiniEventsModule(JavaPlugin plugin) {
        instance = this;
    }
    
    public static MiniEventManager getManager() {
        return instance.manager;
    }

    @Override
    public String id() {
        return "minievents";
    }

    @Override
    public String basePackage() {
        return PACKAGE_NAME;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    @Override
    public void onEnable(JavaPlugin plugin) {
        manager = new MiniEventManager(plugin);
        
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent e) {
                Player p = e.getPlayer();
                NamespacedKey key = new NamespacedKey(plugin, "hyperactivity_penalty");
                if (p.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
                    p.getPersistentDataContainer().remove(key);
                    AttributeInstance maxHealth = p.getAttribute(Attribute.MAX_HEALTH);
                    if (maxHealth != null) {
                        maxHealth.setBaseValue(maxHealth.getBaseValue() - 2.0);
                        Messenger.prefixedSend(p, "<red>Has perdido 1 corazón máximo como penalización por salir en plena hiperactividad.</red>");
                    }
                }
            }
        }, plugin);
        
        manager.registerEvent(new UHCModeEvent());
        manager.registerEvent(new RandomPotionEvent());
        manager.registerEvent(new PositionSwapEvent());
        manager.registerEvent(new SpecialDropMobEvent());
        manager.registerEvent(new BloodMoonEvent());
        manager.registerEvent(new GlobalAcidRainEvent());
        manager.registerEvent(new WrongToolDamageEvent());
        manager.registerEvent(new RushModeEvent());
        manager.registerEvent(new NoOffhandEvent());
        manager.registerEvent(new MermaidModeEvent());
        manager.registerEvent(new ResizeModeEvent());
        manager.registerEvent(new NyctophobiaEvent());
        manager.registerEvent(new ExtremeModeEvent());
        manager.registerEvent(new HyperactivityEvent());
        manager.registerEvent(new JobFairEvent());
        manager.registerEvent(new OppositeHourEvent());

        manager.init();
        enabled = true;
    }

    @Override
    public void onDisable(JavaPlugin plugin) {
        if (manager != null) {
            manager.unregisterAll();
        }
        enabled = false;
    }
}

package com.panita.tezzlar3.rebalance;

import com.panita.tezzlar3.core.modules.PluginModule;
import com.panita.tezzlar3.rebalance.listeners.StackSizeInterceptor;
import org.bukkit.plugin.java.JavaPlugin;

public class RebalanceModule implements PluginModule {
    
    private boolean enabled;
    public static final String PACKAGE_NAME = "com.panita.tezzlar3.rebalance";

    @Override
    public String id() {
        return "rebalance";
    }

    @Override
    public String basePackage() {
        return PACKAGE_NAME;
    }

    @Override
    public void onEnable(JavaPlugin plugin) {
        StackSizeInterceptor interceptor = new StackSizeInterceptor(plugin);
        plugin.getServer().getPluginManager().registerEvents(interceptor, plugin);
        
        // Background sweep task to catch /give and other silent inventory updates (every 40 ticks = 2.0s)
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                if (interceptor.sweepInventory(player.getInventory())) {
                    player.updateInventory();
                }
            }
        }, 40L, 40L);
        
        enabled = true;
    }

    @Override
    public void onDisable(JavaPlugin plugin) {
        // Nothing to clean up for now
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean value) {
        this.enabled = value;
    }
}

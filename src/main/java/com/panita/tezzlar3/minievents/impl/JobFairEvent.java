package com.panita.tezzlar3.minievents.impl;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.minievents.MiniEvent;
import com.panita.tezzlar3.qol.util.CustomItemManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class JobFairEvent implements MiniEvent, Listener {

    private final Random random = new Random();
    private JavaPlugin plugin;

    @Override
    public void start(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop(JavaPlugin plugin) {
        HandlerList.unregisterAll(this);
    }

    @Override
    public String getId() {
        return "job_fair";
    }

    @Override
    public String getDisplayName() {
        return "<#A651A3>Feria de Empleo</#A651A3>";
    }

    @Override
    public String getDescription() {
        return "\n&7Durante los próximos &b60 minutos&7, la feria de empleo estará activa.\n\n&3- &7Los monstruos tienen la posibilidad de soltar un Currículum Vitae. ¡Al recogerlo ocurrirán cosas!";
    }

    @Override
    public long getDurationTicks() {
        return 60 * 60 * 20L; // 1 hour
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Exclude players and armor stands, only monsters/animals etc.
        if (event.getEntity() instanceof Player) return;

        ItemStack cv = new ItemBuilder(Material.PAPER)
                .name("<#A651A3>Currículum Vitae</#A651A3>")
                .lore("<gray>¡Has encontrado un CV!</gray>", "<gray>Recógelo si te atreves.</gray>")
                .build();
                
        cv = CustomItemManager.addCustomMetadata(cv, "job_fair_cv");
        event.getDrops().add(cv);
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack item = event.getItem().getItemStack();
        if (CustomItemManager.isCustomItem(item, "job_fair_cv")) {
            // Broadcast prefixed message
            String broadcastMessage = "<#A651A3>" + player.getName() + " ha tirado un CV para buscar laburo</#A651A3>";
            Messenger.prefixedBroadcast(broadcastMessage);
            
            // Deal random damage between 4 and 12
            double damage = 4.0 + (random.nextDouble() * 8.0); // 4.0 to 12.0
            player.setMetadata("job_fair_death", new FixedMetadataValue(plugin, System.currentTimeMillis()));
            player.damage(damage);
            
            // Consume the item so it doesn't fill the inventory
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.hasMetadata("job_fair_death")) {
            long time = player.getMetadata("job_fair_death").get(0).asLong();
            if (System.currentTimeMillis() - time <= 100) {
                event.setDeathMessage(player.getName() + " murió porque la jornada laboral lo mató.");
            }
            if (plugin != null) {
                player.removeMetadata("job_fair_death", plugin);
            }
        }
    }
}

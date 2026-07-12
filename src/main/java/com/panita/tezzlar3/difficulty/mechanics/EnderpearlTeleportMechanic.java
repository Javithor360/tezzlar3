package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public class EnderpearlTeleportMechanic extends DifficultyMechanic {

    public EnderpearlTeleportMechanic(JavaPlugin plugin) {
        super(plugin, 28);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnderpearlTeleport(PlayerTeleportEvent event) {
        if (!isActive()) return;
        
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        Player player = event.getPlayer();
        Location originalTo = event.getTo();
        if (originalTo == null) return;

        double offsetX = (ThreadLocalRandom.current().nextDouble() * 20.0) - 10.0;
        double offsetZ = (ThreadLocalRandom.current().nextDouble() * 20.0) - 10.0;

        Location targetLoc = originalTo.clone().add(offsetX, 0, offsetZ);

        // Find a safe Y (2 air blocks)
        boolean foundSafe = false;
        World world = targetLoc.getWorld();
        
        for (int yOffset = 0; yOffset <= 15; yOffset++) {
            // Check upwards
            Location checkUp = targetLoc.clone().add(0, yOffset, 0);
            if (isSafe(checkUp)) {
                targetLoc = checkUp;
                foundSafe = true;
                break;
            }
            // Check downwards
            Location checkDown = targetLoc.clone().add(0, -yOffset, 0);
            if (isSafe(checkDown)) {
                targetLoc = checkDown;
                foundSafe = true;
                break;
            }
        }

        if (!foundSafe) {
            // Fallback: highest block if it's the overworld, otherwise just keep the original Y with offset
            if (world != null && world.getEnvironment() == World.Environment.NORMAL) {
                targetLoc.setY(world.getHighestBlockYAt(targetLoc) + 1);
            }
        }

        // Random completely new pitch (-90 to 90) and yaw (0 to 360)
        float newPitch = (ThreadLocalRandom.current().nextFloat() * 180f) - 90f;
        float newYaw = ThreadLocalRandom.current().nextFloat() * 360f;

        targetLoc.setPitch(newPitch);
        targetLoc.setYaw(newYaw);

        event.setTo(targetLoc);
    }

    private boolean isSafe(Location loc) {
        return loc.getBlock().isPassable() && loc.clone().add(0, 1, 0).getBlock().isPassable();
    }
}

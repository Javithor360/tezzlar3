package com.panita.tezzlar3.difficulty.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class VehicleDismountMechanic extends DifficultyMechanic {

    public VehicleDismountMechanic(JavaPlugin plugin) {
        super(plugin, 18);
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!isActive()) return;

        Entity vehicle = event.getVehicle();
        Entity entered = event.getEntered();

        if (entered instanceof Monster) {
            if (vehicle instanceof Boat || vehicle instanceof Minecart) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (entered.isValid() && entered.getVehicle() != null && entered.getVehicle().equals(vehicle)) {
                        entered.leaveVehicle();
                    }
                }, 100L); // 5 seconds (20 ticks * 5)
            }
        }
    }
}

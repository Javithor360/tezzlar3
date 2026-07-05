package com.panita.tezzlar3.hardcore.util;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.timeline.util.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DeathTracker {

    private static final String FILE_NAME = "deaths_tracker.csv";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logDeath(Player player, int totalDeaths, Location loc, String vanillaMessage) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        int serverDay = TimeManager.getCurrentDay();
        
        String reason = "UNKNOWN";
        if (player.getLastDamageCause() != null) {
            reason = player.getLastDamageCause().getCause().name();
        }
        
        String worldName = loc.getWorld() != null ? loc.getWorld().getName() : "unknown";
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        
        // Escape vanilla message for CSV
        String safeVanillaMsg = vanillaMessage != null ? vanillaMessage.replace("\"", "\"\"") : "";
        
        String csvLine = String.format("\"%s\",%d,\"%s\",%d,\"%s\",\"%s\",%d,%d,%d,\"%s\"",
                timestamp, serverDay, player.getName(), totalDeaths, reason, worldName, x, y, z, safeVanillaMsg);

        Bukkit.getScheduler().runTaskAsynchronously(Tezzlar.getInstance(), () -> {
            File file = new File(Tezzlar.getInstance().getDataFolder(), FILE_NAME);
            boolean isNew = !file.exists();
            
            try (FileWriter fw = new FileWriter(file, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                
                if (isNew) {
                    pw.println("Timestamp,ServerDay,Player,TotalDeaths,Reason,World,X,Y,Z,VanillaMessage");
                }
                
                pw.println(csvLine);
                
            } catch (IOException e) {
                Tezzlar.getInstance().getLogger().severe("Could not log death to " + FILE_NAME + ": " + e.getMessage());
            }
        });
    }
}

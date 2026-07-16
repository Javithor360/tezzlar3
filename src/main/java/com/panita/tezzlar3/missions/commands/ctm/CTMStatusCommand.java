package com.panita.tezzlar3.missions.commands.ctm;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

@SubCommandSpec(
        parent = "ctm",
        name = "status",
        description = "Show the list of players with an chosen wool (only works with online people)",
        syntax = "/ctm status"
)
public class CTMStatusCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        NamespacedKey colorKey = new NamespacedKey(Tezzlar.getInstance(), "ctm_chosen_color");
        boolean found = false;
        
        Messenger.prefixedSend(sender, "&6--- &eEstado de Lanas CTM &6---");
        for (Player p : Bukkit.getOnlinePlayers()) {
            String color = p.getPersistentDataContainer().get(colorKey, PersistentDataType.STRING);
            if (color != null) {
                Messenger.prefixedSend(sender, "&a" + p.getName() + " &7está vinculado a la lana: " + translateColor(color));
                found = true;
            }
        }
        
        if (!found) {
            Messenger.prefixedSend(sender, "&cActualmente ningún jugador en línea está vinculado a una lana.");
        }
    }
    
    private String translateColor(String color) {
        switch(color) {
            case "orange": return "&6Naranja";
            case "red": return "&cRoja";
            case "blue": return "&9Azul";
            case "lime": return "&aVerde Lima";
            case "yellow": return "&eAmarilla";
            case "purple": return "&5Morada";
            default: return color;
        }
    }
}

package com.panita.tezzlar3.hardcore.commands.hardcore;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

@SubCommandSpec(
        parent = "tezzlar hardcore",
        name = "topDeaths",
        description = "Muestra el top 10 de jugadores con más muertes.",
        syntax = "/hardcore topDeaths",
        permission = "tezzlar.command.hardcore.topdeaths"
)
public class HardcoreTopDeathsCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<Map.Entry<String, Integer>> top = HardcoreDataManager.getTopDeaths(10);
        
        if (top.isEmpty()) {
            Messenger.prefixedSend(sender, "<red>No hay registros de muertes todavía.</red>");
            return;
        }

        Messenger.prefixedSend(sender, " <dark_red><bold>☠ TOP 10 MUERTES ☠</bold></dark_red>");
        Messenger.send(sender, "");
        
        int rank = 1;
        for (Map.Entry<String, Integer> entry : top) {
            String color = "<gray>";
            if (rank == 1) color = "<gold><bold>";
            else if (rank == 2) color = "<white><bold>";
            else if (rank == 3) color = "<color:#cd7f32><bold>"; // Bronze
            
            Messenger.send(sender, "  " + color + "#" + rank + "</bold> <gray>- <white>" + entry.getKey() + " <dark_gray>» <red>" + entry.getValue() + " muertes</red>");
            rank++;
        }
    }
}

package com.panita.tezzlar3.difficulty.commands.mobcap;

import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.chat.Messenger;

import com.panita.tezzlar3.difficulty.commands.MobcapCommand;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@SubCommandSpec(
    name = "top",
    parent = "mobcap",
    description = "Shows the top 10 most abundant mobs in the world.",
    permission = "tezzlar3.command.mobcap",
    syntax = "/mobcap top",
    playerOnly = true
)
public class MobcapTopCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        World world = player.getWorld();

        Map<EntityType, Integer> counts = new HashMap<>();
        for (Entity entity : world.getEntities()) {
            counts.put(entity.getType(), counts.getOrDefault(entity.getType(), 0) + 1);
        }

        Map<EntityType, Integer> topCounts = counts.entrySet().stream()
                .sorted(Map.Entry.<EntityType, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, 
                        Map.Entry::getValue, 
                        (e1, e2) -> e1, 
                        LinkedHashMap::new
                ));

        Messenger.send(player, "");
        Messenger.send(player, "<#64B5F6><b>Top 10 Mobs en " + world.getName() + "</b></#64B5F6>");
        Messenger.send(player, "<dark_gray>----------------------------------------</dark_gray>");

        int rank = 1;
        for (Map.Entry<EntityType, Integer> entry : topCounts.entrySet()) {
            Messenger.send(player, " <#81C784>" + rank + ".</#81C784> <white>" + entry.getKey().name() + "</white>: <#FFCA28>" + entry.getValue() + "</#FFCA28>");
            rank++;
        }

        Messenger.send(player, "<dark_gray>----------------------------------------</dark_gray>");
        Messenger.send(player, "");
    }
}

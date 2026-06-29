package com.panita.tezzlar3.difficulty.commands;

import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.chat.Messenger;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@CommandSpec(
    name = "mobcap", 
    description = "Shows the current state of world mobcaps.", 
    permission = "tezzlar3.command.mobcap", 
    syntax = "/mobcap", 
    playerOnly = true
)
public class MobcapCommand implements AdvancedCommand, TabSuggestingCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        World world = player.getWorld();
        
        Map<SpawnCategory, Integer> counts = new HashMap<>();
        for (SpawnCategory category : SpawnCategory.values()) {
            counts.put(category, 0);
        }
        
        for (Entity entity : world.getEntities()) {
            SpawnCategory cat = entity.getSpawnCategory();
            counts.put(cat, counts.getOrDefault(cat, 0) + 1);
        }
        
        Messenger.send(player, "");
        Messenger.send(player, "<#64B5F6><b>Estado de Mobcaps en " + world.getName() + "</b></#64B5F6>");
        Messenger.send(player, "<dark_gray>----------------------------------------</dark_gray>");
        
        for (SpawnCategory category : SpawnCategory.values()) {
            if (category == SpawnCategory.MISC) continue;
            
            int limit = world.getSpawnLimit(category);
            if (limit <= 0) continue; 
            
            int count = counts.get(category);
            
            String color = "<#81C784>"; // Green
            if (count >= limit) {
                color = "<#FF5252>"; // Red
            } else if (count >= limit * 0.8) {
                color = "<#FFCA28>"; // Yellow
            }
            
            Messenger.send(player, " <gray>•</gray> <white>" + category.name() + "</white>: " + color + count + "</" + color.substring(1, 8) + "> <gray>/</gray> <#9E9E9E>" + limit + "</#9E9E9E>");
        }
        
        Messenger.send(player, "<dark_gray>----------------------------------------</dark_gray>");
        Messenger.send(player, "");
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> Collections.singletonList("top"));
    }
}

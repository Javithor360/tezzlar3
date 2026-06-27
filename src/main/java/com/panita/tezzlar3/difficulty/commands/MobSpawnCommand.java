package com.panita.tezzlar3.difficulty.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.difficulty.mobs.CustomMobManager;
import com.panita.tezzlar3.difficulty.mobs.CustomMobType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

@CommandSpec(
        name = "mobspawn",
        description = "Spawnea mobs customizados del sistema de dificultad.",
        syntax = "/mobspawn <mob>",
        permission = "tezzlar.command.mobspawn"
)
public class MobSpawnCommand implements AdvancedCommand, TabSuggestingCommand {
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Messenger.prefixedSend(sender, "&cEste comando es solo para jugadores.");
            return;
        }
        
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, MobSpawnCommand.class)) {
            return;
        }
        
        try {
            CustomMobType type = CustomMobType.valueOf(args[0].toUpperCase());
            CustomMobManager.spawn(type, player.getLocation());
            Messenger.prefixedSend(sender, "&aSe ha spawneado un &e" + type.name() + "&a.");
        } catch (IllegalArgumentException e) {
            Messenger.prefixedSend(sender, "&cMob inválido. Usa TAB para ver la lista.");
        }
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> Arrays.stream(CustomMobType.values())
                .map(Enum::name)
                .filter(name -> name.toLowerCase().startsWith(context.getCurrentArg().toLowerCase()))
                .collect(Collectors.toList()));
    }
}

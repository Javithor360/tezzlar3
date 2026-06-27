package com.panita.tezzlar3.hardcore.commands.hardcore.player;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.hardcore.HardcoreModule;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@SubCommandSpec(
        parent = "tezzlar hardcore player",
        name = "unban",
        description = "Unbans a player exiled by deaths.",
        syntax = "/tezzlar hardcore player unban <player>",
        permission = "tezzlar.command.hardcore.player.unban"
)
public class HardcorePlayerUnbanCommand implements AdvancedCommand, TabSuggestingCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        String targetName = args[0];
        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        
        if (!Global.isValidPlayer(sender, target, targetName)) {
            return;
        }

        long banExpiration = HardcoreDataManager.getBanExpiration(target.getUniqueId(), target.getName());
        
        if (banExpiration <= System.currentTimeMillis()) {
            Messenger.prefixedSend(sender, "&eEl jugador &c" + target.getName() + " &eno se encuentra baneado por muertes actualmente.");
            return;
        }

        HardcoreDataManager.setBanExpiration(target.getUniqueId(), target.getName(), 0L);
        HardcoreModule.getPendingKicks().remove(target.getUniqueId());
        
        Messenger.prefixedSend(sender, "&aHas desbaneado a &e" + target.getName() + "&a. Ya puede volver a entrar al servidor.");
    }
    
    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> {
            String current = context.getCurrentArg().toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(current))
                    .collect(Collectors.toList());
        });
    }
}

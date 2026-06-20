package com.panita.tezzlar3.hardcore.commands.hardcore.player;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.core.util.CommandUtils;
import com.panita.tezzlar3.core.util.Global;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

@SubCommandSpec(
        parent = "hardcore player",
        name = "unban",
        description = "Desbanea a un jugador que murió en modo hardcore.",
        syntax = "/hardcore player unban <jugador>",
        permission = "tezzlar.command.hardcore.player.unban"
)
public class HardcorePlayerUnbanCommand implements AdvancedCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!CommandUtils.checkArgsOrUsage(sender, args, 1, this.getClass())) return;

        String targetName = args[0];
        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        
        if (!Global.isValidPlayer(sender, target, targetName)) {
            return;
        }

        long banExpiration = HardcoreDataManager.getBanExpiration(target.getUniqueId());
        
        if (banExpiration <= System.currentTimeMillis()) {
            Messenger.prefixedSend(sender, "&eEl jugador &c" + target.getName() + " &eno se encuentra baneado por muertes actualmente.");
            return;
        }

        HardcoreDataManager.setBanExpiration(target.getUniqueId(), target.getName(), 0L);
        Messenger.prefixedSend(sender, "&aHas desbaneado a &e" + target.getName() + "&a. Ya puede volver a entrar al servidor.");
    }
}

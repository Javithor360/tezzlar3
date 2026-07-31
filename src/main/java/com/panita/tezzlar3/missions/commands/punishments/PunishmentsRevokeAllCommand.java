package com.panita.tezzlar3.missions.commands.punishments;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;
import com.panita.tezzlar3.missions.MissionsModule;
import com.panita.tezzlar3.missions.data.PlayerMissionData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SubCommandSpec(
        parent = "punishments",
        name = "revokeall",
        description = "Revokes all active punishments from all online players.",
        syntax = "/punishments revokeall",
        permission = "tezzlar.admin"
)
public class PunishmentsRevokeAllCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        int count = 0;
        
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            PlayerMissionData data = MissionsModule.getDataManager().getPlayerData(onlinePlayer);
            if (data != null && !data.getActivePunishments().isEmpty()) {
                List<String> toRemove = new ArrayList<>(data.getActivePunishments());
                for (String punishment : toRemove) {
                    data.removePunishment(punishment);
                }
                count++;
                Messenger.prefixedSend(onlinePlayer, "&a¡Todos tus castigos activos han sido revocados por un administrador!");
            }
        }
        
        Messenger.prefixedSend(sender, "&aSe han revocado todos los castigos activos de &e" + count + " &ajugadores en línea.");
    }
}

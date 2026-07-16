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
        name = "reset",
        description = "Resets the player link to a wool in the CTM mission",
        syntax = "/ctm reset <jugador>"
)
public class CTMResetCommand implements AdvancedCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Messenger.prefixedSend(sender, "&cUso incorrecto. Utiliza: &7/ctm reset <jugador>");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            Messenger.prefixedSend(sender, "&cEl jugador '&7" + args[0] + "&c' no está en línea o no existe.");
            return;
        }

        NamespacedKey colorKey = new NamespacedKey(Tezzlar.getInstance(), "ctm_chosen_color");
        if (target.getPersistentDataContainer().has(colorKey, PersistentDataType.STRING)) {
            target.getPersistentDataContainer().remove(colorKey);
            Messenger.prefixedSend(sender, "&aSe ha reseteado el vínculo de lana del jugador &e" + target.getName() + "&a exitosamente.");
            Messenger.prefixedSend(target, "&aUn administrador ha reseteado tu vínculo de lana. Ahora puedes agarrar otra lana.");
        } else {
            Messenger.prefixedSend(sender, "&cEl jugador &e" + target.getName() + " &cno tiene ninguna lana vinculada actualmente.");
        }
    }
}

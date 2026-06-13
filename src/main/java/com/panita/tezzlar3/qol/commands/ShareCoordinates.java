package com.panita.tezzlar3.qol.commands;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.qol.util.QolConfigDefaults;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandSpec(
        name = "sharecoordinates",
        description = "Share your coordinates with other players.",
        syntax = "/sharecoordinates [player]",
        aliases = {"c", "coords"},
        playerOnly = true
)
public class ShareCoordinates implements AdvancedCommand, TabSuggestingCommand {
    private boolean playSoundEnabled() {
        return Tezzlar.getConfigManager().getBoolean(
                "quality-of-life.sharecoordinates.playSound",
                QolConfigDefaults.QOL_SHARECOORDINATES_PLAYSOUND
        );
    }

    private void play(Player player, String path, String fallback) {
        if (!playSoundEnabled()) return;
        String key = Tezzlar.getConfigManager().getString(path, fallback);
        SoundUtils.play(player, key, 1.0f, 1.0f);
    }

    private void send(Player target, String path, String def, Player context) {
        String msg = Tezzlar.getConfigManager().getString(path, def);
        Messenger.prefixedPlaceholderSend(target, context, msg);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (Tezzlar.getConfigManager().getBoolean("quality-of-life.sharecoordinates.enabled", QolConfigDefaults.QOL_SHARECOORDINATES_ENABLED)) {
            if (!(sender instanceof Player player)) {
                Messenger.consoleSend(sender, "&cEste comando solo puede ser ejecutado por un jugador.");
                return;
            }

            if (args.length > 0) {
                Player target = Bukkit.getPlayer(args[0]);

                if (target == null || !target.isOnline()) {
                    Messenger.prefixedSend(player, "&cEl jugador " + args[0] + " no está en línea.");
                    return;
                } else if (target == player) {
                    send(player, "quality-of-life.sharecoordinates.selfMessage", QolConfigDefaults.QOL_SHARECOORDINATES_SELFMESSAGE, player);
                    play(player, "quality-of-life.sharecoordinates.soundPrivate", QolConfigDefaults.QOL_SHARECOORDINATES_SOUNDPRIVATE);
                    return;
                }

                send(target, "quality-of-life.sharecoordinates.privateMessage", QolConfigDefaults.QOL_SHARECOORDINATES_PRIVATEMESSAGE, player);
                send(player, "quality-of-life.sharecoordinates.sentPrivateConfirmation", QolConfigDefaults.QOL_SHARECOORDINATES_SENTPRIVATECONFIRMATION, target);

                play(target, "quality-of-life.sharecoordinates.soundPrivate", QolConfigDefaults.QOL_SHARECOORDINATES_SOUNDPRIVATE);
                play(player, "quality-of-life.sharecoordinates.soundPrivate", QolConfigDefaults.QOL_SHARECOORDINATES_SOUNDPRIVATE);
                return;
            }

            String msg = Tezzlar.getConfigManager().getString("quality-of-life.sharecoordinates.publicMessage", QolConfigDefaults.QOL_SHARECOORDINATES_PUBLICMESSAGE);

            Messenger.prefixedPlaceholderBroadcast(player, msg);

            if (playSoundEnabled()) {
                String key = Tezzlar.getConfigManager().getString(
                        "quality-of-life.sharecoordinates.soundPublic",
                        QolConfigDefaults.QOL_SHARECOORDINATES_SOUNDPUBLIC
                );
                SoundUtils.playGlobal(key, 1.0f, 1.0f);
            }
        }
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

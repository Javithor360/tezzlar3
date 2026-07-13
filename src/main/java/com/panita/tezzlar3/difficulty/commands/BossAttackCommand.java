package com.panita.tezzlar3.difficulty.commands;

import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.commands.dynamic.AdvancedCommand;
import com.panita.tezzlar3.core.commands.dynamic.TabSuggestingCommand;
import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;
import com.panita.tezzlar3.difficulty.mechanics.GigaMagmaCubeBoss;
import com.panita.tezzlar3.difficulty.mechanics.GigaMagmaCubeMechanic;
import com.panita.tezzlar3.difficulty.mechanics.GlacialBonebreakerBoss;
import com.panita.tezzlar3.difficulty.mechanics.GlacialBonebreakerMechanic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CommandSpec(
        name = "bossattack",
        description = "Fuerza a un boss cercano a ejecutar un ataque especial.",
        syntax = "/bossattack <boss_name> [attack_id]",
        permission = "tezzlar.command.bossattack"
)
public class BossAttackCommand implements AdvancedCommand, TabSuggestingCommand {

    private static final List<String> BOSSES = Arrays.asList("GIGA_MAGMA_CUBE", "GLACIAL_BONEBREAKER");

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Messenger.prefixedSend(sender, "&cEste comando es solo para jugadores.");
            return;
        }

        if (args.length == 0) {
            Messenger.prefixedSend(sender, "&cUso: /bossattack <boss_name>");
            return;
        }

        String bossName = args[0].toUpperCase();
        if (!BOSSES.contains(bossName)) {
            Messenger.prefixedSend(sender, "&cBoss inválido. Opciones: " + String.join(", ", BOSSES));
            return;
        }

        Entity nearestBoss = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity e : player.getNearbyEntities(50, 50, 50)) {
            if (bossName.equals("GIGA_MAGMA_CUBE") && e.getPersistentDataContainer().has(GigaMagmaCubeMechanic.BOSS_KEY, PersistentDataType.BYTE)) {
                double dist = e.getLocation().distanceSquared(player.getLocation());
                if (dist < nearestDistance) {
                    nearestDistance = dist;
                    nearestBoss = e;
                }
            } else if (bossName.equals("GLACIAL_BONEBREAKER") && e.getPersistentDataContainer().has(GlacialBonebreakerMechanic.BOSS_KEY, PersistentDataType.BYTE)) {
                double dist = e.getLocation().distanceSquared(player.getLocation());
                if (dist < nearestDistance) {
                    nearestDistance = dist;
                    nearestBoss = e;
                }
            }
        }

        if (nearestBoss == null) {
            Messenger.prefixedSend(sender, "&cNo se encontró ningún " + bossName + " cercano en un radio de 50 bloques.");
            return;
        }

        int attackId = -1;
        if (args.length > 1) {
            try {
                attackId = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {}
        }

        if (bossName.equals("GIGA_MAGMA_CUBE")) {
            GigaMagmaCubeBoss bossLogic = GigaMagmaCubeMechanic.getBoss(nearestBoss.getUniqueId());
            if (bossLogic != null) {
                bossLogic.forceAttack(attackId);
                Messenger.prefixedSend(sender, "&aSe ha forzado un ataque del Giga Magma Cube. (ID: " + attackId + ")");
            } else {
                Messenger.prefixedSend(sender, "&cError: El boss existe físicamente pero no está registrado lógicamente.");
            }
        } else if (bossName.equals("GLACIAL_BONEBREAKER")) {
            GlacialBonebreakerBoss bossLogic = GlacialBonebreakerMechanic.getBoss(nearestBoss.getUniqueId());
            if (bossLogic != null) {
                bossLogic.forceAttack(attackId);
                Messenger.prefixedSend(sender, "&aSe ha forzado un ataque del Quebrantahuesos Glacial. (ID: " + attackId + ")");
            } else {
                Messenger.prefixedSend(sender, "&cError: El boss existe físicamente pero no está registrado lógicamente.");
            }
        }
    }

    @Override
    public void applySuggestions(CommandMeta meta) {
        meta.setArgumentSuggestion(0, context -> BOSSES.stream()
                .filter(name -> name.startsWith(context.getCurrentArg().toUpperCase()))
                .collect(Collectors.toList()));
    }
}

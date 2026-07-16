package com.panita.tezzlar3.difficulty.listeners;

import org.bukkit.event.Listener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import com.panita.tezzlar3.core.chat.Messenger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MobDebugListener implements Listener {
    
    private static final Set<UUID> activeDebuggers = new HashSet<>();
    
    public static boolean toggleDebug(UUID uuid) {
        if (activeDebuggers.contains(uuid)) {
            activeDebuggers.remove(uuid);
            return false;
        } else {
            activeDebuggers.add(uuid);
            return true;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!activeDebuggers.contains(player.getUniqueId())) return;
        
        event.setCancelled(true);
        
        if (!(event.getEntity() instanceof LivingEntity mob)) return;
        if (mob instanceof Player) return; // Ignore hitting other players
        
        String name = "Ninguno";
        if (mob.customName() != null) {
             name = PlainTextComponentSerializer.plainText().serialize(mob.customName());
        }
        
        double maxHp = mob.getAttribute(Attribute.MAX_HEALTH) != null ? mob.getAttribute(Attribute.MAX_HEALTH).getValue() : 0;
        double currentHp = mob.getHealth();
        
        EntityEquipment eq = mob.getEquipment();
        
        // Print to chat
        Messenger.send(player, "<dark_gray>----------------------------------------");
        Messenger.send(player, "<gold><bold>DEBUG: <yellow>" + mob.getType().name());
        Messenger.send(player, "<gray>Vida: <red>" + String.format("%.1f", currentHp) + " / " + String.format("%.1f", maxHp));
        if (!name.equals("Ninguno")) {
            Messenger.send(player, "<gray>Nombre: <white>" + name);
        }
        try {
            Messenger.send(player, "<gray>Spawn: <aqua>" + mob.getEntitySpawnReason().name());
        } catch (NoSuchMethodError ignored) {}
        
        if (eq != null) {
            printEq(player, "Mano", eq.getItemInMainHand(), eq.getItemInMainHandDropChance());
            printEq(player, "Sec.", eq.getItemInOffHand(), eq.getItemInOffHandDropChance());
            printEq(player, "Casco", eq.getHelmet(), eq.getHelmetDropChance());
            printEq(player, "Pecho", eq.getChestplate(), eq.getChestplateDropChance());
            printEq(player, "Piernas", eq.getLeggings(), eq.getLeggingsDropChance());
            printEq(player, "Botas", eq.getBoots(), eq.getBootsDropChance());
        }
        
        int count = 0;
        for (Attribute attr : Registry.ATTRIBUTE) {
            if (attr == Attribute.MAX_HEALTH) continue;
            AttributeInstance inst = mob.getAttribute(attr);
            if (inst != null && inst.getBaseValue() != 0 && count < 12) { // limit 12 attributes so it doesnt spam
                Messenger.send(player, "<gray>Atr. <white>" + attr.getKey().getKey() + ": <green>" + String.format("%.2f", inst.getValue()));
                count++;
            }
        }
        
        String rawNbt = "NBT Data no disponible";
        try {
            Object snapshot = mob.getClass().getMethod("createSnapshot").invoke(mob);
            rawNbt = (String) snapshot.getClass().getMethod("getAsString").invoke(snapshot);
        } catch (Exception e) {
            rawNbt = "No se pudo obtener el NBT. Asegúrate de usar una versión reciente de Paper.";
        }

        Component btnCopiar = Component.text("[")
            .color(NamedTextColor.WHITE)
            .append(Component.text("Copiar", NamedTextColor.GREEN))
            .append(Component.text("]"))
            .clickEvent(ClickEvent.copyToClipboard(rawNbt))
            .hoverEvent(HoverEvent.showText(Component.text("Copiar el NBT crudo del mob al portapapeles", NamedTextColor.GREEN)));
            
        Component btnUuid = Component.text("[")
            .color(NamedTextColor.WHITE)
            .append(Component.text("UUID", NamedTextColor.AQUA))
            .append(Component.text("]"))
            .clickEvent(ClickEvent.copyToClipboard(mob.getUniqueId().toString()))
            .hoverEvent(HoverEvent.showText(Component.text("Copiar UUID", NamedTextColor.AQUA)));
            
        Component btnEliminar = Component.text("[")
            .color(NamedTextColor.WHITE)
            .append(Component.text("Eliminar", NamedTextColor.RED))
            .append(Component.text("]"))
            .clickEvent(ClickEvent.runCommand("/minecraft:kill " + mob.getUniqueId().toString()))
            .hoverEvent(HoverEvent.showText(Component.text("Ejecutar /kill en este mob", NamedTextColor.RED)));
        
        Component buttons = Component.text("")
            .append(btnCopiar).append(Component.space())
            .append(btnUuid).append(Component.space())
            .append(btnEliminar);
        
        player.sendMessage(Component.text(""));
        player.sendMessage(buttons);
        Messenger.send(player, "<dark_gray>----------------------------------------");
    }
    
    private void printEq(Player player, String slot, ItemStack item, float drop) {
        if (item == null || item.getType().isAir()) return;
        Messenger.send(player, "<gray>" + slot + ": <white>" + item.getType().name() + " <dark_gray>(<green>" + String.format("%.1f", drop * 100) + "% drop<dark_gray>)");
        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
            for (java.util.Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : item.getItemMeta().getEnchants().entrySet()) {
                Messenger.send(player, "<dark_gray>  - <aqua>" + entry.getKey().getKey().getKey() + " <gray>Lvl " + entry.getValue());
            }
        }
    }
}

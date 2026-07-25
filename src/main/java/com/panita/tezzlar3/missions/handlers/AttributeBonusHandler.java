package com.panita.tezzlar3.missions.handlers;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AttributeBonusHandler implements RewardHandler, PunishmentHandler {

    private final JavaPlugin plugin;

    public AttributeBonusHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "MODIFY_ATTRIBUTE";
    }

    @Override
    public void apply(Player player, ConfigurationSection args) {
        if (args == null) return;
        
        String attrName = args.getString("attribute");
        if (attrName == null) return;
        
        double amount = args.getDouble("amount", 0.0);

        try {
            Attribute attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(attrName.toLowerCase()));
            if (attribute == null) {
                plugin.getLogger().warning("Atributo no encontrado: " + attrName);
                return;
            }
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                // Generate a unique key so bonuses accumulate
                NamespacedKey key = new NamespacedKey(plugin, "bonus_" + attrName.toLowerCase() + "_" + java.util.UUID.randomUUID().toString());
                
                // Apply the new modifier (can be positive or negative)
                AttributeModifier modifier = new AttributeModifier(key, amount, AttributeModifier.Operation.ADD_NUMBER);
                instance.addModifier(modifier);
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Atributo no encontrado: " + attrName);
        }
    }
}

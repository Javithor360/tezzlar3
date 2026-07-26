package com.panita.tezzlar3.bossfight.gui;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.bossfight.util.BossManager;
import com.panita.tezzlar3.core.gui.ItemBuilder;
import com.panita.tezzlar3.core.gui.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BossAttributesMenu extends Menu {

    public BossAttributesMenu(Player player) {
        super(player);
    }

    @Override
    public String getMenuName() {
        return "<green><bold>Atributos del Jefe";
    }

    @Override
    public int getSlots() {
        return 36;
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        
        AttributeInstance scale = player.getAttribute(Attribute.SCALE);
        double currentScale = scale != null ? scale.getBaseValue() : 1.0;
        inventory.setItem(10, new ItemBuilder(Material.SLIME_BALL)
                .name("<green><bold>Escala")
                .lore("<gray>Actual: <white>" + String.format("%.1f", currentScale),
                      "", "<green>Click Izquierdo: <gray>+0.5", "<red>Click Derecho: <gray>-0.5")
                .build());

        AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        double currentSpeed = speed != null ? speed.getBaseValue() : 0.1;
        inventory.setItem(11, new ItemBuilder(Material.SUGAR)
                .name("<aqua><bold>Velocidad")
                .lore("<gray>Actual: <white>" + String.format("%.3f", currentSpeed),
                      "", "<green>Click Izquierdo: <gray>+0.005", "<red>Click Derecho: <gray>-0.005")
                .build());

        AttributeInstance damage = player.getAttribute(Attribute.ATTACK_DAMAGE);
        double currentDamage = damage != null ? damage.getBaseValue() : 1.0;
        inventory.setItem(12, new ItemBuilder(Material.IRON_SWORD)
                .name("<red><bold>Daño Base")
                .lore("<gray>Actual: <white>" + String.format("%.1f", currentDamage),
                      "", "<green>Click Izquierdo: <gray>+1.0", "<red>Click Derecho: <gray>-1.0")
                .build());

        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        double currentHealth = maxHealth != null ? maxHealth.getBaseValue() : 20.0;
        inventory.setItem(13, new ItemBuilder(Material.GOLDEN_APPLE)
                .name("<gold><bold>Vida Máxima")
                .lore("<gray>Actual: <white>" + String.format("%.1f", currentHealth),
                      "", "<green>Click Izquierdo: <gray>+100.0", "<red>Click Derecho: <gray>-100.0")
                .build());

        AttributeInstance armor = player.getAttribute(Attribute.ARMOR);
        double currentArmor = armor != null ? armor.getBaseValue() : 0.0;
        inventory.setItem(14, new ItemBuilder(Material.IRON_CHESTPLATE)
                .name("<gray><bold>Armadura")
                .lore("<gray>Actual: <white>" + String.format("%.1f", currentArmor),
                      "", "<green>Click Izquierdo: <gray>+2.0", "<red>Click Derecho: <gray>-2.0")
                .build());

        AttributeInstance gravity = player.getAttribute(Attribute.GRAVITY);
        double currentGravity = gravity != null ? gravity.getBaseValue() : 0.08;
        inventory.setItem(15, new ItemBuilder(Material.FEATHER)
                .name("<white><bold>Gravedad")
                .lore("<gray>Actual: <white>" + String.format("%.3f", currentGravity),
                      "", "<green>Click Izquierdo: <gray>+0.005", "<red>Click Derecho: <gray>-0.005")
                .build());

        inventory.setItem(19, getPotionItem(Material.GHAST_TEAR, "Regeneración", PotionEffectType.REGENERATION));
        inventory.setItem(20, getPotionItem(Material.SHIELD, "Resistencia", PotionEffectType.RESISTANCE));
        inventory.setItem(21, getPotionItem(Material.MAGMA_CREAM, "Ignífugo", PotionEffectType.FIRE_RESISTANCE));
        inventory.setItem(22, getPotionItem(Material.BLAZE_POWDER, "Fuerza", PotionEffectType.STRENGTH));
    }

    private ItemStack getPotionItem(Material material, String name, PotionEffectType type) {
        PotionEffect effect = player.getPotionEffect(type);
        boolean hasEffect = effect != null;
        int level = hasEffect ? effect.getAmplifier() + 1 : 0;
        
        ItemStack item = new ItemBuilder(material)
                .name("<light_purple><bold>" + name)
                .lore("<gray>Nivel Actual: " + (hasEffect ? "<green>" + level : "<red>Desactivado"),
                      "", "<green>Click Izquierdo: <gray>+1 Nivel", "<red>Click Derecho: <gray>-1 Nivel")
                .build();

        if (hasEffect) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setEnchantmentGlintOverride(true);
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getSlot();
        boolean isLeft = e.isLeftClick();
        boolean isRight = e.isRightClick();
        boolean updated = false;

        if (!isLeft && !isRight) return;

        // Attributes
        if (slot == 10) {
            updated = modifyAttribute(Attribute.SCALE, isLeft ? 0.5 : -0.5, 0.1, 16.0);
        } else if (slot == 11) {
            updated = modifyAttribute(Attribute.MOVEMENT_SPEED, isLeft ? 0.005 : -0.005, 0.0, 3.0);
        } else if (slot == 12) {
            updated = modifyAttribute(Attribute.ATTACK_DAMAGE, isLeft ? 1.0 : -1.0, 0.0, 1000.0);
        } else if (slot == 13) {
            updated = modifyAttribute(Attribute.MAX_HEALTH, isLeft ? 100.0 : -100.0, 1.0, 5000.0);
            if (updated) {
                // Heal automatically if increasing max health
                if (isLeft) player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
                Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> BossManager.getInstance().updateBossBar(), 1L);
            }
        } else if (slot == 14) {
            updated = modifyAttribute(Attribute.ARMOR, isLeft ? 2.0 : -2.0, 0.0, 100.0);
        } else if (slot == 15) {
            updated = modifyAttribute(Attribute.GRAVITY, isLeft ? 0.005 : -0.005, -1.0, 1.0);
        } 
        
        // Potions
        else if (slot == 19) {
            modifyPotion(PotionEffectType.REGENERATION, isLeft);
            updated = true;
        } else if (slot == 20) {
            modifyPotion(PotionEffectType.RESISTANCE, isLeft);
            updated = true;
        } else if (slot == 21) {
            modifyPotion(PotionEffectType.FIRE_RESISTANCE, isLeft);
            updated = true;
        } else if (slot == 22) {
            modifyPotion(PotionEffectType.STRENGTH, isLeft);
            updated = true;
        }

        if (updated) {
            setMenuItems(); // Reload menu items to reflect changes
        }
    }

    private boolean modifyAttribute(Attribute attr, double delta, double min, double max) {
        AttributeInstance instance = player.getAttribute(attr);
        if (instance != null) {
            double newValue = instance.getBaseValue() + delta;
            if (newValue < min) newValue = min;
            if (newValue > max) newValue = max;
            instance.setBaseValue(newValue);
            return true;
        }
        return false;
    }

    private void modifyPotion(PotionEffectType type, boolean isLeft) {
        PotionEffect current = player.getPotionEffect(type);
        int currentAmplifier = current != null ? current.getAmplifier() : -1;
        
        int newAmplifier = currentAmplifier + (isLeft ? 1 : -1);
        
        // max level 10 (amplifier 9), min -1 (disabled)
        if (newAmplifier < -1) newAmplifier = -1;
        if (newAmplifier > 9) newAmplifier = 9;
        
        player.removePotionEffect(type);
        if (newAmplifier >= 0) {
            player.addPotionEffect(new PotionEffect(type, PotionEffect.INFINITE_DURATION, newAmplifier, false, false));
        }
    }
}

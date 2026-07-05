package com.panita.tezzlar3.minievents.gui;

import com.panita.tezzlar3.Tezzlar;
import com.panita.tezzlar3.core.chat.Messenger;
import com.panita.tezzlar3.core.util.SoundUtils;
import com.panita.tezzlar3.hardcore.util.HardcoreDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RouletteGUI implements Listener {

    private final Player player;
    private final Inventory inventory;
    private final Random random = new Random();
    
    private final List<ItemStack> itemsPool = new ArrayList<>();
    
    private int ticksPassed = 0;
    private int currentIndex = 0;
    
    // We pre-generate a sequence of outcomes. The final item will be at index (spinCount) in the sequence.
    private final List<ItemStack> sequence = new ArrayList<>();
    
    private BukkitRunnable spinTask;

    public RouletteGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27, Messenger.mini("<dark_red><b>Ruleta Rusa</b></dark_red>"));
        Bukkit.getPluginManager().registerEvents(this, Tezzlar.getInstance());
        
        setupPool();
        generateSequence();
        updateInventoryView(0);
    }

    private void setupPool() {
        // Win item (70% weight) -> Player's Head
        ItemStack winItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta winMeta = (SkullMeta) winItem.getItemMeta();
        winMeta.setOwningPlayer(player);
        winMeta.displayName(Messenger.mini("<green><b>Recuperar vida</b></green>"));
        winItem.setItemMeta(winMeta);
        
        // Lose item (30% weight) -> Skeleton Skull
        ItemStack loseItem = new ItemStack(Material.SKELETON_SKULL);
        ItemMeta loseMeta = loseItem.getItemMeta();
        loseMeta.displayName(Messenger.mini("<dark_red><b>Muerte instantánea</b></dark_red>"));
        loseItem.setItemMeta(loseMeta);
        
        // Populate pool (100 items for exact percentages)
        for (int i = 0; i < 70; i++) itemsPool.add(winItem);
        for (int i = 0; i < 30; i++) itemsPool.add(loseItem);
    }

    private void generateSequence() {
        // We generate 50 random items for the spin sequence
        for (int i = 0; i < 50; i++) {
            sequence.add(itemsPool.get(random.nextInt(itemsPool.size())));
        }
    }

    public void open() {
        player.openInventory(inventory);
        
        spinTask = new BukkitRunnable() {
            int speed = 1; // ticks between shifts
            int shifts = 0;
            int maxShifts = 40; // Total shifts before stopping
            
            @Override
            public void run() {
                if (player.getOpenInventory().getTopInventory() != inventory) {
                    // Player forcefully closed the inventory mid-spin! Force apply outcome and cancel
                    applyOutcome(sequence.get(shifts + 4)); // +4 is the center index
                    this.cancel();
                    HandlerList.unregisterAll(RouletteGUI.this);
                    return;
                }
                
                ticksPassed++;
                if (ticksPassed % speed == 0) {
                    shifts++;
                    SoundUtils.play(player, "ui.button.click", 0.5f, 1.5f);
                    
                    updateInventoryView(shifts);
                    
                    // Decelerate
                    if (shifts > 20) speed = 2;
                    if (shifts > 30) speed = 4;
                    if (shifts > 35) speed = 6;
                    if (shifts > 38) speed = 10;
                    
                    if (shifts >= maxShifts) {
                        // Finished
                        SoundUtils.play(player, "entity.player.levelup", 1.0f, 1.0f);
                        ItemStack finalItem = sequence.get(shifts + 4);
                        
                        Bukkit.getScheduler().runTaskLater(Tezzlar.getInstance(), () -> {
                            player.closeInventory();
                            applyOutcome(finalItem);
                            HandlerList.unregisterAll(RouletteGUI.this);
                        }, 40L); // wait 2 seconds before closing
                        
                        this.cancel();
                    }
                }
            }
        };
        spinTask.runTaskTimer(Tezzlar.getInstance(), 0L, 1L);
    }

    private void updateInventoryView(int offset) {
        inventory.clear();
        
        // Fill top and bottom with gray panes
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Messenger.mini(" "));
        border.setItemMeta(borderMeta);
        
        for (int i = 0; i < 9; i++) {
            if (i == 4) {
                // The pointer
                ItemStack pointer = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta pMeta = pointer.getItemMeta();
                pMeta.displayName(Messenger.mini(" "));
                pointer.setItemMeta(pMeta);
                inventory.setItem(i, pointer);
                inventory.setItem(i + 18, pointer);
            } else {
                inventory.setItem(i, border);
                inventory.setItem(i + 18, border);
            }
        }
        
        // The sliding row (9 slots in the middle, indexes 9 to 17)
        for (int i = 0; i < 9; i++) {
            int seqIndex = offset + i;
            if (seqIndex < sequence.size()) {
                inventory.setItem(9 + i, sequence.get(seqIndex));
            }
        }
    }

    private void applyOutcome(ItemStack item) {
        if (item.getType() == Material.PLAYER_HEAD) {
            // Win
            int currentLives = HardcoreDataManager.getLives(player.getUniqueId(), player.getName());
            HardcoreDataManager.setLives(player.getUniqueId(), player.getName(), currentLives + 1);
            Messenger.prefixedSend(player, "<green>¡LA RULETA SE DETUVO A TU FAVOR! Has ganado 1 vida extra.</green>");
            SoundUtils.play(player, "entity.player.levelup", 1.0f, 0.8f);
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        } else {
            // Lose
            Messenger.prefixedSend(player, "<dark_red>¡LA RULETA HA HABLADO! Has perdido la apuesta.</dark_red>");
            SoundUtils.play(player, "entity.wither.death", 1.0f, 0.5f);
            player.setHealth(0.0); // Kill them instantly!
        }
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().equals(inventory)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        // If they close it naturally after it finished, we unregister
        if (event.getInventory().equals(inventory) && (spinTask == null || spinTask.isCancelled())) {
            HandlerList.unregisterAll(this);
        }
    }
}

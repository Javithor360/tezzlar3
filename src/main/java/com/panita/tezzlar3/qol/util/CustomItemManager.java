package com.panita.tezzlar3.qol.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.panita.tezzlar3.Tezzlar;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

public class CustomItemManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static JsonObject items = new JsonObject();
    private static File file;

    /** Result of attempting to save an item */
    public enum ItemResult {
        SUCCESS,
        DUPLICATE_NAME,
        NOT_FOUND,
        ERROR
    }

    /** Initializes the CustomItemManager with the plugin's data folder */
    public static void init(File dataFolder) {
        file = new File(dataFolder, "customitems.json");
        loadItems();
    }

    /** Loads items from the JSON file into the items JsonObject */
    private static void loadItems() {
        try {
            if (!file.exists()) {
                Tezzlar.getInstance().saveResource("customitems.json", false);
            }
            items = gson.fromJson(new FileReader(file), JsonObject.class);
            if (items == null) items = new JsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            items = new JsonObject();
        }
    }

    /** Saves the current items JsonObject to the file */
    private static void saveItems() {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(items, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves an ItemStack with a custom name.
     * The item is serialized to JSON and stored in a file.
     * A custom identifier is added to the item's metadata to ensure uniqueness.
     *
     * @param name The unique name to save the item under.
     * @param item The ItemStack to save.
     * @return true if the item was saved successfully, false if an item with the same name already exists or an error occurred.
     */
    public static ItemResult saveItem(String name, ItemStack item) {
        if (items.has(name)) return ItemResult.DUPLICATE_NAME;

        try {
            ItemStack targetItem = addCustomMetadata(item, name);

            // Serialize the complete item including meta
            Map<String, Object> serialized = targetItem.serialize();
            String json = gson.toJson(serialized);
            items.addProperty(name, json);

            // Save to file
            saveItems();
            return ItemResult.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ItemResult.ERROR;
        }
    }

    /**
     * Retrieves a saved ItemStack by its custom name.
     * @param name The name of the saved item.
     * @return The ItemStack if found, or null if not found or an error occurred.
     */
    public static ItemStack getItem(String name) {
        if (!items.has(name)) return null;
        try {
            String json = items.get(name).getAsString();
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> data = gson.fromJson(json, type);
            return ItemStack.deserialize(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Removes a saved item by its custom name.
     * @param name The name of the item to remove.
     * @return ItemResult indicating success, not found, or error.
     */
    public static ItemResult removeItem(String name) {
        if (!items.has(name)) return ItemResult.NOT_FOUND;
        try {
            items.remove(name);
            saveItems();
            return ItemResult.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ItemResult.ERROR;
        }
    }

    /** Returns a set of all saved item names */
    public static Set<String> getAllItemNames() {
        return items.keySet();
    }

    /**
     * Adds the plugin's custom metadata to the given ItemStack.
     * This does NOT save the item to the JSON file.
     *
     * @param item The ItemStack to tag.
     * @param name The identifier to assign.
     * @return A clone of the original ItemStack with the custom metadata applied.
     */
    public static ItemStack addCustomMetadata(ItemStack item, String name) {
        if (item == null || item.getType().isAir()) return item;

        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) return clone;

        NamespacedKey key = new NamespacedKey(Tezzlar.getInstance(), "custom_item_id");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, name);
        clone.setItemMeta(meta);

        return clone;
    }

    /**
     * Checks if an ItemStack is a custom saved item by looking for the custom identifier in its metadata.
     * @param item The ItemStack to check.
     * @return true if the item is a custom saved item, false otherwise.
     */
    public static boolean isCustomItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(Tezzlar.getInstance(), "custom_item_id");
        return meta.getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    /**
     * Checks if an ItemStack is a specific custom saved item by comparing the custom identifier in its metadata.
     * @param item The ItemStack to check.
     * @param name The name of the custom item to compare against.
     * @return true if the item matches the specified custom item, false otherwise.
     */
    public static boolean isCustomItem(ItemStack item, String name) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(Tezzlar.getInstance(), "custom_item_id");
        String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return name.equals(id);
    }
}

package com.dumbdogdiner.stickycommands.utils;

import java.io.File;
import java.io.IOException;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickyapi.common.configuration.InvalidConfigurationException;
import com.dumbdogdiner.stickyapi.common.configuration.file.FileConfiguration;
import com.dumbdogdiner.stickyapi.common.configuration.file.YamlConfiguration;
import com.dumbdogdiner.stickyapi.common.util.StringUtil;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.persistence.PersistentDataType;

import lombok.Getter;

public class Item {
    Main self = Main.getInstance();
    static File configFile;
    static FileConfiguration config;
    private static String[] modifierPool = {
        "white", "orange", "magenta", "lightblue", "yellow", "lime", "pink", "gray", "lightgray", "cyan", "purple", "blue", "brown", "green", "red", "black", "oak", "spruce", "birch", "jungle", "acacia", "darkoak"
    };
    private static String[] durItemPool = {
        "helmet", "tunic", "chestplate", "leggings", "boots", "axe", "shovel", "sword", "hoe"
    };

    @Getter
    private Material type;
    @Getter
    private int amount;
    @Getter
    private MaterialData data;
    @Getter
    private ItemMeta itemMeta;
    @Getter
    private String name;
    @Getter
    private ItemStack asItemStack;
    public Item() {
        final var worthFile = self.getConfig().getString("worth-file", "worth.yml");
        configFile = new File(self.getDataFolder(), worthFile);
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            self.saveResource(worthFile, false);
        }
        
        FileConfiguration fc = new YamlConfiguration();
        try {
            fc.load(configFile);
            config = fc;
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public Item(ItemStack is) {
        this.type = is.getType();
        this.amount = is.getAmount();
        this.data = is.getData();
        this.itemMeta = is.getItemMeta();
        this.name = StringUtil.capitaliseSentence(is.getType().toString().replace("_", " "));
        this.asItemStack = is;
    }

    /**
     * Get the worth of an {@link org.bukkit.inventory.ItemStack}
     * 
     * @param is The ItemStack
     * @return The worth of the ItemStack
     */
    public double getWorth(final ItemStack is) {
        final var name = is.getType().toString().replace("_", "").toLowerCase();
        final var worth = config.getDouble(name, 0.0);
        if (worth == 0) {
            for (final var s : modifierPool) {
                if (name.startsWith(s)) {
                    final var it = name.replace(s, "");
                    return config.getDouble(it);
                }
            }
        }
        if (!isSellable(is))
            return 0.0;

        return worth;
    }

    /**
     * Check if an item stack has "notsellable" in nbt data
     * @param is The ItemStack
     * @return True if the item can be sold
     */
    public boolean isSellable(final ItemStack is) {
        final var meta = is.getItemMeta();
        final var dataStore = meta.getPersistentDataContainer();
        return !dataStore.has(new NamespacedKey(Main.getInstance(), "notsellable"), PersistentDataType.STRING);
    }

    /**
     * Get the worth of an Item
     * 
     * @return The worth of the ItemStack
     */
    public double getWorth() {
        final var name = this.getType().toString().replace("_", "").toLowerCase();
        final var worth = config.getDouble(name, 0.0);
        if (worth == 0.0) {
            for (final String s : modifierPool) {
                if (name.startsWith(s)) {
                    final String it = name.replace(s, "");
                    return config.getDouble(it, 0.0);
                }
            }
        }
        if (!isSellable())
            return 0.0;

        return worth;
    }

    /**
     * Check if an item has "notsellable" in nbt data
     * @return True if the item can be sold
     */
    public boolean isSellable() {
        final var meta = this.getItemMeta();
        final var dataStore = meta.getPersistentDataContainer();
        return !dataStore.has(new NamespacedKey(Main.getInstance(), "notsellable"), PersistentDataType.STRING);
    }

    public boolean hasDurability() {
        for(String s : durItemPool) {
            if(getName().toLowerCase().endsWith(s)) {
                return true;
            }
        }
        return false;
    } 
}
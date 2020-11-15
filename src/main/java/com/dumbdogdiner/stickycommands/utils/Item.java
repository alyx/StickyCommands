package com.dumbdogdiner.stickycommands.utils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.StickyCommands;
import com.dumbdogdiner.stickyapi.common.configuration.InvalidConfigurationException;
import com.dumbdogdiner.stickyapi.common.configuration.file.FileConfiguration;
import com.dumbdogdiner.stickyapi.common.configuration.file.YamlConfiguration;
import com.dumbdogdiner.stickyapi.common.util.Debugger;
import com.dumbdogdiner.stickyapi.common.util.StringUtil;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.persistence.PersistentDataType;

import lombok.Getter;

public class Item {
    StickyCommands self = StickyCommands.getInstance();
    @Getter
    static DecimalFormat decimalFormat = new DecimalFormat("0.00"); // We don't want something like 25.3333333333, instead we want 25.33
    static File configFile;
    static FileConfiguration config;

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
        this.name = StringUtil.capitaliseSentence(is.getType().toString());
        this.asItemStack = is;
    }

    /**
     * Get the worth of an {@link org.bukkit.inventory.ItemStack}
     * 
     * @param is The ItemStack
     * @return The worth of the ItemStack
     */
    public double getWorth(final ItemStack is) {
        final var name = is.getType().toString();
        final var worth = config.getDouble(name, 0.0);
        if (!isSellable(is))
            return 0.0;

        return Double.valueOf(decimalFormat.format(worth));
    }

    /**
     * Check if an item stack has "notsellable" in nbt data
     * @param is The ItemStack
     * @return True if the item can be sold
     */
    public boolean isSellable(final ItemStack is) {
        final var meta = is.getItemMeta();
        final var dataStore = meta.getPersistentDataContainer();
        return !dataStore.has(new NamespacedKey(StickyCommands.getInstance(), "notsellable"), PersistentDataType.STRING);
    }

    /**
     * Get the worth of an Item
     * 
     * @return The worth of the ItemStack
     */
    public double getWorth() {
        final var name = this.getType().toString();
        final var worth = config.getDouble(name, 0.0);
        if (!sellable())
            return 0.0;

        return worth;
    }

    /**
     * Check if an item has "notsellable" in nbt data
     * @return True if the item can be sold
     */
    public boolean sellable() {
        final var meta = this.getItemMeta();
        final var dataStore = meta.getPersistentDataContainer();
        return !dataStore.has(new NamespacedKey(StickyCommands.getInstance(), "notsellable"), PersistentDataType.STRING);
    }

    public boolean hasDurability() {
        return this.asItemStack.getDurability() > 0;
    } 

    public void sell(Player player, Boolean sellInventory, TreeMap<String, String> variables, Integer amount) {
        Debugger debug = new Debugger(getClass());
        debug.print("selling item " + getName());
        var database = StickyCommands.getInstance().getDatabase();
        debug.print("database variable declared");
        if (!sellInventory) {
            debug.print("Not selling inventory...");
            StickyCommands.getInstance().getEconomy().depositPlayer(player, getWorth() * amount);
            debug.print("Deposited " + getWorth() * amount + " in " + player.getName() + "'s account");
            player.getInventory().getItemInMainHand().setAmount(0);
            debug.print("removed items");
        } else {
            StickyCommands.getInstance().getEconomy().depositPlayer(player, getWorth() * amount);
            debug.print("Deposited " + getWorth() * amount + " in " + player.getName() + "'s account");
            debug.print("removing items...");
            consumeItem(player, amount, getType());
        }
        database.logSell(player.getUniqueId(), player.getName(), this, amount, Double.valueOf(decimalFormat.format(getWorth() * amount)));
    }

        
    public boolean consumeItem(Player player, int count, Material mat) {
        Debugger debug = new Debugger(getClass());
        ItemStack[] item = player.getInventory().getContents();
        debug.print("Removing " + count + "items of type " + mat.toString() + " from " + player.getName() +"'s inventory'");
        for (ItemStack s : item) {
            if (s != null) {
                if (s.getType() == mat) {
                    debug.print("found slot with item, removing");
                    s.setAmount(0);
                }
            }
        }

        player.updateInventory();
        return true;
    }
}
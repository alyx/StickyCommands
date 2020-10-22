package com.dumbdogdiner.stickycommands.utils;

import com.dumbdogdiner.stickyapi.common.configuration.InvalidConfigurationException;
import com.dumbdogdiner.stickyapi.common.configuration.file.FileConfiguration;
import com.dumbdogdiner.stickyapi.common.configuration.file.YamlConfiguration;
import com.dumbdogdiner.stickyapi.common.util.Debugger;
import com.dumbdogdiner.stickyapi.common.util.StringUtil;
import com.dumbdogdiner.stickycommands.Main;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.TreeMap;

public class Item {
    Main self = Main.getInstance();
    @Getter
    static DecimalFormat decimalFormat = new DecimalFormat("0.00"); // We don't want something like 25.3333333333, instead we want 25.33. This also hides floating point precision things from the user.
    static File configFile;
    static FileConfiguration config;
    private static final String[] modifierPool = {
        "white", "orange", "magenta", "lightblue", "yellow", "lime", "pink", "gray", "lightgray", "cyan", "purple", "blue", "brown", "green", "red", "black", "oak", "spruce", "birch", "jungle", "acacia", "darkoak"
    };

    @Getter
    private Material type;
    @Getter
    private int amount;
    /*@Getter
    private MaterialData data;*/
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
        //this.data = is.getData();
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
        final String name = is.getType().toString().replace("_", "").toLowerCase();
        final double worth = config.getDouble(name, 0.0);
        if (worth == 0) {
            for (final var s : modifierPool) {
                if (name.startsWith(s)) {
                    final String it = name.replace(s, "");
                    return Double.parseDouble(decimalFormat.format(config.getDouble(it)));
                }
            }
        }
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
        return !dataStore.has(new NamespacedKey(Main.getInstance(), "notsellable"), PersistentDataType.STRING);
    }

    /**
     * Get the worth of an Item
     * 
     * @return The worth of the ItemStack
     */
    public double getWorth() {
        if (!sellable()) return 0.0;
        final var name = this.getType().toString().replace("_", "").toLowerCase();
        double worth = config.getDouble(name, 0.0);
        if (isDamageable()) {
            double percentage;
            double maxDur = getMaxDurability();
            double currDur = getDurability();
            percentage = Math.round((currDur / maxDur) * 100.00) / 100.0;

            if (percentage > 0.4) {
                worth = Math.round((worth * percentage) * 100.00) / 100.00;
            } else {
                worth = 0;
            }


        } else if (worth == 0.0) {
            for (final String s : modifierPool) {
                if (name.startsWith(s)) {
                    final String it = name.replace(s, "");
                    worth = config.getDouble(it, 0.0);
                    break;
                }
            }
        }

        return worth;
    }

    /**
     * Check if an item has "notsellable" in nbt data
     * @return True if the item can be sold
     */
    public boolean sellable() {
        final var meta = this.getItemMeta();
        final var dataStore = meta.getPersistentDataContainer();
        return !dataStore.has(new NamespacedKey(Main.getInstance(), "notsellable"), PersistentDataType.STRING);
    }

    public boolean isDamageable() {
        return itemMeta instanceof Damageable;
    }

    public int getDurability() {
        if(! isDamageable())
            return -1;

        return ((Damageable) itemMeta).getDamage();
    }

    /**
     * Sets durability of item if it is damageable, otherwise does nothing
     * @param damage The damage to set the item to
     */
    public void setDurability(int damage){
        if(isDamageable()){
            ((Damageable) itemMeta).setDamage(damage);
            asItemStack.setItemMeta(itemMeta);
        }
    }

    /**
     * Provides the maximum durability of an item
     * @return the maximum durability of an item
     */
    public int getMaxDurability() {
        return isDamageable() ? type.getMaxDurability() : -1;
    }


    //TODO: Allow selling an arbitrary amount of items in hand
    public void sell(Player player, Boolean sellInventory, TreeMap<String, String> variables, Integer amount) {
        Debugger debug = new Debugger(getClass());
        debug.print("selling item " + getName());
        var database = Main.getInstance().getDatabase();
        debug.print("database variable declared");
        if (!sellInventory) {
            debug.print("Not selling inventory...");
            Main.getInstance().getEconomy().depositPlayer(player, getWorth() * amount);
            debug.print("Depositted " + getWorth() * amount + " in " + player.getName() + "'s account");
            player.getInventory().getItemInMainHand().setAmount(0);
            debug.print("removed items");
        } else {
            Main.getInstance().getEconomy().depositPlayer(player, getWorth() * amount);
            debug.print("Depositted " + getWorth() * amount + " in " + player.getName() + "'s account");
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
                if (s.getType().equals(mat)) {
                    debug.print("found slot with item, removing");
                    s.setAmount(0);
                }
            }
        }

        player.updateInventory();
        return true;
    }
}
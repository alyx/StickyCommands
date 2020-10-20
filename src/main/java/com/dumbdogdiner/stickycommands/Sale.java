package com.dumbdogdiner.stickycommands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import com.dumbdogdiner.stickycommands.utils.Database;
import com.dumbdogdiner.stickycommands.utils.Item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;

/**
 * An object to represent a sale, this is mostly for convenience later...
 */
public class Sale {

    @Getter
    Integer orderId;

    @Getter
    UUID uniqueId;

    @Getter
    String username;

    @Getter
    Item item;

    @Getter
    Integer amount;

    @Getter
    Double price;

    @Getter
    Double newBalance;

    @Getter
    Timestamp date;

    public Sale(ResultSet result) {
        try {
            if (result == null)
                throw new NullPointerException("ResultSet is null, did the database query execute successfully?");
            UUID uuid = UUID.fromString(result.getString("uuid"));
            // We want the username of the player... So we need to query the users table.
            this.orderId = result.getInt("id");
            this.uniqueId = uuid;
            this.username = result.getString("player_name");
            this.item = new Item(new ItemStack(Material.getMaterial(result.getString("item")), result.getInt("amount")));
            this.amount = result.getInt("amount");
            this.price = Double.valueOf(Item.getDecimalFormat().format(result.getDouble("item_worth")));
            this.newBalance = Double.valueOf(Item.getDecimalFormat().format(result.getDouble("new_balance")));
            this.date = result.getTimestamp("time_sold");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public Double getOldBalance() {
        return Double.valueOf(Item.getDecimalFormat().format(this.newBalance - this.price));
    }
    
}

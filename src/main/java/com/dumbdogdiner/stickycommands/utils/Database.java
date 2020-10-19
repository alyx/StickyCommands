package com.dumbdogdiner.stickycommands.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.dumbdogdiner.stickyapi.common.util.Debugger;
import com.dumbdogdiner.stickyapi.common.util.TimeUtil;
import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.SpeedType;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import lombok.Getter;
import lombok.Setter;

/**
 * Utility class for interfacing with the database.
 */
public class Database {
    private Connection connection = null;

    private FileConfiguration config = Main.getInstance().getConfig();

    @Getter
    @Setter
    private String host = config.getString("database.host", "localhost");
    @Getter
    @Setter
    private Integer port = config.getInt("database.port", 3306);
    @Getter
    @Setter
    private String name = config.getString("database.name", "lolbans");
    @Getter
    @Setter
    private Integer maxReconnects = config.getInt("database.max-reconnects", 5);
    @Getter
    @Setter
    private Boolean useSSL = config.getBoolean("database.use-ssl", false);

    @Getter
    @Setter
    private String username = config.getString("database.username", "root");
    @Getter
    @Setter
    private String password = config.getString("database.password", "password");

    @Getter
    @Setter
    private String tablePrefix = config.getString("database.table-prefix", "stickycommands_");

    // TODO Add configuration check
    public Database() {
        openConnection();
    }

    /**
     * Open a new connection to the database.
     */
    private Connection openConnection() {
        try {
            if (this.connection != null)
                this.connection.close();

            this.connection = DriverManager.getConnection(String.format(
                    "jdbc:mysql://%s:%s/%s?autoReconnect=true&failOverReadOnly=false&maxReconnects=%d&useSSL=%s",
                    this.host, this.port, this.name, this.maxReconnects, this.useSSL), this.username, this.password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return this.connection;
    }

    /**
     * Get a reference to this instance's database connection. Will attempt to
     * create a new connection if the existing one doesn't exist, or has been
     * closed.
     */
    public Connection getConnection() {
        try {
            if (this.connection != null && !this.connection.isClosed())
                return this.connection;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return openConnection();
    }

    public void terminate() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create all missing tables.
     */
    public boolean createMissingTables() {
        if (this.connection == null)
            return false;

        // Ensure Our tables are created.
        try {
            this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + withPrefix("users") + "("
                    + "uuid VARCHAR(36) NOT NULL PRIMARY KEY," + "player_name VARCHAR(17),"
                    + "ip_address VARCHAR(48) NOT NULL," + "first_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "last_server TEXT NOT NULL," + "times_connected INT NULL," + "walk_speed FLOAT(2,1) DEFAULT 0.2,"
                    + "fly_speed FLOAT(2,1) DEFAULT 0.1," + "is_online BOOLEAN DEFAULT FALSE" + ")").execute();

            this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + withPrefix("sales") + "("
                    + "id INT AUTO_INCREMENT NOT NULL PRIMARY KEY," + "uuid VARCHAR(36) NOT NULL,"
                    + "amount INT NOT NULL," + "item VARCHAR(256) NOT NULL," + "item_worth DOUBLE NOT NULL,"
                    + "new_balance DOUBLE NOT NULL," + "time_sold TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" + ")")
                    .execute();

        } catch (SQLException e) {
            e.printStackTrace();
            Main.getInstance().getLogger()
                    .severe("Cannot create database tables, please ensure your SQL user has the correct permissions.");
            return false;

        }
        return true;
    }

    /**
     * Format a table name to include the table prefix.
     */
    private String withPrefix(String tableName) {
        return this.tablePrefix + tableName;
    }

    /**
     * Set a player's speed
     * 
     * @param uuid  the UUID of the player
     * @param speed the speed to set
     * @param type  the speed type (0 = walk, 1 = fly)
     * @return
     */
    public Boolean setSpeed(UUID uuid, Float speed, int type) {
        FutureTask<Boolean> t = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    PreparedStatement updateSpeed = connection
                            .prepareStatement("UPDATE " + withPrefix("users") + " SET ? = ? WHERE uuid = ?");
                    switch (type) {
                        case 0:
                            updateSpeed.setString(1, "walk_speed");
                        case 1:
                            updateSpeed.setString(1, "fly_speed");
                        default:
                            updateSpeed.setString(1, "walk_speed");
                    }
                    updateSpeed.setFloat(2, speed);
                    updateSpeed.setString(3, uuid.toString());
                    updateSpeed.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        });
        Main.getInstance().getPool().execute(t);
        return true;
    }

    /**
     * Log an item sale
     * @param uuid The UUID of the user selling the item
     * @param item The item being sold
     * @param amount The amount of the item being sold
     * @param worth The worth of the item being sold
     * @return {@link java.lang.Boolean}
     */
    public Boolean logSell(UUID uuid, Item item, Integer amount, Double worth) {
        Debugger debug = new Debugger(getClass());
        debug.print("Logging sale for " + uuid.toString());
        debug.print(item.getName());
        debug.print(amount);
        debug.print(worth);
        FutureTask<Boolean> t = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    PreparedStatement updateSpeed = connection.prepareStatement("INSERT INTO " + withPrefix("sales") + " (uuid, amount, item, item_worth, new_balance) VALUES (?, ?, ?, ?, ?)");
                    updateSpeed.setString(1, uuid.toString());
                    updateSpeed.setInt(2, amount);
                    updateSpeed.setString(3, item.getType().toString());
                    updateSpeed.setDouble(4, worth);
                    updateSpeed.setDouble(5, Double.valueOf(Item.getDecimalFormat().format(Main.getInstance().getEconomy().getBalance(Bukkit.getOfflinePlayer(uuid)))));
                    updateSpeed.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        });
        Main.getInstance().getPool().execute(t);
        return true;
    }


}
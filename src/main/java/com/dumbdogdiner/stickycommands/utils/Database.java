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
                    + "last_login TIMESTAMP,"
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
                    var killMe = (type == 1 ? "fly_speed" : "walk_speed");
                    PreparedStatement updateSpeed = connection
                            .prepareStatement("UPDATE " + withPrefix("users") + " SET " + killMe + " = ? WHERE uuid = ?");
                    updateSpeed.setFloat(1, speed);
                    updateSpeed.setString(2, uuid.toString());
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
     * Get the speed for a user of a certain speed type from the database
     * @param uuid The UUID of the player
     * @param type The speed type
     * @return {@link java.lang.Float}
     */
    public Float getSpeed(UUID uuid, SpeedType type) {
        try {
            var speed = (type == SpeedType.FLY ? "fly_speed" : "walk_speed");
            PreparedStatement updateSpeed = connection.prepareStatement("SELECT " + speed + " FROM " + withPrefix("users") + " WHERE uuid = ?");
            updateSpeed.setString(1, uuid.toString());
            updateSpeed.executeQuery();
            return updateSpeed.getResultSet().getFloat(speed);
        } catch (SQLException e) {
            e.printStackTrace();
            return (type == SpeedType.FLY ? 1.0F : 1.1F);
        }
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

     /**
     * Insert a user into the database.
     * 
     * @param uuid       UUID of the minecraft user
     * @param playerName Name of the minecraft player
     * @param ipAddress  IP address of the minecraft player
     * @param firstLogin The first time they logged in (as a timestamp)
     * @param lastLogin  The last time they logged in (as a timestamp)
     * @param isOnline   Is the user online
     * @return True if the user was created successfully
     */
    public Boolean insertUser(String uuid, String playerName, String ipAddress, Timestamp firstLogin, Timestamp lastLogin, Boolean isOnline) {
        FutureTask<Boolean> t = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    // Make sure we're not duping data, if they already exist go ahead and update
                    // them
                    // This happens because we insert every time they join for the first time, but
                    // if the playerdata is removed on the world
                    // or the spigot plugin is setup in multiple servers using the same database, it
                    // would add them a second time
                    // lets not do that....
                    PreparedStatement checkUser = connection.prepareStatement("SELECT uuid FROM " + withPrefix("users"));
                    ResultSet results = checkUser.executeQuery();
                    if (results.next() && !results.wasNull()) {
                        updateUser(uuid, playerName, ipAddress, lastLogin, true, isOnline);
                        return true;
                    }
                    checkUser.close();

                    // Preapre a statement
                    int i = 1;
                    PreparedStatement insertUser = connection.prepareStatement(String.format(
                            "INSERT INTO " + withPrefix("users") + " (UUID, player_name, ip_address, first_login, last_login, last_server, times_connected, is_online) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"));
                    insertUser.setString(i++, uuid);
                    insertUser.setString(i++, playerName);
                    insertUser.setString(i++, ipAddress);
                    insertUser.setTimestamp(i++, firstLogin);
                    insertUser.setTimestamp(i++, lastLogin);
                    insertUser.setString(i++, Main.getInstance().getConfig().getString("server", "#"));
                    insertUser.setInt(i++, 1);
                    insertUser.setBoolean(i++, isOnline);
                    insertUser.executeUpdate();
                    insertUser.close();
                } catch (Throwable e) {
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
     * Update a user record
     * 
     * @param uuid       Users current UUID
     * @param playerName Users current player name
     * @param ipAddress  Users current IP address
     * @param lastLogin  The timestamp of the last time a user logged in
     * @param isOnline   Is the user online
     * @param isJoining  Will update the times connected if true, other wise false
     * @return True if the update was successful.
     */
    public Boolean updateUser(String uuid, String playerName, String ipAddress, Timestamp lastLogin, Boolean isOnline, Boolean isJoining)
    // (Timestamp last_login, String player_name, String ip_address, String UUID)
    {
        FutureTask<Boolean> t = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    // This is a fail-safe just incase the table was dropped or the player joined
                    // the server BEFORE the plugin was added...
                    // This will ensure they get added to the database no matter what.
                    PreparedStatement checkUser = connection
                            .prepareStatement(String.format("SELECT uuid FROM " + withPrefix("users")));
                    ResultSet results = checkUser.executeQuery();
                    if (!results.next()) {
                        Timestamp first_login = TimeUtil.now();
                        insertUser(uuid, playerName, ipAddress, first_login, lastLogin, true);
                        return true;
                    }
                    
                    PreparedStatement gtc = connection
                    .prepareStatement(String.format("SELECT times_connected FROM " + withPrefix("users") + " WHERE uuid = ?"));
                    gtc.setString(1, uuid);
                    
                    ResultSet gtc2 = gtc.executeQuery();
                    int tc = 1;
                    if (gtc2.next()) {
                        if (!gtc2.wasNull()) {
                            tc = gtc2.getInt("times_connected");
                        } else {
                            tc = 0;
                        }
                    }
                    checkUser.close();
                    gtc.close();
                    if (isJoining) {
                        // Preapre a statement
                        int i = 1;
                        PreparedStatement updateUser = connection.prepareStatement(
                                "UPDATE " + withPrefix("users") + " SET last_login = ?, player_name = ?, ip_address = ?, last_server = ?, times_connected = ?, is_online = ? WHERE uuid = ?");
                        updateUser.setTimestamp(i++, lastLogin);
                        updateUser.setString(i++, playerName);
                        updateUser.setString(i++, ipAddress);
                        updateUser.setString(i++, Main.getInstance().getConfig().getString("server", "#"));
                        updateUser.setInt(i++, isJoining ? ++tc : tc);
                        updateUser.setBoolean(i++, isOnline);
                        updateUser.setString(i++, uuid);
                        updateUser.executeUpdate();
                        updateUser.close();
                        return true;
                    }

                    int i = 1;
                    PreparedStatement updateUserOffline = connection.prepareStatement(
                            "UPDATE " + withPrefix("users") + " SET last_login = ?, player_name = ?, ip_address = ?, is_online = ? WHERE uuid = ?");
                    updateUserOffline.setTimestamp(i++, lastLogin);
                    updateUserOffline.setString(i++, playerName);
                    updateUserOffline.setString(i++, ipAddress);
                    updateUserOffline.setBoolean(i++, isOnline);
                    updateUserOffline.setString(i++, uuid);
                    updateUserOffline.executeUpdate();
                    updateUserOffline.close();
                } catch (Throwable e) {
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
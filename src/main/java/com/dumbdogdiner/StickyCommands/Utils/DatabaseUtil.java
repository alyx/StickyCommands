package com.dumbdogdiner.StickyCommands.Utils;

import java.sql.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import com.dumbdogdiner.StickyCommands.Main;

public class DatabaseUtil {

    public static Connection connection;
    private static Main self = Main.getPlugin(Main.class);

    /**
     * Actually open the conenction to the database, this should not be used outside
     * this class.
     * 
     * @throws SQLException SQL exception if the connection fails
     */
    public static void OpenConnection() throws SQLException {
        if (connection != null && !connection.isClosed())
            return;

        synchronized (self) {
            if (connection != null && !connection.isClosed())
                return;

            connection = DriverManager.getConnection(String.format(
                    "jdbc:mysql://%s:%s/%s?autoReconnect=true&failOverReadOnly=false&maxReconnects=%d&useSSL=%s",
                    Configuration.dbhost, Configuration.dbport, Configuration.dbname, Configuration.MaxReconnects,
                    Configuration.useSSL), Configuration.dbusername, Configuration.dbpassword);
        }
    }

    /**
     * Terminate the connection to the database.
     */
    public static void Terminate() {
        // Close the database connection (if open)
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initialize the database tables and connection. This also starts the
     * synchronization thread for the database.
     * 
     * @return True if the tables were created successfully and the connection
     *         completed successfully. Otherwise false
     */
    public static boolean InitializeDatabase() {
        try {
            DatabaseUtil.OpenConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            self.getLogger().severe(
                    "Cannot connect to database, ensure your database is setup correctly and restart the server.");
            // Just exit and let the user figure it out.
            return false;
        }

        // Ensure Our tables are created.
        try {
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS Users " + "(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                            + "UUID VARCHAR(36) NOT NULL," + "PlayerName VARCHAR(17),"
                            + "IPAddress VARCHAR(48) NOT NULL," + "FirstLogin TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                            + "LastLogin TIMESTAMP DEFAULT CURRENT_TIMESTAMP," + "LastServer TEXT NOT NULL,"
                            + "TimesConnected INT NULL," + "WalkSpeed FLOAT(2,1) DEFAULT 0.2,"
                            + "FlySpeed FLOAT(2,1) DEFAULT 0.1," + "IsOnline BOOLEAN DEFAULT FALSE" + ")")
                    .execute();
        } catch (SQLException e) {
            e.printStackTrace();
            self.getLogger()
                    .severe("Cannot create database tables, please ensure your SQL user has the correct permissions.");
            return false;
        }
        return true;
    }

    /**
     * Insert a user into the database.
     * 
     * @param UUID       UUID of the minecraft user
     * @param PlayerName Name of the minecraft player
     * @param IPAddress  IP address of the minecraft player
     * @param FirstLogin The first time they logged in (as a timestamp)
     * @param LastLogin  The last time they logged in (as a timestamp)
     * @param IsOnline   Is the user online
     * @return True if the user was created successfully
     */
    public static Future<Boolean> InsertUser(String UUID, String PlayerName, String IPAddress, Timestamp FirstLogin,
            Timestamp LastLogin, Boolean IsOnline) {
        FutureTask<Boolean> t = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                // This is where you should do your database interaction
                try {
                    // Make sure we're not duping data, if they already exist go ahead and update
                    // them
                    // This happens because we insert every time they join for the first time, but
                    // if the playerdata is removed on the world
                    // or the spigot plugin is setup in multiple servers using the same database, it
                    // would add them a second time
                    // lets not do that....
                    int j = 1;
                    PreparedStatement CheckUser = connection.prepareStatement("SELECT id FROM Users WHERE UUID = ?");
                    CheckUser.setString(j++, UUID);
                    ResultSet results = CheckUser.executeQuery();
                    if (results.next() && !results.wasNull()) {
                        DebugUtil.sendDebug("User is already in database, but has not joined... Updating user...",
                                this.getClass(), DebugUtil.getLineNumber());
                        UpdateUser(UUID, PlayerName, IPAddress, LastLogin, true, true);
                        return true;
                    }
                    CheckUser.close();

                    // Preapre a statement
                    DebugUtil.sendDebug("Preparing a statement...", this.getClass(), DebugUtil.getLineNumber());
                    int i = 1;
                    PreparedStatement InsertUser = connection.prepareStatement(String.format(
                            "INSERT INTO Users (UUID, PlayerName, IPAddress, FirstLogin, LastLogin, LastServer, TimesConnected, IsOnline) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"));
                    InsertUser.setString(i++, UUID);
                    InsertUser.setString(i++, PlayerName);
                    InsertUser.setString(i++, IPAddress);
                    InsertUser.setTimestamp(i++, FirstLogin);
                    InsertUser.setTimestamp(i++, LastLogin);
                    if (User.getServer(PlayerName).get() == null)
                        InsertUser.setString(i++, "error");
                    else
                        InsertUser.setString(i++, User.getServer(PlayerName).get());
                    InsertUser.setInt(i++, 1);
                    InsertUser.setBoolean(i++, IsOnline);
                    DebugUtil.sendDebug("Attempting to execute update...", this.getClass(), DebugUtil.getLineNumber());
                    InsertUser.executeUpdate();
                    DebugUtil.sendDebug("Update was successful!", this.getClass(), DebugUtil.getLineNumber());
                    InsertUser.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        });

        Main.pool.execute(t);

        return (Future<Boolean>) t;
    }

    /**
     * Update a user record
     * 
     * @param UUID       Users current UUID
     * @param PlayerName Users current player name
     * @param IPAddress  Users current IP address
     * @param LastLogin  The timestamp of the last time a user logged in
     * @param IsOnline   Is the user online
     * @param IsJoining  Will update the times connected if true, other wise false
     * @return True if the update was successful.
     */
    public static Future<Boolean> UpdateUser(String UUID, String PlayerName, String IPAddress, Timestamp LastLogin,
            Boolean IsOnline, Boolean IsJoining)
    // (Timestamp LastLogin, String PlayerName, String IPAddress, String UUID)
    {
        FutureTask<Boolean> t = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                // This is where you should do your database interaction
                try {
                    int j = 1;
                    // This is a fail-safe just incase the table was dropped or the player joined
                    // the server BEFORE the plugin was added...
                    // This will ensure they get added to the database no matter what.
                    PreparedStatement CheckUser = connection
                            .prepareStatement(String.format("SELECT id FROM Users WHERE UUID = ?"));
                    CheckUser.setString(j++, UUID);
                    ResultSet results = CheckUser.executeQuery();
                    if (!results.next()) {
                        DebugUtil.sendDebug("User has joined before, but is not in the database, inserting user...",
                        this.getClass(), DebugUtil.getLineNumber());
                        Timestamp FirstLogin = TimeUtil.TimestampNow();
                        InsertUser(UUID, PlayerName, IPAddress, FirstLogin, LastLogin, true);
                        return true;
                    }
                    
                    DebugUtil.sendDebug("Preparing statment for getting TimesConnected", this.getClass(),
                    DebugUtil.getLineNumber());
                    PreparedStatement gtc = connection
                    .prepareStatement(String.format("SELECT TimesConnected FROM Users WHERE UUID = ?"));
                    gtc.setString(1, UUID);
                    
                    DebugUtil.sendDebug("Executing TimesConnected query...", this.getClass(),
                    DebugUtil.getLineNumber());
                    ResultSet gtc2 = gtc.executeQuery();
                    int tc = 1;
                    if (gtc2.next()) {
                        if (!gtc2.wasNull()) {
                            tc = gtc2.getInt("TimesConnected");
                        } else {
                            tc = 0;
                        }
                    }
                    CheckUser.close();
                    gtc.close();
                    if (IsJoining) {
                        DebugUtil.sendDebug("User is joining, attempting to communicate with bungeecord",
                                this.getClass(), DebugUtil.getLineNumber());
                        DebugUtil.sendDebug("Prepaing UpdateUser statement...", this.getClass(),
                                DebugUtil.getLineNumber());
                        // Preapre a statement
                        int i = 1;
                        PreparedStatement UpdateUser = connection.prepareStatement(
                                "UPDATE Users SET LastLogin = ?, PlayerName = ?, IPAddress = ?, LastServer = ?, TimesConnected = ?, IsOnline = ? WHERE UUID = ?");
                        UpdateUser.setTimestamp(i++, LastLogin);
                        UpdateUser.setString(i++, PlayerName);
                        UpdateUser.setString(i++, IPAddress);
                        if (User.getServer(PlayerName).get() == null)
                            UpdateUser.setString(i++, "error");
                        else
                            UpdateUser.setString(i++, User.getServer(PlayerName).get());
                        UpdateUser.setInt(i++, ++tc);
                        UpdateUser.setBoolean(i++, IsOnline);
                        UpdateUser.setString(i++, UUID);
                        DebugUtil.sendDebug("Attemping to execute update...", this.getClass(),
                                DebugUtil.getLineNumber());
                        UpdateUser.executeUpdate();
                        DebugUtil.sendDebug("UpdateUser was successful", this.getClass(), DebugUtil.getLineNumber());
                        UpdateUser.close();
                        return true;
                    }

                    DebugUtil.sendDebug("User is leaving...", this.getClass(), DebugUtil.getLineNumber());
                    int i = 1;
                    DebugUtil.sendDebug("Reparing Offline Update User", this.getClass(), DebugUtil.getLineNumber());
                    PreparedStatement UpdateUserOffline = connection.prepareStatement(
                            "UPDATE Users SET LastLogin = ?, PlayerName = ?, IPAddress = ?, TimesConnected = ?, IsOnline = ? WHERE UUID = ?");
                    UpdateUserOffline.setTimestamp(i++, LastLogin);
                    UpdateUserOffline.setString(i++, PlayerName);
                    UpdateUserOffline.setString(i++, IPAddress);
                    UpdateUserOffline.setInt(i++, tc);
                    UpdateUserOffline.setBoolean(i++, IsOnline);
                    UpdateUserOffline.setString(i++, UUID);
                    DebugUtil.sendDebug("Attemping to execute update...", this.getClass(), DebugUtil.getLineNumber());
                    DebugUtil.sendDebug("UpdateUserOffline was successful", this.getClass(), DebugUtil.getLineNumber());
                    UpdateUserOffline.executeUpdate();
                    UpdateUserOffline.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        });

        Main.pool.execute(t);

        return (Future<Boolean>) t;
    }

    /**
     * Lookup a user in the database
     * 
     * @param Search The lookup term for the database (Can be IP, Username, or UUID)
     * @return A result set with the user data, or a null one if the user doesn't
     *         exist.
     */
    public static Future<ResultSet> LookupUser(String Search) {
        FutureTask<ResultSet> t = new FutureTask<>(new Callable<ResultSet>() {
            @Override
            public ResultSet call() {
                // This is where you should do your database interaction
                try {
                    DebugUtil.sendDebug("Preparing lookup statement", this.getClass(), DebugUtil.getLineNumber());
                    // Preapre a statement
                    int i = 1;
                    PreparedStatement InsertUser = connection.prepareStatement(String
                            .format("SELECT * FROM Users WHERE UUID = ? OR PlayerName = ? OR IPAddress = ? LIMIT 1"));
                    InsertUser.setString(i++, Search);
                    InsertUser.setString(i++, Search);
                    InsertUser.setString(i++, Search);
                    DebugUtil.sendDebug("Returning lookup ResultSet", this.getClass(), DebugUtil.getLineNumber());
                    ResultSet result = InsertUser.executeQuery();
                    InsertUser.close();
                    return result;
                } 
                catch (Throwable e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });

        Main.pool.execute(t);

        return (Future<ResultSet>) t;
    }

    /**
     * Lookup a user in the database
     * 
     * @param Search The lookup term for the database (Can be IP, Username, or UUID)
     * @return A result set with the user data, or a null one if the user doesn't
     *         exist.
     */
    public static Future<Boolean> UpdateSpeed(Float speed, String uuid, String SpeedType) {
        FutureTask<Boolean> t = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                // This is where you should do your database interaction
                try {
                    // Preapre a statement
                    int i = 1;
                    if (SpeedType == "WalkSpeed") {
                        DebugUtil.sendDebug("SpeedType is WalkSpeed, preparing statement", this.getClass(), DebugUtil.getLineNumber());
                        PreparedStatement UpdateSpeed = connection
                                .prepareStatement(String.format("UPDATE Users SET WalkSpeed = ? WHERE UUID = ?"));
                        UpdateSpeed.setFloat(i++, speed);
                        UpdateSpeed.setString(i++, uuid);
                        DebugUtil.sendDebug("Attempting to execute update...", this.getClass(), DebugUtil.getLineNumber());
                        UpdateSpeed.executeUpdate();
                        UpdateSpeed.close();
                        DebugUtil.sendDebug("UpdateSpeed was successfull", this.getClass(), DebugUtil.getLineNumber());
                    } 
                    else if (SpeedType == "FlySpeed") {
                        DebugUtil.sendDebug("SpeedType is FlySpeed, preparing statement", this.getClass(), DebugUtil.getLineNumber());
                        PreparedStatement UpdateSpeed = connection
                        .prepareStatement(String.format("UPDATE Users SET FlySpeed = ? WHERE UUID = ?"));
                        UpdateSpeed.setFloat(i++, speed);
                        UpdateSpeed.setString(i++, uuid);
                        DebugUtil.sendDebug("Attempting to execute update...", this.getClass(), DebugUtil.getLineNumber());
                        UpdateSpeed.executeUpdate();
                        UpdateSpeed.close();
                        DebugUtil.sendDebug("UpdateSpeed was successfull", this.getClass(), DebugUtil.getLineNumber());
                    }
                    return true;
                } catch (Throwable e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });

        Main.pool.execute(t);

        return (Future<Boolean>) t;
    }

    /**
     * Lookup a user in the database
     * 
     * @param Search The lookup term for the database (Can be IP, Username, or UUID)
     * @return A result set with the user data, or a null one if the user doesn't
     *         exist.
     */
    public static Future<Float> GetSpeed(String uuid, String SpeedType) {
        FutureTask<Float> t = new FutureTask<>(new Callable<Float>() {
            @Override
            public Float call() {
                // This is where you should do your database interaction
                try {
                    // Preapre a statement
                    int i = 1;
                    if (SpeedType == "WalkSpeed") {
                        DebugUtil.sendDebug("SpeedType is WalkSpeed, preparing statement", this.getClass(), DebugUtil.getLineNumber());
                        PreparedStatement UpdateSpeed = connection
                                .prepareStatement(String.format("SELECT Walkspeed FROM Users WHERE UUID = ?"));
                        UpdateSpeed.setString(i++, uuid);

                        ResultSet result = UpdateSpeed.executeQuery();
                        if (result.wasNull()) {
                            // Format our message.
                            DebugUtil.sendDebug("WalkSpeed was null, returning default value 0.2F", this.getClass(), DebugUtil.getLineNumber());
                            return 0.2F;
                        }

                        if (result.next()) {
                            DebugUtil.sendDebug("WalkSpeed was not null, returning result...", this.getClass(), DebugUtil.getLineNumber());
                            return result.getFloat("WalkSpeed");
                        }
                        UpdateSpeed.close();
                    } 
                    else if (SpeedType == "FlySpeed") {
                        DebugUtil.sendDebug("SpeedType is FlySpeed, preparing statement", this.getClass(), DebugUtil.getLineNumber());
                        PreparedStatement UpdateSpeed = connection
                                .prepareStatement(String.format("SELECT FlySpeed FROM Users WHERE UUID = ?"));
                        UpdateSpeed.setString(i++, uuid);

                        ResultSet result = UpdateSpeed.executeQuery();
                        if (result.wasNull()) {
                            // Format our message.
                            DebugUtil.sendDebug("FlySpeed was null, returning default value 0.1F", this.getClass(), DebugUtil.getLineNumber());
                            return 0.1F;
                        }

                        if (result.next()) {
                            DebugUtil.sendDebug("FlySpeed was not null, returning result...", this.getClass(), DebugUtil.getLineNumber());
                            return result.getFloat("FlySpeed");
                        }
                        UpdateSpeed.close();
                        return 0.1F;
                    }
                } 
                catch (Throwable e) {
                    e.printStackTrace();
                    return 0.2F;
                }
                return 0.2F;
            }
        });

        Main.pool.execute(t);

        return (Future<Float>) t;
    }

}
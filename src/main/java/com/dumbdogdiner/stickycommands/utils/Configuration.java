package com.dumbdogdiner.stickycommands.utils;

import com.dumbdogdiner.stickycommands.Main;

import org.bukkit.configuration.file.FileConfiguration;

public class Configuration {
    static Main self = Main.getPlugin(Main.class);
    private static Configuration me;

    public static String dbhost = "";
    public static String dbname = "";
    public static String dbusername = "";
    public static String dbpassword = "";
    public static Integer dbport = 3306;
    public static Integer MaxReconnects = 5;
    public static Boolean useSSL = false;

    public static Long QueryUpdateLong;

    /**
     * Parse the configuration file.
     * 
     * @param config Config from spigot's system.
     */
    public Configuration(FileConfiguration config) {
        this.Reload(config);
        Configuration.me = this;
    }

    /**
     * Get the current plugin configuration
     * 
     * @return the currently allocated version of this object.
     */
    public static Configuration GetConfig() {
        return me;
    }

    /**
     * Reload the configuration from the file.
     * 
     * @param config Config from spigot's system
     */
    public void Reload(FileConfiguration config) {
        // Database
        Configuration.dbhost = config.getString("database.host");
        Configuration.dbport = config.getInt("database.port");
        Configuration.dbname = config.getString("database.name");
        Configuration.dbusername = config.getString("database.username");
        Configuration.dbpassword = config.getString("database.password");
        Configuration.MaxReconnects = config.getInt("database.maxReconnects");
        Configuration.useSSL = config.getBoolean("database.useSSL");
    }
}
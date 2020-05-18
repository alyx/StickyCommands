package com.dumbdogdiner.StickyCommands;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dumbdogdiner.StickyCommands.Commands.JumpCommand;
import com.dumbdogdiner.StickyCommands.Commands.SpeedCommand;
import com.dumbdogdiner.StickyCommands.Commands.TopCommand;
import com.dumbdogdiner.StickyCommands.Utils.Configuration;
import com.dumbdogdiner.StickyCommands.Utils.Messages;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    // For some reason using Futures with the Bukkit Async scheduler doesn't work.
    // Instead of relying on dumb bukkit APIs to get tasks done, we use a thread
    // pool of
    // our own control to get whatever we want done.
    public static ExecutorService pool = Executors.newFixedThreadPool(3);

    public void onEnable() {

        // Plugin startup logic
        new Configuration(this.getConfig());

        // Creating config folder, and adding config to it.
        if (!this.getDataFolder().exists()) {
            // Sticky~
            getLogger().info("Error: No folder for StickyCommands was found! Creating...");
            this.getDataFolder().mkdirs();
            this.saveDefaultConfig();
            getLogger().severe("Please configure StickyCommands and restart the server! :)");
            // They're not gonna have their database setup, just exit. It stops us from
            // having errors.
            return;
        }

        if (!(new File(this.getDataFolder(), "config.yml").exists())) {
            this.saveDefaultConfig();
            getLogger().severe("Please configure StickyCommands and restart the server! :)");
            // They're not gonna have their database setup, just exit. It stops us from
            // having errors.
            return;
        }

        // Make sure our messages file exists
        Messages.GetMessages();

/*         // Initialize our database connections.
        if (!DatabaseUtil.InitializeDatabase())
            return; */

        this.getCommand("top").setExecutor(new TopCommand());
        this.getCommand("jump").setExecutor(new JumpCommand());
        this.getCommand("speed").setExecutor(new SpeedCommand());

        getLogger().info("StickyCommands started successfully!");
    }

    public void onDisable() {

    }
}
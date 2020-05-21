package com.dumbdogdiner.StickyCommands; // package owo

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dumbdogdiner.StickyCommands.Commands.JumpCommand;
import com.dumbdogdiner.StickyCommands.Commands.SeenCommand;
import com.dumbdogdiner.StickyCommands.Commands.SmiteCommand;
import com.dumbdogdiner.StickyCommands.Commands.SpeedCommand;
import com.dumbdogdiner.StickyCommands.Commands.StickyCommand;
import com.dumbdogdiner.StickyCommands.Commands.TopCommand;
import com.dumbdogdiner.StickyCommands.Listeners.PlayerConnectionListeners;
import com.dumbdogdiner.StickyCommands.Utils.Configuration;
import com.dumbdogdiner.StickyCommands.Utils.DatabaseUtil;
import com.dumbdogdiner.StickyCommands.Utils.Messages;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class Main extends JavaPlugin implements PluginMessageListener {
    // For some reason using Futures with the Bukkit Async scheduler doesn't work.
    // Instead of relying on dumb bukkit APIs to get tasks done, we use a thread
    // pool of
    // our own control to get whatever we want done.
    public static ExecutorService pool = Executors.newFixedThreadPool(3);

    public Boolean sqlError = false;
    public static CompletableFuture<String> serverName;

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
            // return;
        }

        if (!(new File(this.getDataFolder(), "config.yml").exists())) {
            this.saveDefaultConfig();
            getLogger().severe("Please configure StickyCommands and restart the server! :)");
            // They're not gonna have their database setup, just exit. It stops us from
            // having errors.
            // return;
        }

        // Initialize our database connections.
        if (!DatabaseUtil.InitializeDatabase())
            getLogger().severe("Database failed to connect! Disabling seen and speed commands and login events");

        // return;

        else {
            this.getCommand("seen").setExecutor(new SeenCommand());
            this.getCommand("speed").setExecutor(new SpeedCommand());
            getServer().getPluginManager().registerEvents(new PlayerConnectionListeners(), this);
        }

        // Make sure our messages file exists
        Messages.GetMessages();

        this.getCommand("top").setExecutor(new TopCommand());
        this.getCommand("jump").setExecutor(new JumpCommand());
        this.getCommand("stickycommands").setExecutor(new StickyCommand());
        this.getCommand("smite").setExecutor(new SmiteCommand());
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        getLogger().info("StickyCommands started successfully!");
    }

    @Override
    public void onDisable() {
        // Save our config values
        reloadConfig();
        // Close out or database.
        DatabaseUtil.Terminate();
    }

    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if(subchannel.equals("GetServer")) {
            String name = in.readUTF();

            if (Main.serverName == null)
                Main.serverName = new CompletableFuture<>();

            Main.serverName.complete(name);
        }
    }
}

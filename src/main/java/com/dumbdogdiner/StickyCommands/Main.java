package com.dumbdogdiner.StickyCommands; // package owo

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dumbdogdiner.StickyCommands.Commands.AFKComand;
import com.dumbdogdiner.StickyCommands.Commands.HatCommand;
import com.dumbdogdiner.StickyCommands.Commands.ItemCommand;
import com.dumbdogdiner.StickyCommands.Commands.JumpCommand;
import com.dumbdogdiner.StickyCommands.Commands.MemoryCommand;
import com.dumbdogdiner.StickyCommands.Commands.SeenCommand;
import com.dumbdogdiner.StickyCommands.Commands.SellCommand;
import com.dumbdogdiner.StickyCommands.Commands.SmiteCommand;
import com.dumbdogdiner.StickyCommands.Commands.SpeedCommand;
import com.dumbdogdiner.StickyCommands.Commands.StickyCommand;
import com.dumbdogdiner.StickyCommands.Commands.TopCommand;
import com.dumbdogdiner.StickyCommands.Commands.WorthCommand;
import com.dumbdogdiner.StickyCommands.Listeners.PlayerConnectionListeners;
import com.dumbdogdiner.StickyCommands.Listeners.PlayerMovementListener;
import com.dumbdogdiner.StickyCommands.Utils.Configuration;
import com.dumbdogdiner.StickyCommands.Utils.DatabaseUtil;
import com.dumbdogdiner.StickyCommands.Utils.DebugUtil;
import com.dumbdogdiner.StickyCommands.Utils.Item;
import com.dumbdogdiner.StickyCommands.Utils.Messages;
import com.dumbdogdiner.StickyCommands.Utils.User;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitScheduler;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin implements PluginMessageListener {
    // For some reason using Futures with the Bukkit Async scheduler doesn't work.
    // Instead of relying on dumb bukkit APIs to get tasks done, we use a thread
    // pool of
    // our own control to get whatever we want done.
    public static ExecutorService pool = Executors.newFixedThreadPool(3);
    public static HashMap<UUID, User> USERS = new HashMap<UUID, User>();

    public Boolean sqlError = false;
    public static CompletableFuture<String> serverName;
    private static Economy econ = null;

    public void onEnable() {

        // Plugin startup logic
        new Configuration(this.getConfig());
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled economy commands due to no Vault dependency found!", getDescription().getName()));
        }

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
        DebugUtil.sendDebug("Config successfully loaded", this.getClass(), DebugUtil.getLineNumber());

        // Initialize our database connections.
        if (!DatabaseUtil.InitializeDatabase())
            getLogger().severe("Database failed to connect! Disabling seen and speed commands and login events");
        else {
            DebugUtil.sendDebug("Database connected successfully, registering /seen and /speed commands and connection listeners", this.getClass(), DebugUtil.getLineNumber());
            this.getCommand("seen").setExecutor(new SeenCommand());
            this.getCommand("speed").setExecutor(new SpeedCommand());
            getServer().getPluginManager().registerEvents(new PlayerConnectionListeners(), this);
        }

        DebugUtil.sendDebug("Grabbing messages", this.getClass(), DebugUtil.getLineNumber());
        // Make sure our messages file exists
        Messages.GetMessages();

        DebugUtil.sendDebug("Grabbing item worth values", this.getClass(), DebugUtil.getLineNumber());
        // Grab the worth values for our items
        Item.getItems();

        if (this.getConfig().getBoolean("general.allowSelling")) {
            DebugUtil.sendDebug("Attempting to disabled /sell and /worth", this.getClass(), DebugUtil.getLineNumber());
            this.getCommand("worth").setExecutor(new WorthCommand());
            this.getCommand("sell").setExecutor(new SellCommand());
            getLogger().info("Worth and selling commands are disabled in this server, skipping commmand registration");
        }

        DebugUtil.sendDebug("Attempting to register PlayerMovementListener", this.getClass(), DebugUtil.getLineNumber());
        getServer().getPluginManager().registerEvents(new PlayerMovementListener(), this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            USERS.put(p.getUniqueId(), new User(p));
        }

        DebugUtil.sendDebug("Attempting to register commands", this.getClass(), DebugUtil.getLineNumber());
        this.getCommand("top").setExecutor(new TopCommand());
        this.getCommand("jump").setExecutor(new JumpCommand());
        this.getCommand("stickycommands").setExecutor(new StickyCommand());
        this.getCommand("smite").setExecutor(new SmiteCommand());
        this.getCommand("afk").setExecutor(new AFKComand());
        this.getCommand("hat").setExecutor(new HatCommand());
        this.getCommand("item").setExecutor(new ItemCommand());
        this.getCommand("item").setTabCompleter(new ItemCommand());
        this.getCommand("memory").setExecutor(new MemoryCommand());
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

    public static Economy getEconomy() {
        return econ;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}

package com.dumbdogdiner.stickycommands; // package owo

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dumbdogdiner.stickycommands.commands.*;
import com.dumbdogdiner.stickycommands.listeners.PlayerInteractionListener;
import com.dumbdogdiner.stickycommands.listeners.PlayerJoinListener;
import com.dumbdogdiner.stickycommands.runnables.AfkTimeRunnable;
import com.dumbdogdiner.stickycommands.listeners.AfkEventListener;
import com.dumbdogdiner.stickycommands.utils.Database;
import com.dumbdogdiner.stickycommands.utils.Item;
import com.dumbdogdiner.stickyapi.StickyAPI;
import com.dumbdogdiner.stickyapi.bukkit.util.CommandUtil;
import com.dumbdogdiner.stickyapi.bukkit.util.StartupUtil;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;
import com.dumbdogdiner.stickyapi.common.util.TimeUtil;

import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;

public class StickyCommands extends JavaPlugin {

    /**
     * The singleton instance of the plugin.
     */
    @Getter
    static StickyCommands instance;

    @Getter
    protected Boolean enabled = false;

    /**
     * Thread pool for the execution of asynchronous tasks.
     */
    @Getter
    protected ExecutorService pool = Executors.newFixedThreadPool(3);

    /**
     * Cache of all online users.
     */
    @Getter
    protected HashMap<UUID, User> onlineUserCache = new HashMap<UUID, User>();


    /**
     * AFK TimerTask that tracks how long a player has been AFK
     */
    @Getter
    protected Timer afkRunnable = new Timer();
    

    /**
     * The server's uptime in seconds
     */
    @Getter
    protected Long upTime = TimeUtil.getUnixTime();

    /**
     * The current vault economy instance.
     */
    @Getter
    Economy economy = null;

    @Getter
    LocaleProvider localeProvider;

    /**
     * The LuckPerms API instance
     */
    @Getter
    LuckPerms perms;

    /**
     * The database connected
     */
    @Getter
    Database database;

    
    @Override
    public void onLoad() {
        enabled = true;
        instance = this;
        // Set our thread pool
        StickyAPI.setPool(pool);
        new Item();
        // onlineUserCache.setMaxSize(1000);
    }
    
    @Override
    public void onEnable() {
        if (!StartupUtil.setupConfig(this))
            return;
        
        this.localeProvider = StartupUtil.setupLocale(this, this.localeProvider);
        if (this.localeProvider == null)
            return;
        // Register PAPI support if present
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Bukkit.getLogger().info("Registering PlaceholderAPI placeholders");

            StickyCommandsPlaceholder.getInstance().register();
        }

        
        if (!setupEconomy())
            getLogger().severe("Disabled economy commands due to no Vault dependency found!");

        if (!setupLuckperms())
            getLogger().severe("Disabled group listing/luckperms dependant features due to no Luckperms dependency found!");

        
        this.database = new Database();
        database.createMissingTables();
        
        // Register currently online users - in case of a reload.
        // (stop reloading spigot, please.)
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.onlineUserCache.put(player.getUniqueId(), new User(player));
        }
        
        if (!registerEvents())
        return;
        
        if (!registerCommands())
        return;
        
        afkRunnable.scheduleAtFixedRate(new AfkTimeRunnable(), 1000L, 1000L); // We must run this every ONE second!
        
        getLogger().info("StickyCommands started successfully!");
    }

    private boolean setupLuckperms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            perms = provider.getProvider();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDisable() {
        reloadConfig(); // Save our config
        database.terminate(); // Terminate our database connection
        afkRunnable.cancel(); // Stop our AFK runnable
        enabled = false;
    }
    
    /**
     * Setup the vault economy instance.
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
    
    /**
     * Register all the commands!
     */
    boolean registerCommands() {
        List<Command> commandList = new ArrayList<Command>();
        // Register economy based commands only if the economy provider is not null.
        if (economy != null) {
            commandList.add(new Sell(this));
            commandList.add(new Worth(this));
        }

        commandList.add(new Speed(this));
        commandList.add(new Seen(this));
    
        commandList.add(new Kill(this));
        commandList.add(new Jump(this));
        commandList.add(new Memory(this));
        commandList.add(new Top(this));
        commandList.add(new PowerTool(this));
        commandList.add(new Afk(this));
        commandList.add(new PlayerTime(this));
        commandList.add(new Smite(this));

        CommandUtil.registerCommands(getServer(), commandList);
        return true;
    }

    boolean registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerInteractionListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new AfkEventListener(), this);
        return true;
    }

    /**
     * Get an online user
     * 
     * @param uuid the UUID of the user to lookup
     * @return The user if found, otherwise null
     */
    public User getOnlineUser(UUID uuid) {
        for (User user : getOnlineUserCache().values()) {
            if (user.getUniqueId().equals(uuid))
                return user;
        }
        return null;
    }
    

    // Before you get mad, just remember this knob named md_5 couldn't help but make Bukkit the worst Minecraft API
    // and while making it, didn't add a way of getting the server's TPS without NMS or reflection.
    // Special thanks to this guy who saved me all of 5 minutes! https://gist.github.com/vemacs/6a345b2f9822b79a9a7f
    
    private static Object minecraftServer;
    private static Field recentTps; 
    /**
     * Get the server's recent TPS
     * @return {@link java.lang.Double} The server TPS in the last 15 minutes (1m, 5m, 15m)
     */
    public double[] getRecentTps() {        
        try {
            if (minecraftServer == null) {
                Server server = Bukkit.getServer();
                Field consoleField = server.getClass().getDeclaredField("console");
                consoleField.setAccessible(true);
                minecraftServer = consoleField.get(server);
            }
            if (recentTps == null) {
                recentTps = minecraftServer.getClass().getSuperclass().getDeclaredField("recentTps");
                recentTps.setAccessible(true);
            }
            return (double[]) recentTps.get(minecraftServer);
        } catch (IllegalAccessException | NoSuchFieldException ignored) {
        }
        return new double[] {0, 0, 0}; // If there's an issue, let's make it known.
    }
}

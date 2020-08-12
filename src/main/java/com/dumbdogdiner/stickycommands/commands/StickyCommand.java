package com.dumbdogdiner.stickycommands.commands;

import java.util.Map;
import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.utils.Configuration;
import com.dumbdogdiner.stickycommands.utils.DatabaseUtil;
import com.dumbdogdiner.stickycommands.utils.Messages;
import com.dumbdogdiner.stickycommands.utils.PermissionUtil;
import com.dumbdogdiner.stickycommands.utils.User;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class StickyCommand implements CommandExecutor {

    private Main self = Main.getPlugin(Main.class);
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.reload", true))
            return User.PermissionDenied(sender, "stickycommands.reload");

        // Format our message.
        Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
            {
                put("player", sender.getName());
            }
        };

        if (args.length < 1) {
            try { 
                Messages.Reload();
                new Configuration(self.getConfig());
                self.reloadConfig();
                sender.sendMessage(Messages.Translate("reload.configsSuccess", Variables));
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(Messages.serverError);
                return false;
            }
        }
        if (args[0].equalsIgnoreCase("reload")) {
            try { 
                Messages.Reload();
                new Configuration(self.getConfig());
                self.reloadConfig();
                sender.sendMessage(Messages.Translate("reload.configsSuccess", Variables));
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(Messages.serverError);
                return false;
            }
        }
        
        if (args[0].equalsIgnoreCase("database") || args[0].equalsIgnoreCase("db")) {
            if (!PermissionUtil.Check(sender, "stickycommands.reload.database", true))
                return User.PermissionDenied(sender, "stickycommands.reload.database");
            
            try {
                self.sqlError = true;
                DatabaseUtil.Terminate();
                // Initialize our database connections.
                if (!DatabaseUtil.initDatabase()) {
                    self.getLogger().severe("Database failed to connect! Disabling /seen command and login events");
                    sender.sendMessage(Messages.Translate("reload.error", Variables));
                    self.sqlError = true;
                }
                self.sqlError = false;
                sender.sendMessage(Messages.Translate("reload.databaseSuccess", Variables));
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(Messages.serverError);
                return false;
            }
        }

        if (args[0].equalsIgnoreCase("config")) {
            if (args.length < 3)
                return User.invalidSyntax(sender);

            if (args[1].equalsIgnoreCase("debug")) {
                self.getConfig().set("general.debug", Boolean.parseBoolean(args[2]));
                sender.sendMessage(Messages.prefix + ChatColor.GREEN + "Set debug mode to " + Boolean.parseBoolean(args[2]));
            }
        }
/* 
        if (args[0].equalsIgnoreCase("jar")) {
            if (args.length < 2)
                return User.invalidSyntax(sender);

            if (args[1].equalsIgnoreCase("reload")) {
                Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("StickyCommands");
                plugin.getPluginLoader().disablePlugin(plugin);
                plugin.getPluginLoader().enablePlugin(plugin);
                
            }
        } */
        return true;
    }
    

}
package com.dumbdogdiner.StickyCommands.Commands;

import java.util.Map;
import java.util.TreeMap;

import com.dumbdogdiner.StickyCommands.Main;
import com.dumbdogdiner.StickyCommands.Utils.Configuration;
import com.dumbdogdiner.StickyCommands.Utils.DatabaseUtil;
import com.dumbdogdiner.StickyCommands.Utils.Messages;
import com.dumbdogdiner.StickyCommands.Utils.PermissionUtil;
import com.dumbdogdiner.StickyCommands.Utils.User;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;

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
                if (!DatabaseUtil.InitializeDatabase()) {
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
        return true;
    }
    

}
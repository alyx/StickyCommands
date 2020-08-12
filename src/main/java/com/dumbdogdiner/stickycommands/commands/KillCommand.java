package com.dumbdogdiner.stickycommands.commands;

import java.util.Map;
import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.utils.Messages;
import com.dumbdogdiner.stickycommands.utils.PermissionUtil;
import com.dumbdogdiner.stickycommands.utils.User;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

public class KillCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.kill", false))
            return User.PermissionDenied(sender, "stickycommands.kill");

        Player player = (Player) sender;
        Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
            {
                put("player", player.getName());
            }
        };

        try{
            if (args.length < 1) {
                sender.sendMessage(Messages.Translate("kill.suicide", Variables));
                player.setHealth(0);
                return true;
            }
            if (args.length > 0) {
                
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null)
                    return User.invalidPlayer(sender, args[0]);
                Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                    {
                        put("player", target.getName());
                    }
                };
                if (target == player) {
                        target.setHealth(0);
                        sender.sendMessage(Messages.Translate("kill.suicide", Variables));
                        return true;
                }
                sender.sendMessage(Messages.Translate("kill.killed", Variables));
                target.sendMessage(Messages.Translate("kill.beenKilled", Variables));
                target.setHealth(0);
                
            }

        }
        catch (InvalidConfigurationException e) {
            e.printStackTrace();
            sender.sendMessage(Messages.serverError);
            return false;
        }
        return true;
    }

}
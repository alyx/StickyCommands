package com.dumbdogdiner.stickycommands.commands;

import java.util.Map;
import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.utils.DebugUtil;
import com.dumbdogdiner.stickycommands.utils.Messages;
import com.dumbdogdiner.stickycommands.utils.PermissionUtil;
import com.dumbdogdiner.stickycommands.utils.User;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AFKComand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.afk", false))
            return User.PermissionDenied(sender, "stickycommands.afk");

        if (args.length > 0)
            return User.invalidSyntax(sender);

        try {
            Player player = (Player) sender;
            User u = Main.USERS.get(player.getUniqueId());
            Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {{put("player", player.getName());}};

            if (u.isAfk()) {
                u.setAfk(false);
                DebugUtil.sendDebug("Setting AFK mode to false for " + player.getName(), this.getClass(), DebugUtil.getLineNumber());
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(Messages.Translate("notAfkMessage", Variables));
                }
            } 
            else {
                u.setAfk(true);
                DebugUtil.sendDebug("Setting AFK mode to true for " + player.getName(), this.getClass(), DebugUtil.getLineNumber());
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(Messages.Translate("afkMessage", Variables));
                }
            }

        } 
        catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(Messages.serverError);
            return false;
        }
        return true;
    }
}
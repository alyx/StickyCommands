package com.dumbdogdiner.StickyCommands.Commands;

import com.dumbdogdiner.StickyCommands.Utils.PermissionUtil;
import com.dumbdogdiner.StickyCommands.Utils.User;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BoopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.boop", false))
            return User.PermissionDenied(sender, "stickycommands.boop");

        if (args.length < 1) 
            return User.invalidSyntax(sender);

        if (Bukkit.getPlayer(args[0]) == null)
            return User.invalidSyntax(sender);

        Player target = Bukkit.getPlayer(args[0]);
        target.playSound(target.getLocation(), Sound.ENTITY_FOX_AMBIENT, 1.0F, 1.0F);
        target.sendMessage(ChatColor.ITALIC + "" + ChatColor.LIGHT_PURPLE + sender.getName() + ": *boop*! " + ChatColor.RED + "❤");

        return false;
    }
    

}
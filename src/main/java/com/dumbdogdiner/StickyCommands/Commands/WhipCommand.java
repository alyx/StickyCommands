package com.dumbdogdiner.StickyCommands.Commands;

import java.util.Map;
import java.util.TreeMap;

import com.dumbdogdiner.StickyCommands.Utils.Messages;
import com.dumbdogdiner.StickyCommands.Utils.PermissionUtil;
import com.dumbdogdiner.StickyCommands.Utils.TranslationUtil;
import com.dumbdogdiner.StickyCommands.Utils.User;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class WhipCommand implements CommandExecutor {

    // Syntax: /whip <player> [times] [damage]
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.whip", false))
            return User.PermissionDenied(sender, "stickycommands.whip");

        if (args.length < 1)
            return User.invalidSyntax(sender);

        CraftPlayer target = (CraftPlayer) Bukkit.getPlayer(args[0]);

        if (target == null)
            return User.invalidPlayer(sender, args[0]);
        try {
            if (args.length == 1) {
                User.whipPlayer(target, 1, 0);
                Map<String, String> Vars = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                    {
                        put("player", target.getName());
                        put("amount", "1");
                        put("damage", "1");
                    }
                };
                target.sendMessage(Messages.Translate("whip.wereWhipped", Vars));
                sender.sendMessage(Messages.Translate("whip.youWhipped", Vars));
            }

            else if (args.length > 2) {
                if (!TranslationUtil.isInteger(args[1]) || !TranslationUtil.isInteger(args[2]))
                    return User.invalidSyntax(sender);

                if (Integer.valueOf(args[1]) > 20)
                    return User.invalidSyntax(sender);
                Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                    {
                        put("player", target.getName());
                        put("amount", args[1]);
                        put("damage", args[2]);
                    }
                };
                sender.sendMessage(Messages.Translate("whip.youWhipped", Variables));
                target.sendMessage(Messages.Translate("whip.wereWhipped", Variables));
                

                User.whipPlayer(target, Integer.valueOf(args[1]), Double.valueOf(args[2]));

            }
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            sender.sendMessage(Messages.serverError);
            return false;
        }

        return true;
    }

}
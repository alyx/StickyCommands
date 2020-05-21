package com.dumbdogdiner.StickyCommands.Commands;

import java.util.Map;
import java.util.TreeMap;

import com.dumbdogdiner.StickyCommands.Main;
import com.dumbdogdiner.StickyCommands.Utils.DatabaseUtil;
import com.dumbdogdiner.StickyCommands.Utils.Messages;
import com.dumbdogdiner.StickyCommands.Utils.PermissionUtil;
import com.dumbdogdiner.StickyCommands.Utils.TranslationUtil;
import com.dumbdogdiner.StickyCommands.Utils.User;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class SmiteCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.smite", false))
            return User.PermissionDenied(sender, "stickycommands.smite");

        if (args.length < 1) {
            sender.sendMessage(Messages.invalidSyntax);
            return false;
        }
        int power = 5;
        if (args.length > 1 && TranslationUtil.isInteger(args[1])) {
            power = Integer.parseInt(args[1]);
        }
        try {
            if (Bukkit.getPlayer(args[0]) == null) {
                // Format our message.
                Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                    {
                        put("player", args[0]);
                    }
                };
                sender.sendMessage(Messages.Translate("playerDoesNotExist", Variables));
            }
            Player player = Bukkit.getPlayer(args[0]);
            World world = player.getWorld();
            Location loc = player.getLocation();
            final LightningStrike strike = world.strikeLightningEffect(loc);

            // Format our message.
            Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                {
                    put("player", player.getName());
                    put("playerNick", player.getDisplayName());
                    put("sender", sender.getName());
                    put("locx", Double.toString(Math.round(loc.getX())));
                    put("locy", Double.toString(Math.round(loc.getX())));
                    put("locz", Double.toString(Math.round(loc.getX())));
                }
            };
            sender.sendMessage(Messages.Translate("youSmitted", Variables));
            player.sendMessage(Messages.Translate("youWereSmitten", Variables));

            player.damage(power, strike);
        } 
        catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(Messages.serverError);
            return false;
        }
        return true;
    }

}
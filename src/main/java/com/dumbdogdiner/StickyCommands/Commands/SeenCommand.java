package com.dumbdogdiner.StickyCommands.Commands;

import java.sql.ResultSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;

import com.dumbdogdiner.StickyCommands.Utils.DatabaseUtil;
import com.dumbdogdiner.StickyCommands.Utils.Messages;
import com.dumbdogdiner.StickyCommands.Utils.PermissionUtil;
import com.dumbdogdiner.StickyCommands.Utils.TimeUtil;
import com.dumbdogdiner.StickyCommands.Utils.User;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SeenCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.seen", true))
            return User.PermissionDenied(sender, "stickycommands.seen");

        try {
            if (args.length < 1) {
                sender.sendMessage(Messages.invalidSyntax);
                return false;
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            Future<ResultSet> res = DatabaseUtil.LookupUser(args[0]);
            ResultSet result = res.get();

            if (player == null) {
                // Format our message.
                Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                    {
                        put("player", args[0]);
                    }
                };
                sender.sendMessage(Messages.Translate("playerDoesNotExist", Variables));
                return true;
            }

            if (result.wasNull()) {
                // Format our message.
                Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                    {
                        put("player", args[0]);
                    }
                };
                sender.sendMessage(Messages.Translate("playerHasNotJoined", Variables));
                return true;
            }

            if (result.next()) {
                // Format our message.
                Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                    {
                        put("player", result.getString("PlayerName"));
                        put("uuid", result.getString("UUID"));
                        put("ipaddress", result.getString("IPAddress"));
                        put("firstlogin", TimeUtil.TimeString(result.getTimestamp("FirstLogin")));
                        put("lastlogin", TimeUtil.TimeString(result.getTimestamp("LastLogin")));
                        put("timesconnected", Integer.toString(result.getInt("TimesConnected")));
                        put("online", Boolean.toString(result.getBoolean("IsOnline")));

                    }
                };
                System.out.println(TimeUtil.DurationString(result.getTimestamp("FirstLogin")));
                sender.sendMessage(Messages.Translate("seenMessage", Variables));
            }

        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(Messages.serverError);
            return true;
        }

        return true;
    }
}
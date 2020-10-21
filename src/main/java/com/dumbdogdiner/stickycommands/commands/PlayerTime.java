package com.dumbdogdiner.stickycommands.commands;

import java.util.Arrays;
import java.util.TreeMap;

import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;
import com.dumbdogdiner.stickyapi.common.util.NumberUtil;
import com.dumbdogdiner.stickycommands.Main;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlayerTime extends AsyncCommand {

    LocaleProvider locale = Main.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();

    public PlayerTime(Plugin owner) {
        super("ptime", owner);
        setPermission("stickycommands.ptime");
        setDescription("Adjust player's client time.");
        setAliases(Arrays.asList("ptime,eplayertime,eptime".split(",")));
        variables.put("syntax", "/ptime [reset|day|night|dawn|0]");
    }

    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.hasPermission("stickycommands.sell") || (!(sender instanceof Player)))
            return ExitCode.EXIT_PERMISSION_DENIED;

        var player = (Player) sender;
        variables.put("player", player.getName());
        variables.put("player_uuid", player.getUniqueId().toString());
        variables.put("time", String.valueOf(player.getWorld().getFullTime()));
        variables.put("time_hour", String.valueOf(player.getWorld().getTime()));
        variables.put("world", String.valueOf(player.getWorld().getName()));

        Arguments a = new Arguments(args);
        a.optionalString("time");
        a.optionalFlag("relative", "relative");
        variables.put("relative", String.valueOf(a.exists("relative")));

        String time = a.get("time");

        if (time == null 
        || time.equalsIgnoreCase("reset")
        || time.equalsIgnoreCase("normal")
        || time.equalsIgnoreCase("default")) {
            player.setPlayerTime(0, true);
            player.sendMessage(locale.translate("player-time.time-reset", variables));
            return ExitCode.EXIT_SUCCESS;

        } else if (NumberUtil.isNumeric(time)) {
            if (Long.parseLong(time) > 24000L || Long.parseLong(time) < 0L) 
                return ExitCode.EXIT_INVALID_SYNTAX;
            return setPlayerTime(player, Long.parseLong(time), a);

        } else {
            switch (time.toLowerCase()) {
                case "dawn":
                case "sunrise":
                    return setPlayerTime(player, 23000L, a);
                case "day":
                    return setPlayerTime(player, 0L, a);
                case "morning":
                    return setPlayerTime(player, 1000L, a);
                case "midday":
                case "noon":
                    return setPlayerTime(player, 6000L, a);
                case "afternoon":
                    return setPlayerTime(player, 9000L, a);
                case "dusk":
                    return setPlayerTime(player, 12000L, a);
                case "night":
                    return setPlayerTime(player, 14000L, a);
                case "midnight":
                    return setPlayerTime(player, 18000L, a);
            }
        }
        return ExitCode.EXIT_SUCCESS;
    }


    @Override
    public void onSyntaxError(CommandSender sender, String label, String[] args) {
        sender.sendMessage(locale.translate("invalid-syntax", variables));
    }

    @Override
    public void onPermissionDenied(CommandSender sender, String label, String[] args) {
        sender.sendMessage(locale.translate("no-permission", variables));
    }

    @Override
    public void onError(CommandSender sender, String label, String[] args) {
        sender.sendMessage(locale.translate("server-error", variables));
    }

    ExitCode setPlayerTime(Player player, Long time, Arguments args) {
        var relative = args.exists("relative");
        variables.put("time", String.valueOf(time));
        variables.put("time_hour", String.valueOf(time * 1000L));
        variables.put("relative", String.valueOf(relative));
        player.setPlayerTime(time, relative);
        player.sendMessage(locale.translate("player-time.time-set", variables));
        return ExitCode.EXIT_SUCCESS;
    }
}

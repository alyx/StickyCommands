package com.dumbdogdiner.stickycommands.commands;

import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;
import com.dumbdogdiner.stickyapi.common.util.NumberUtil;
import com.dumbdogdiner.stickycommands.StickyCommands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.TreeMap;

public class PlayerTimeCommand extends AsyncCommand {

    LocaleProvider locale = StickyCommands.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();

    public PlayerTimeCommand(Plugin owner) {
        super("ptime", owner);
        setPermission("stickycommands.ptime");
        setDescription("Adjust player's client time.");
        setAliases(Arrays.asList("ptime,eplayertime,eptime".split(",")));
        variables.put("syntax", "/ptime [reset|day|night|dawn|0]");
    }

    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.hasPermission("stickycommands.sell") || (!(sender instanceof Player)))
            return ExitCode.EXIT_PERMISSION_DENIED.setMessage(locale.translate("no-permission", variables));

        var player = (Player) sender;
        variables.put("player", player.getName());
        variables.put("player_uuid", player.getUniqueId().toString());
        variables.put("time", String.valueOf(player.getWorld().getFullTime()));
        variables.put("time_hour", String.valueOf(player.getWorld().getTime()));
        variables.put("world", player.getWorld().getName());

        Arguments a = new Arguments(args);
        //TODO: In stickyapi, make a optionalFlag that has multiple possible names
        a.optionalString("time");
        a.optionalFlag("relative");
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
            try {
                if (Long.parseLong(time) > 24000L || Long.parseLong(time) < 0L) 
                    return onSyntaxError();
                return setPlayerTime(player, Long.parseLong(time), a);
            } catch (NumberFormatException e) { // They likely gave a number to big or small, so it's just invalid.
                return onSyntaxError();
            }

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
                default:
                    return onSyntaxError();
            }
        }
    }

    ExitCode onSyntaxError() {
        return ExitCode.EXIT_INVALID_SYNTAX.setMessage(locale.translate("invalid-syntax", variables));
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

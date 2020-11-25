package com.dumbdogdiner.stickycommands.commands;

import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.bukkit.util.ServerUtil;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;
import com.dumbdogdiner.stickyapi.common.util.StringUtil;
import com.dumbdogdiner.stickyapi.common.util.TimeUtil;
import com.dumbdogdiner.stickycommands.StickyCommands;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class MemoryCommand extends AsyncCommand {
    LocaleProvider locale = StickyCommands.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();

    public MemoryCommand(Plugin owner) {
        super("memory", owner);
        setPermission("stickycommands.memory");
        setDescription("Check the server's performance");
        setAliases(Arrays.asList("lag,elag,egc,mem,emem,memory,ememory,uptime,euptime,tps,etps,entities,eentities".split(",")));
        variables.put("syntax", "/memory");
    }

    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        try {
            if (!sender.hasPermission("stickycommands.memory"))
                return ExitCode.EXIT_PERMISSION_DENIED.setMessage(locale.translate("no-permission", variables));
            if (!(sender instanceof Player)) {
                sender.sendMessage(locale.translate("must-be-player", new TreeMap<String, String>()));
                return ExitCode.EXIT_SUCCESS;
            }
            var player = (Player) sender;
            var df = new DecimalFormat("0.0");

            // TODO Use an external utility to get memory usage stuffs
            var max = Runtime.getRuntime().maxMemory() / 1024 / 1024;
            var used = Runtime.getRuntime().totalMemory() / 1024 / 1024 - Runtime.getRuntime().freeMemory() / 1024 / 1024;
            double usage = Double.parseDouble(df.format((((float)used / max) * 100)));
            char color = usage < 60 ? 'a' : (usage < 85 ? 'e' : 'c');
            
            double[] tps = ServerUtil.getRecentTps();
            
            variables.put("tps_1m", String.valueOf(df.format(tps[0])));
            variables.put("tps_5m", String.valueOf(df.format(tps[1])));
            variables.put("tps_15m", String.valueOf(df.format(tps[2])));
            variables.put("max_memory", String.valueOf(max));
            variables.put("used_memory", String.valueOf(used));
            variables.put("memory_bar", ChatColor.translateAlternateColorCodes('&', "&f[&" + color + StringUtil.createProgressBar(25, usage, false, true, false) + "&f]"));
            variables.put("loaded_chunks", String.valueOf(player.getWorld().getLoadedChunks().length));
            variables.put("entities", String.valueOf(player.getWorld().getEntities().size()));
            variables.put("world", player.getWorld().getName());
            variables.put("uptime", String.valueOf(TimeUtil.getUnixTime() - StickyCommands.getInstance().getUpTime()));
            variables.put("uptime_long", String.valueOf(StickyCommands.getInstance().getUpTime()));
            sender.sendMessage(locale.translate("memory-message", variables));
        } catch (Exception e) {
            e.printStackTrace();
            return ExitCode.EXIT_ERROR.setMessage(locale.translate("server-error", variables));
        }
        return ExitCode.EXIT_SUCCESS;
    }
    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
        return List.of();
    }

    public String createBar(double size, double usage) {
        var barCount = ((usage / 100) * size);
        StringBuilder bar = new StringBuilder();
        for (var i = 0.0; i < size; i++) {
            if (i < barCount && size - i > 5)
                bar.append("\u0258");
            else {
                if (size - i == 5) {
                    bar.append(usage).append("%");
                    break;
                }
                bar.append(" "); // single space
            }
        }
        return bar.toString();
    }
}
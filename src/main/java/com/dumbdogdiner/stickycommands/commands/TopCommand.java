package com.dumbdogdiner.stickycommands.commands;

import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.StickyCommands;
import com.dumbdogdiner.stickycommands.utils.LocationUtil;
import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TopCommand extends AsyncCommand {

    LocaleProvider locale = StickyCommands.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();
    public TopCommand(Plugin owner) {
        super("top", owner);
        setPermission("stickycommands.top");
        setDescription("Teleport to the highest block above you");
        variables.put("syntax", "/top");
    }

    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        try {
            if (!sender.hasPermission("stickycommands.top"))
                return ExitCode.EXIT_PERMISSION_DENIED.setMessage(locale.translate("no-permission", variables));
    
            if (!(sender instanceof Player)) {
                sender.sendMessage(locale.translate("must-be-player", variables));
                return ExitCode.EXIT_MUST_BE_PLAYER.setMessage(locale.translate("must-be-player", variables));
            }
            
            var player = (Player) sender;
            var loc = LocationUtil.getSafeDestination(new Location(player.getWorld(), player.getLocation().getBlockX(), player.getWorld().getMaxHeight(), player.getLocation().getBlockZ(), player.getLocation().getYaw(), player.getLocation().getPitch()));
            variables.put("x", String.valueOf(loc.getX()));
            variables.put("y", String.valueOf(loc.getY()));
            variables.put("z", String.valueOf(loc.getZ()));
            variables.put("player", player.getName());
            Bukkit.getScheduler().scheduleSyncDelayedTask(StickyCommands.getInstance(), () -> player.teleport(loc), 1L);
            sender.sendMessage(locale.translate("top-message", variables));
        } catch (Exception e) {
            e.printStackTrace();
            return ExitCode.EXIT_ERROR.setMessage(locale.translate("server-error", variables));
        }

        return ExitCode.EXIT_SUCCESS;
    }
    
}
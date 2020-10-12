package com.dumbdogdiner.stickycommands.commands;

import java.util.List;
import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.utils.LocationUtil;
import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;

public class Jump extends AsyncCommand {
    private static LocaleProvider locale = Main.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();

    public Jump(Plugin owner) {
        super("jump", owner);
        setPermission("stickycommands.jump");
        setDescription("Jump to a block");
        variables.put("syntax", "/jump");
    }

    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        // TODO handle
        try {
            if (!(sender instanceof Player)) {
                sender.sendMessage(locale.translate("must-be-player", new TreeMap<String, String>()));
                return ExitCode.EXIT_SUCCESS;
            }
    
            var player = (Player) sender;
            Location loc = null;
            
            try {
                loc = LocationUtil.getSafeDestination(LocationUtil.getTarget(player));
            } catch (Exception e) {
                e.printStackTrace();
                return ExitCode.EXIT_ERROR;
            }
            
            if (loc.getBlock().getType() == Material.AIR)
                loc.setY(player.getWorld().getHighestBlockYAt(loc));
            
            loc.setYaw(player.getLocation().getYaw());
            loc.setPitch(player.getLocation().getPitch());
            loc.add(0, 1, 0);
            final Location syncLoc = loc;
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> player.teleport(syncLoc), 1L);
            variables.put("player", player.getName());
            variables.put("x", String.valueOf(loc.getX()));
            variables.put("y", String.valueOf(loc.getY()));
            variables.put("z", String.valueOf(loc.getZ()));
            
            sender.sendMessage(locale.translate("jump-message", variables));
    
            return ExitCode.EXIT_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ExitCode.EXIT_ERROR;
        }
    }

    @Override
    public void onSyntaxError(CommandSender sender, String label, String[] args) {
        
    }
    
    @Override
    public void onPermissionDenied(CommandSender sender, String label, String[] args) {
        sender.sendMessage(locale.translate("no-permission", variables));
    }

    @Override
    public void onError(CommandSender sender, String label, String[] args) {
        sender.sendMessage(locale.translate("server-error", variables));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null; // We don't want any tab complete for this command
    }
}
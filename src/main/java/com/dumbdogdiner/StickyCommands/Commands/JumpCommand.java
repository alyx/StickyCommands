package com.dumbdogdiner.StickyCommands.Commands;

import com.dumbdogdiner.StickyCommands.Utils.LocationUtil;
import com.dumbdogdiner.StickyCommands.Utils.PermissionUtil;
import com.dumbdogdiner.StickyCommands.Utils.User;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JumpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.jump", false))
            return User.PermissionDenied(sender, "stickcommands.jump");

        Player user = (Player) sender;

        Location loc = null;
        final Location cloc = user.getLocation();

        try {
            loc = LocationUtil.getTarget(user);
            loc.setYaw(cloc.getYaw());
            loc.setPitch(cloc.getPitch());
            loc.setY(loc.getY() + 1);
            if (loc.getBlock().getType() == Material.AIR)
                loc.setY(user.getWorld().getHighestBlockYAt(loc));;
            loc = LocationUtil.getSafeDestination(
                new Location(user.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), user.getLocation().getYaw(), user.getLocation().getPitch()));
        } catch (Exception ex) {
            // TODO: Add message for player
            ex.printStackTrace();
            return false; // There was an error, return
        }

        user.teleport(loc);
        
        return false;
    }
}
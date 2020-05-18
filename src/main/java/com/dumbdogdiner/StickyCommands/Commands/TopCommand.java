package com.dumbdogdiner.StickyCommands.Commands;

import com.dumbdogdiner.StickyCommands.Utils.LocationUtil;
import com.dumbdogdiner.StickyCommands.Utils.PermissionUtil;
import com.dumbdogdiner.StickyCommands.Utils.User;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "StickyCommands.top", false))
            return User.PermissionDenied(sender, "StickyCommands.top");

        Player user = (Player) sender;

        final int topX = user.getLocation().getBlockX();
        final int topZ = user.getLocation().getBlockZ();
        final float pitch = user.getLocation().getPitch();
        final float yaw = user.getLocation().getYaw();
        Location loc = null;
        try {
            loc = LocationUtil.getSafeDestination(
                    new Location(user.getWorld(), topX, user.getWorld().getMaxHeight(), topZ, yaw, pitch));
        } 
        catch (Exception e) {
            e.printStackTrace();
            // Don't finish the command
            return false;
        }
        user.teleport(loc);
        // TODO: Send message
        return false;
    }
}
package com.dumbdogdiner.StickyCommands.Utils;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.dumbdogdiner.StickyCommands.Main;

// This class just makes life simple for the permission logic...
public class PermissionUtil {

    private static Main self = Main.getPlugin(Main.class);

    /**
     * Check if the command sender has been grated the permission
     * 
     * @param sender Command Sender to check a permission against
     * @param Perm   the permission node to check
     * @param AllowConsole Allow console or not (Should only be used if the command affects the user/world)
     * @return True if the command sender has the permission
     */
    public static boolean Check(CommandSender sender, String Perm, Boolean AllowConsole) {
        // Console ALWAYS has full perms, although there are exceptions for commands like /top, which teleport the player.
        if (AllowConsole && sender instanceof ConsoleCommandSender)
            return true;

        if (sender instanceof Player) {
            Player p = (Player) sender;

            // If configured to allow ops to bypass all permission checks
            if (self.getConfig().getBoolean("general.opsBypassPermissions") && p.isOp())
                return true;

            // Otherwise check if they actually have the permission
            return p.hasPermission(Perm);
        } 
        else
            return false; // Something that isnt a player or a command sender
    }
}
package com.dumbdogdiner.StickyCommands.Commands;

import com.dumbdogdiner.StickyCommands.Utils.Messages;
import com.dumbdogdiner.StickyCommands.Utils.PermissionUtil;
import com.dumbdogdiner.StickyCommands.Utils.TranslationUtil;
import com.dumbdogdiner.StickyCommands.Utils.User;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpeedCommand implements CommandExecutor {

    // TODO: Maybe add the ability to change another players speed?
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.speed", false))
            return User.PermissionDenied(sender, "stickycommands.speed");
        
        if (!TranslationUtil.isInteger(args[0]) || args.length < 1 || args.length > 1) {
            sender.sendMessage(Messages.InvalidSyntax);
            return false;
        }
        
        Player user = (Player) sender;
        // hhhhh, this needs to be a float so it divides correctly......
        float arg = Integer.parseInt(args[0]);
        // Minecraft uses floats for the player speed, so just divide by 10.
        Float speed = arg / 10;
        if (speed > 1) {
            sender.sendMessage(Messages.InvalidSyntax);
            return false;
        }

        if (user.isFlying()) {
            user.setFlySpeed(speed);
        }
        else {
            // The default walking speed is actually high than the default fly speed
            // So we have to add 0.1 to the total, however, if they enter 10
            // the result is 1.1, which it too high, so lets check if speed is 1.0
            // if so, leave it, if not, add 0.1
            if (speed == 1.0)
                user.setWalkSpeed(speed);
            else
                user.setWalkSpeed(speed + 0.1F);
        }


        return true;
    }

}
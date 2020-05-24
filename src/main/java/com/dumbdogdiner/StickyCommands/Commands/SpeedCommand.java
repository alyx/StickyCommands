package com.dumbdogdiner.StickyCommands.Commands;

import java.util.Map;
import java.util.TreeMap;

import com.dumbdogdiner.StickyCommands.Utils.DatabaseUtil;
import com.dumbdogdiner.StickyCommands.Utils.DebugUtil;
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
        
        if (args.length < 1 || args.length > 1) {
            sender.sendMessage(Messages.invalidSyntax);
            return false;
        }

        if (!TranslationUtil.isInteger(args[0])) {
            sender.sendMessage(Messages.invalidSyntax);
            return false;
        }
        
        Player user = (Player) sender;
        // hhhhh, this needs to be a float so it divides correctly......
        float arg = Integer.parseInt(args[0]);
        // Minecraft uses floats for the player speed, so just divide by 10.
        Float speed = arg / 10;
        DebugUtil.sendDebug(speed.toString() + " < Speed for " + sender.getName(), this.getClass(), DebugUtil.getLineNumber());
        if (speed > 1) {
            sender.sendMessage(Messages.invalidSyntax);
            return false;
        }

        try {
            // Format our message.
            Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                {
                    put("player", user.getName());
                    put("speed", args[0]);
                }
            };
            DebugUtil.sendDebug("Checking if user is flying...", this.getClass(), DebugUtil.getLineNumber());
            if (user.isFlying()) {
                DebugUtil.sendDebug(sender.getName() + " is flying, setting fly speed to " + speed.toString(), this.getClass(), DebugUtil.getLineNumber());
                user.sendMessage(Messages.Translate("speedMessage", Variables));
                user.setFlySpeed(speed);
                DatabaseUtil.UpdateSpeed(speed, user.getUniqueId().toString(), "FlySpeed");
            }
            else {
                DebugUtil.sendDebug(sender.getName() + " is walking", this.getClass(), DebugUtil.getLineNumber());
                // The default walking speed is actually high than the default fly speed
                // So we have to add 0.1 to the total, however, if they enter 10
                // the result is 1.1, which it too high, so lets check if speed is 1.0
                // if so, leave it, if not, add 0.1
                if (speed == 1.0) {
                    DebugUtil.sendDebug("", this.getClass(), DebugUtil.getLineNumber());
                    user.setWalkSpeed(speed);
                    user.sendMessage(Messages.Translate("speedMessage", Variables));
                    DatabaseUtil.UpdateSpeed(speed, user.getUniqueId().toString(), "WalkSpeed");
                    DebugUtil.sendDebug("Updated " + sender.getName() + "'s speed to " + speed.toString(), this.getClass(), DebugUtil.getLineNumber());
                }
                else {
                    DebugUtil.sendDebug("message", this.getClass(), DebugUtil.getLineNumber());
                    user.setWalkSpeed(speed + 0.1F);
                    user.sendMessage(Messages.Translate("speedMessage", Variables));
                    DatabaseUtil.UpdateSpeed(speed + 0.1F, user.getUniqueId().toString(), "WalkSpeed");
                    DebugUtil.sendDebug("Updated " + sender.getName() + "'s speed to " + (speed + 0.1F), this.getClass(), DebugUtil.getLineNumber());

                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            user.sendMessage(Messages.serverError);
            return true;
        }
        return true;
    }

}
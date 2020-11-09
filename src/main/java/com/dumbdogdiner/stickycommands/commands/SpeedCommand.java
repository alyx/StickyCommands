package com.dumbdogdiner.stickycommands.commands;

import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;
import com.dumbdogdiner.stickycommands.StickyCommands;
import com.dumbdogdiner.stickycommands.User;
import com.dumbdogdiner.stickycommands.utils.Constants;
import com.dumbdogdiner.stickycommands.utils.SpeedType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class SpeedCommand extends AsyncCommand {
    LocaleProvider locale = StickyCommands.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public SpeedCommand(Plugin owner) {
        super("speed", owner);
        setPermission("stickycommands.speed");
        setDescription("Change your fly or walk speed");
        variables.put("syntax", "/speed [0-10]");
    }
    
    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player))
            return ExitCode.EXIT_PERMISSION_DENIED.setMessage(locale.translate("no-permission", variables));

        User user = StickyCommands.getInstance().getOnlineUser(((Player)sender).getUniqueId());
        Arguments a = new Arguments(args);
        a.optionalString("speed");

        if (!a.valid())
            return ExitCode.EXIT_INVALID_SYNTAX.setMessage(locale.translate("invalid-syntax", variables));
        boolean flying = ((Player)sender).isFlying(); // We save state to prevent a race condition
        float speed;
        variables.put("speed", a.exists("speed") ? a.get("speed") : "1");
        if(!a.exists("speed")){ // No argument provided, use the default
            if(flying) {
                speed = Constants.DEFAULT_FLYING_SPEED;
            } else {
                speed = Constants.DEFAULT_WALKING_SPEED;
            }
        } else if (!(a.get("speed").matches("\\d*\\.?\\d+"))) {
            return ExitCode.EXIT_INVALID_SYNTAX.setMessage(locale.translate("invalid-syntax", variables));
        } else {
            speed = Float.parseFloat(a.get("speed"));

            if (speed > 10 || speed <= 0)
                return ExitCode.EXIT_INVALID_SYNTAX.setMessage(locale.translate("invalid-syntax", variables));
            else speed /= 10f;
        }
        if (flying) {
            user.setSpeed(SpeedType.FLY, speed);
        } else {
            user.setSpeed(SpeedType.WALK, speed);
        }
        sender.sendMessage(locale.translate("speed-message", variables));
        return ExitCode.EXIT_SUCCESS;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
        if (args.length < 2) {
            return Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "0");
        }
        return List.of();
    }
}

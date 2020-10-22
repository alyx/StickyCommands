package com.dumbdogdiner.stickycommands.commands;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.SpeedType;
import com.dumbdogdiner.stickycommands.User;
import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Speed extends AsyncCommand {

    LocaleProvider locale = Main.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    public Speed(Plugin owner) {
        super("speed", owner);
        setPermission("stickycommands.speed");
        setDescription("Change your fly or walk speed");
    }
    
    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player))
            return ExitCode.EXIT_PERMISSION_DENIED.setMessage(locale.translate("no-permission", variables));

        User user = Main.getInstance().getOnlineUser(((Player)sender).getUniqueId());
        Arguments a = new Arguments(args);
        a.requiredString("speed");

        if (!a.valid())
            return onSyntaxError();

        if (!(a.get("speed").matches("\\d*\\.?\\d+")))
            return onSyntaxError();

        var speed = Float.parseFloat(a.get("speed")) / 10;
        if (speed*10> 10 || speed*10 <= 0)
            return onSyntaxError();

        if (((Player)sender).isFlying()) {
            user.setSpeed(SpeedType.FLY, speed);
        } else {
            user.setSpeed(SpeedType.WALK, speed);
        }
        variables.put("speed", a.get("speed"));
        sender.sendMessage(locale.translate("speed-message", variables));
        return ExitCode.EXIT_SUCCESS;
    }

    ExitCode onSyntaxError() {
        return onSyntaxError().setMessage(locale.translate("invalid-syntax", variables));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length < 2) {
            return Arrays.asList(new String[] {
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "10"
            });
        }
        return null;
    }
}

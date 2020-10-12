package com.dumbdogdiner.stickycommands.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Kill extends AsyncCommand {

    private LocaleProvider locale = Main.getPlugin(Main.class).getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();

    public Kill(Plugin owner) {
        super("kill", owner);
        setPermission("stickycommands.kill");
        setDescription("Kill a player, or yourself...");
        setAliases(Arrays.asList(new String[] { "slay" }));
        variables.put("syntax", "/kill [player]");
    }

    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        try {
            if (!sender.hasPermission("stickycommands.kill"))
                return ExitCode.EXIT_PERMISSION_DENIED;

            Arguments a = new Arguments(args);
            a.optionalString("target");

            Player target = null;
            variables.put("player", a.get("target"));
            variables.put("sender", sender.getName());

            if (a.get("target") == null) {
                if (sender instanceof Player) {
                    target = (Player) sender;
                    killPlayer(target, variables, "kill.suicide");
                } else
                    sender.sendMessage(locale.translate("must-be-player", variables));
                    
                return ExitCode.EXIT_SUCCESS;
            }

            target = Bukkit.getPlayer(a.get("target"));
            if (target != null) {
                killPlayer(target, variables, "kill.you-were-killed");
                sender.sendMessage(locale.translate("kill.you-killed", variables));
            } else
                sender.sendMessage(locale.translate("player-does-not-exist", variables));

            return ExitCode.EXIT_SUCCESS;
        } catch (Exception e) {
            return ExitCode.EXIT_ERROR;
        }
    }

    private void killPlayer(Player target, TreeMap<String, String> variables, String message) {
        variables.put("player", target.getName());
        target.sendMessage(locale.translate(message, variables));
        // md_5 couldn't help but throw exceptions instead of trying to actually solve the problem
        // so we have to run certain events, like kick and death, synchronously
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> target.setHealth(0), 1L);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        var usernames = new ArrayList<String>();
        if (args.length < 2) {
            for (var player : Bukkit.getOnlinePlayers())
                usernames.add(player.getName());
        }
        return usernames;
    }

    @Override
    public void onSyntaxError(CommandSender sender, String label, String[] args) {
        sender.sendMessage(locale.translate("invalid-syntax", variables));
    }

    @Override
    public void onPermissionDenied(CommandSender sender, String label, String[] args) {
        sender.sendMessage(locale.translate("no-permission", variables));
    }

    @Override
    public void onError(CommandSender sender, String label, String[] args) {
        sender.sendMessage(locale.translate("server-error", variables));
    }
}
package com.dumbdogdiner.stickycommands.commands;

import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.bukkit.player.PlayerUtils;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;
import com.dumbdogdiner.stickyapi.common.util.StringUtil;
import com.dumbdogdiner.stickycommands.StickyCommands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.TreeMap;

public class SeenCommand extends AsyncCommand {
    private static LocaleProvider locale = StickyCommands.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();
    
    public SeenCommand(Plugin owner) {
        super("seen", owner);
        setPermission("stickycommands.seen");
        setDescription("Check when a player was last online!");
        variables.put("syntax", "/seen <player>");
    }

    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player))
            return ExitCode.EXIT_PERMISSION_DENIED.setMessage(locale.translate("no-permission", variables));

        var player = (Player)sender;
        variables.put("player", player.getName());
        variables.put("player_uuid", player.getUniqueId().toString());
        Arguments a = new Arguments(args);
        a.requiredString("player");

        if (!a.valid())
            return ExitCode.EXIT_INVALID_SYNTAX.setMessage(locale.translate("invalid-syntax", variables));

        var userData = StickyCommands.getInstance().getDatabase().getUserData(a.get("player"));
        if (userData == null) {
            variables.put("bad_user", a.get("player"));
            sender.sendMessage(locale.translate("player-has-not-joined", variables));
            return ExitCode.EXIT_SUCCESS;
        }

        userData.put("player", player.getName());   
        userData.put("player_uuid", player.getUniqueId().toString());
        userData.put("ipaddress", sender.hasPermission("stickycommands.seen.ip") ? userData.get("ipaddress") : StringUtil.censorWord(userData.get("ipaddress")));
        sender.sendMessage(locale.translate("seen-message", userData));

        return ExitCode.EXIT_SUCCESS;
    }


    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
        if (args.length < 2) {
            return PlayerUtils.Names.getAllPlayers();
        }
        return List.of();
    }
}

package com.dumbdogdiner.stickycommands.commands;

import com.dumbdogdiner.stickyapi.bukkit.command.Command;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;
import com.dumbdogdiner.stickycommands.StickyCommands;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.TreeMap;

public class RulebookCommand extends Command {
    private LocaleProvider locale = StickyCommands.getInstance().getLocaleProvider();
    private TreeMap<String, String> variables = locale.newVariables();
    public static final String COMMAND_NAME = "rulebook";
    public static final String USAGE_PERMISSION = "stickycommands.rulebook";
    public RulebookCommand(Plugin owner) {
        super(COMMAND_NAME, owner);
        setPermission(USAGE_PERMISSION);
        setDescription("Get the server rulebook");
        variables.put("syntax", "/rulebook");
    }

    @Override
    public ExitCode executeCommand(CommandSender commandSender, String s, String[] strings) {
        return null;
    }

    @Override
    public @NotNull List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }
}
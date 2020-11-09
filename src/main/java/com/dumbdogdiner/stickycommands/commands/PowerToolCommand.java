package com.dumbdogdiner.stickycommands.commands;

import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;
import com.dumbdogdiner.stickyapi.common.util.ReflectionUtil;
import com.dumbdogdiner.stickycommands.StickyCommands;
import com.dumbdogdiner.stickycommands.utils.Item;
import com.dumbdogdiner.stickycommands.utils.PowerTool;
import com.google.common.base.Joiner;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class PowerToolCommand extends AsyncCommand {
    LocaleProvider locale = StickyCommands.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();

    public PowerToolCommand(Plugin owner) {
        super("powertool", owner);
        setPermission("stickycommands.powertool");
        setDescription("Bind an item to a command");
        variables.put("syntax", "/powertool [command/clear/toggle]");
    }

    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.hasPermission("stickycommands.powertool") || (!(sender instanceof Player)))
            return ExitCode.EXIT_PERMISSION_DENIED.setMessage(locale.translate("no-permission", variables));

        var player = (Player) sender;
        variables.put("player", player.getName());
        try {
            if (args.length < 1) {
                if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                    variables.put("item", player.getInventory().getItemInMainHand().getItemMeta().getDisplayName());
                    createPowerTool(player, null, true);
                    sender.sendMessage(locale.translate("powertool.cleared", variables));
                }
            } else {
                var user = StickyCommands.getInstance().getOnlineUser(player.getUniqueId());
                if (args[0].equalsIgnoreCase("toggle")) {
                    if (user.getPowerTools() == null) {
                        sender.sendMessage(locale.translate("powertool.no-powertool", variables));
                        return ExitCode.EXIT_ERROR_SILENT;
                    }

                    // TODO: move this to the user class
                    for (PowerTool pt : user.getPowerTools().values()) {
                        if (pt.getItem().getType() == player.getInventory().getItemInMainHand().getType()) {
                            pt.setEnabled(!pt.getEnabled());
                            variables.put("toggled", pt.getEnabled().toString());
                        }
                    }
                    sender.sendMessage(locale.translate("powertool.toggled", variables));
                    return ExitCode.EXIT_SUCCESS;
                }

                var s = Joiner.on(" ").join(args);
                variables.put("command", s);
                if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                    variables.put("item", player.getInventory().getItemInMainHand().getItemMeta().getDisplayName());
                    createPowerTool(player, s, false);
                    sender.sendMessage(locale.translate("powertool.assigned", variables));
                    return ExitCode.EXIT_SUCCESS;
                }
                sender.sendMessage(locale.translate("powertool.cannot-bind-air", variables));
            }
        } catch (Exception e) {
            return ExitCode.EXIT_ERROR.setMessage(locale.translate("server-error", variables));
        }
        return ExitCode.EXIT_SUCCESS;
    }

    @Override
    public @NotNull List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        var commands = new ArrayList<String>();
        if (args.length < 2) {
            commands.add("toggle");
            // I hate this, but it's the only way to my knowledge get all known commands...
            // If you know of a better way, please create a pull request with the fix
            // as I really don't like doing this bullshit. -zach
            SimpleCommandMap cmap = ReflectionUtil.getProtectedValue(StickyCommands.getInstance().getServer(), "commandMap");
            for (Command command : cmap.getCommands()) {
                // If somone didn't do permissions correctly...
                if (command.getPermission() == null)
                    continue;

                // If they don't have the permission, why would we show them the command?
                if (sender.hasPermission(command.getPermission()))
                    commands.add(command.getName());
            }
        }
        return commands;
    }

    private void createPowerTool(Player player, String command, boolean clear) {
        try {
            ItemStack is = player.getInventory().getItemInMainHand();
            var user = StickyCommands.getInstance().getOnlineUser(player.getUniqueId());
            if (clear) {
                user.removePowerTool(new Item(is));
            }
            else
                user.addPowerTool(new PowerTool(new Item(is), command, player));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
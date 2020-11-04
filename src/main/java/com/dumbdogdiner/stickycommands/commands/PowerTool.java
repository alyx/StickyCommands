package com.dumbdogdiner.stickycommands.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.StickyCommands;
import com.google.common.base.Joiner;
import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

//TODO: add a /powertooltoggle to this!
public class PowerTool extends AsyncCommand {
    LocaleProvider locale = StickyCommands.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();

    public PowerTool(Plugin owner) {
        super("powertool", owner);
        setPermission("stickycommands.powertool");
        setDescription("Bind an item to a command");
        variables.put("syntax", "/powertool [command/clear]");
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
                    getPowerTool(player, null, true);
                    sender.sendMessage(locale.translate("powertool.cleared", variables));
                }
            } else {
                var s = Joiner.on(" ").join(args);
                variables.put("command", s);
                if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                    variables.put("item", player.getInventory().getItemInMainHand().getItemMeta().getDisplayName());
                    getPowerTool(player, s, false);
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

    private ItemStack getPowerTool(Player player, String command, boolean clear) {
        ItemStack is = player.getInventory().getItemInMainHand();
        ItemMeta meta = is.getItemMeta();
        List<String> lore = new ArrayList<String>();
        if (clear)
            lore.clear();
        else
            lore.add("command:" + command);
        meta.setLore(lore);
        is.setItemMeta(meta);
        return is;
    }
}
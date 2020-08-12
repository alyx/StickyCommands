package com.dumbdogdiner.stickycommands.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.utils.Messages;
import com.dumbdogdiner.stickycommands.utils.PermissionUtil;
import com.dumbdogdiner.stickycommands.utils.User;
import com.google.common.base.Joiner;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PowertoolCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String lable, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.powertool", false))
            return User.PermissionDenied(sender, "stickycommands.powertool");
        Player player = (Player) sender;
        try {
            if (args.length < 1) {
                Map<String, String> Vars = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                    {
                        put("player", sender.getName());
                    }
                };
                if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                    ItemStack is = player.getInventory().getItemInMainHand();
                    ItemMeta meta = is.getItemMeta();
                    List<String> lore = new ArrayList<String>();
                    lore.clear();
                    meta.setLore(lore);
                    is.setItemMeta(meta);
                    sender.sendMessage(Messages.Translate("powertool.cleared", Vars));
                }
            }
            else {
                String s = Joiner.on(" ").join(args);
                Map<String, String> Vars = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                    {
                        put("player", sender.getName());
                        put("command", s);
                    }
                };
                if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                    ItemStack is = player.getInventory().getItemInMainHand();
                    ItemMeta meta = is.getItemMeta();
                    List<String> lore = new ArrayList<String>();
                    lore.add("command:"+s);
                    meta.setLore(lore);
                    is.setItemMeta(meta);
                    sender.sendMessage(Messages.Translate("powertool.assigned", Vars));
                    return true;
                }
                sender.sendMessage("You cannot bind air to a command!");
            }
        }
        catch (InvalidConfigurationException e) {
            e.printStackTrace();
            sender.sendMessage(Messages.serverError);
            return true;
        }
        return true;
    }

}
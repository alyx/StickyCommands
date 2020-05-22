package com.dumbdogdiner.StickyCommands.Commands;

import java.util.Map;
import java.util.TreeMap;

import com.dumbdogdiner.StickyCommands.Utils.Item;
import com.dumbdogdiner.StickyCommands.Utils.Messages;
import com.dumbdogdiner.StickyCommands.Utils.PermissionUtil;
import com.dumbdogdiner.StickyCommands.Utils.User;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WorthCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.worth", false))
            return User.PermissionDenied(sender, "stickycommands.worth");

        Player player = (Player) sender;

        if (args.length < 1) {
            ItemStack is = player.getInventory().getItemInMainHand();
            ItemStack[] invent = player.getInventory().getContents();
            String iss = is.getType().toString().replace("_", " ").toLowerCase();
            double isd = Item.getItem(is.getType().toString().replace("_", "").toLowerCase());
            int isa = 0;
            for (ItemStack s : invent) {
                if (s != null) {
                    if (s.getType() == is.getType())
                        isa = isa + s.getAmount();
                }
            }
            final int javaisdumb = isa;
            Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                {
                    put("player", player.getName());
                    put("singleworth", Double.toString(isd));
                    put("handworth", Double.toString(isd * is.getAmount()));
                    put("inventworth", String.valueOf(isd * javaisdumb));
                    put("item", iss);
                }
            };

            if (isd == 0.0) {
                try {
                    sender.sendMessage(Messages.Translate("cannotsell", Variables));
                    return false;
                } catch (InvalidConfigurationException e) {
                    sender.sendMessage(Messages.serverError);
                    e.printStackTrace();
                    return false;
                }
            }

            try {
                sender.sendMessage(Messages.Translate("worthMessage", Variables));
                return true;
            } catch (InvalidConfigurationException e) {
                sender.sendMessage(Messages.serverError);
                e.printStackTrace();
            }
            return true;
        }
        return true;
    }
}
package com.dumbdogdiner.stickycommands.commands;

import java.util.Map;
import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.utils.InventoryWorkaround;
import com.dumbdogdiner.stickycommands.utils.Messages;
import com.dumbdogdiner.stickycommands.utils.PermissionUtil;
import com.dumbdogdiner.stickycommands.utils.User;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class HatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.hat", false))
            return User.PermissionDenied(sender, "stickycommands.hat");

        Player player = (Player) sender;

        final PlayerInventory inv = player.getInventory();
        final ItemStack head = inv.getHelmet();
        Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {{put("player", player.getName());}};
        try {
            if (args.length > 0 && (args[0].equalsIgnoreCase("rem") || args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("0"))) {
                if (head == null || head.getType() == Material.AIR) {
                    sender.sendMessage(Messages.Translate("hat.noHat", Variables));
                    return true;
                } 
                else {
                    final ItemStack air = new ItemStack(Material.AIR);
                    InventoryWorkaround.addItems(player.getInventory(), head);
                    inv.setHelmet(air);
                    sender.sendMessage(Messages.Translate("hat.removedHat", Variables));
                    return true;
                }
            } 
            else {
                final ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand != null && hand.getType() != Material.AIR) {
                    final PlayerInventory inv2 = player.getInventory();
                    final ItemStack head2 = inv2.getHelmet();
                    inv.setHelmet(hand);
                    InventoryWorkaround.setItemInMainHand(player, head2);
                    //inv.setItemInMainHand(head2);
                    sender.sendMessage(Messages.Translate("hat.newHat", Variables));
                    return true;
                }
                else if (hand == null || hand.getType() == Material.AIR) {
                    if (head == null || head.getType() == Material.AIR) {
                        sender.sendMessage(Messages.Translate("hat.noHat", Variables));
                        return true;
                    }
                    final PlayerInventory inv2 = player.getInventory();
                    final ItemStack head2 = inv2.getHelmet();
                    inv.setHelmet(hand);
                    InventoryWorkaround.setItemInMainHand(player, head2);
                    //inv.setItemInMainHand(head2);
                    sender.sendMessage(Messages.Translate("hat.removedHat", Variables));
                    return true;
                }
            }
        }
        catch (InvalidConfigurationException e) {
            e.printStackTrace();
            sender.sendMessage(Messages.serverError);
            return false;
        }
        return true;
    }

}
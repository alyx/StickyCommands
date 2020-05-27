package com.dumbdogdiner.StickyCommands.Commands;

import java.util.Map;
import java.util.TreeMap;

import com.dumbdogdiner.StickyCommands.Main;
import com.dumbdogdiner.StickyCommands.Utils.Item;
import com.dumbdogdiner.StickyCommands.Utils.Messages;
import com.dumbdogdiner.StickyCommands.Utils.PermissionUtil;
import com.dumbdogdiner.StickyCommands.Utils.User;

import net.minecraft.server.v1_15_R1.NBTTagCompound;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.sell", false))
            return User.PermissionDenied(sender, "stickycommands.sell");

        Player player = (Player) sender;

        if (args.length < 1)
            return User.invalidSyntax(sender);

        ItemStack is = player.getInventory().getItemInMainHand();
        ItemStack[] invent = player.getInventory().getContents();
        String iss = is.getType().toString().replace("_", " ").toLowerCase();
        // If the item does not exist in worth.yml. Do not allow selling. Defaults to 0 (not sellable).
        double isd = Item.getItem(is.getType().toString().replace("_", "").toLowerCase()); 
        
        // ----- Check NBT Data -----
        
        // Create a *Minecraft Server* ItemStack from the Bukkit one.
        net.minecraft.server.v1_15_R1.ItemStack nmsis = CraftItemStack.asNMSCopy(is);
        
        // Create an NBTTagCompound to access raw NBT data.
        NBTTagCompound isCompound = (nmsis.hasTag()) ? nmsis.getTag() : new NBTTagCompound();
        
        // Defaults to false (?) - check required
        // If the item was marked as not sellable, set the price to 0.0 to prevent selling.
        isd = (isCompound.getBoolean("notsellable")) ? 0.0 : isd;
        
        // ----- Check NBT Data END -----
        
        int isa = 0;
        for (ItemStack s : invent) {
            if (s != null) {
                if (s.getType() == is.getType())
                    isa = isa + s.getAmount();
            }
        }
        // "Fucking Java" - DDD Staff 2020
        final int isaFinal = isa;
        final double isdFinal = isd;
        try {
            Map<String, String> var = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                {
                    put("player", player.getName());
                    put("item", iss);
                }
            };

            if (isd == 0.0) {
                sender.sendMessage(Messages.Translate("cannotsell", var));
                return false;
            }

            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("hand")) {
                    if (!PermissionUtil.Check(sender, "stickycommands.sell.hand", false))
                        return User.PermissionDenied(sender, "stickycommands.sell.hand");
                    Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                        {
                            put("player", player.getName());
                            put("amount", String.valueOf(is.getAmount()));
                            put("worth", String.valueOf(is.getAmount() * isdFinal));
                            put("item", iss);
                        }
                    };
                    Main.getEconomy().depositPlayer(player, is.getAmount() * isd);
                    player.sendMessage(Messages.Translate("sellMessage", Variables));
                    player.getInventory().getItemInMainHand().setAmount(0);
                    return true;
                }
                if ((args[0].equalsIgnoreCase("inventory") || args[0].equalsIgnoreCase("inv")
                        || args[0].equalsIgnoreCase("invent"))
                        || args.length > 1 && args[0].equalsIgnoreCase("all") && args[1].equalsIgnoreCase("hand")) {
                    if (!PermissionUtil.Check(sender, "stickycommands.sell.inventory", false))
                        return User.PermissionDenied(sender, "stickycommands.sell.inventory");
                    Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                        {
                            put("player", player.getName());
                            put("amount", String.valueOf(isaFinal));
                            put("worth", String.valueOf(isdFinal * isaFinal));
                            put("item", iss);
                        }
                    };
                    Main.getEconomy().depositPlayer(player, isd * isaFinal);
                    player.sendMessage(Messages.Translate("sellMessage", Variables));
                    consumeItem(player, isaFinal, is.getType());
                    return true;
                }
                if (args[0].equalsIgnoreCase("all")) {
                    sender.sendMessage(Messages.prefix + ChatColor.RED + "Sorry, this hasn't been added yet!");
                    return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(Messages.serverError);
            return false;
        }

        return true;
    }

    public boolean consumeItem(Player player, int count, Material mat) {
        ItemStack[] item = player.getInventory().getContents();

        for (ItemStack s : item) {
            if (s != null) {
                if (s.getType() == mat) {
                    s.setAmount(0);
                }
            }
        }

        player.updateInventory();
        return true;
    }
}
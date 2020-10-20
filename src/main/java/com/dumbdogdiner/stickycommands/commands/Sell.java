package com.dumbdogdiner.stickycommands.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.Sale;
import com.dumbdogdiner.stickycommands.utils.Database;
import com.dumbdogdiner.stickycommands.utils.Item;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;
import com.dumbdogdiner.stickyapi.common.util.NumberUtil;
import com.dumbdogdiner.stickyapi.common.util.Paginator;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class Sell extends AsyncCommand {
    static Main self = Main.getInstance();
    LocaleProvider locale = Main.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();

    public Sell(Plugin owner) {
        super("sell", owner);
        setDescription("Check the worth of an item.");
        setPermission("stickycommands.worth");
        variables.put("syntax", "/worth");
    }

    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.hasPermission("stickycommands.sell") || (!(sender instanceof Player)))
            return ExitCode.EXIT_PERMISSION_DENIED;

        Arguments a = new Arguments(args);
        a.optionalString("sellMode");

        var player = (Player) sender;
        variables.put("player", player.getName());
        variables.put("uuid", player.getUniqueId().toString());
        // Find a better way to do this.
        if (a.get("sellMode") != null && a.get("sellMode").equalsIgnoreCase("log")) {
            if (!sender.hasPermission("stickycommands.sell.log"))
                return ExitCode.EXIT_PERMISSION_DENIED;
            return handleLog(player, new Arguments(Arrays.copyOfRange(args, 1, args.length))); // We no longer need the first
            // argument, just everything
            // after it!
        }

        var item = new Item(player.getInventory().getItemInMainHand());
        ItemStack[] inventory = player.getInventory().getContents();
        variables.put("item", item.getName());

        if (item.getAsItemStack().getType() == Material.AIR) {
            sender.sendMessage(locale.translate("sell.cannot-sell", variables));
            return ExitCode.EXIT_SUCCESS;
        }

        var inventoryAmount = 0;
        for (var is : inventory) {
            if (is != null && is.getType() == item.getType() && is != item.getAsItemStack()) {
                inventoryAmount += is.getAmount();
            }
        }
        var worth = item.getWorth();
        double percentage = 100.00;
        if (item.hasDurability()) {
            double maxDur = item.getAsItemStack().getType().getMaxDurability();
            double currDur = maxDur - item.getAsItemStack().getDurability(); // TODO Change to use Damagables
            percentage = Math.round((currDur / maxDur) * 100.00) / 100.00;

            if ((currDur / maxDur) < 0.4) {
                worth = 0.0;
            } else {
                worth = Math.round((worth * percentage) * 100.00) / 100.00;
            }

        }
        variables.put("single_worth", Double.toString(worth));
        variables.put("hand_worth", Double.toString(worth * item.getAmount()));
        variables.put("inventory_worth", Double.toString(worth * inventoryAmount));

        if (worth > 0.0) {
            switch (a.get("sellMode") == null ? "" : a.get("sellMode").toLowerCase()) {
                case "hand":
                case "":
                    variables.put("amount", String.valueOf(item.getAmount()));
                    variables.put("worth", String.valueOf(item.getWorth() * item.getAmount()));
                    player.sendMessage(locale.translate("sell.sell-message", variables));
                    item.sell(player, false, variables, item.getAmount());
                    return ExitCode.EXIT_SUCCESS;
                case "inventory":
                case "invent":
                case "inv":
                    variables.put("amount", String.valueOf(inventoryAmount));
                    variables.put("worth", Item.getDecimalFormat().format(item.getWorth() * inventoryAmount));
                    player.sendMessage(locale.translate("sell.sell-message", variables));
                    item.sell(player, true, variables, inventoryAmount);
                    return ExitCode.EXIT_SUCCESS;
                default:
                    return ExitCode.EXIT_INVALID_SYNTAX;
            }
        } else if (worth == 0.0) {
            sender.sendMessage(locale.translate("sell.cannot-sell", variables));
            return ExitCode.EXIT_SUCCESS;
        } else {
            sender.sendMessage(locale.translate("sell.bad-worth", variables)); // todo: set a locale for this
            return ExitCode.EXIT_ERROR;
        }
        // SHOULD NOT REACH HERE
    }

    // TODO: Allow for specifying player
    /**
     * Handles the log sub command!
     */
    ExitCode handleLog(Player sender, Arguments a) {
        a.optionalString("page");
        if (!a.valid())
            return ExitCode.EXIT_INVALID_SYNTAX;

        // I hate and love this.
        Integer page = a.get("page") == null ? 1 : (NumberUtil.isNumeric(a.get("page")) ? Integer.parseInt(a.get("page")) : 1);

        Database database = Main.getInstance().getDatabase();

        var salesList = database.getSaleLog();
        ArrayList<String> sales = new ArrayList<String>();
        for (Sale sale : salesList) {
            variables.put("saleid", sale.getOrderId().toString());
            variables.put("item", sale.getItem().getName());
            variables.put("item_actual_name", sale.getItem().getType().toString());
            variables.put("amount", sale.getAmount().toString());
            variables.put("price", sale.getPrice().toString());
            variables.put("new_balance", sale.getNewBalance().toString());
            variables.put("old_balance", sale.getOldBalance().toString());
            variables.put("date", sale.getDate().toString());
            sales.add(locale.translate("sell.log.log", variables));
        }

        Paginator<String> pages = new Paginator<String>(sales, 8);
        if (pages.getTotalPages() < 1 || page > pages.getTotalPages()) {
            sender.sendMessage(locale.translate("sell.log.no-sales", variables));
            return ExitCode.EXIT_SUCCESS;
        }

        // Let's send this is one giant message, just incase it manages to
        // get cut off by other messages!
        var logMessage = "";
        for (String log : pages.getPage(page))
            logMessage += log;

        variables.put("current", String.valueOf(pages.getCurrent()));
        variables.put("total", String.valueOf(pages.getTotalPages()));

        sender.sendMessage(locale.translate("sell.log.log-message", variables));
        sender.sendMessage(logMessage);
        sender.sendMessage(locale.translate("sell.log.paginator", variables));

        return ExitCode.EXIT_SUCCESS;
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

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }
}
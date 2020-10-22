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
        setDescription("Sell an item.");
        setPermission("stickycommands.sell");
        variables.put("syntax", "/sell");
    }

    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.hasPermission("stickycommands.sell") || (!(sender instanceof Player)))
            return ExitCode.EXIT_PERMISSION_DENIED.setMessage(locale.translate("no-permission", variables));

        Arguments a = new Arguments(args);
        a.optionalString("sellMode");
        a.optionalString("confirm");

        var player = (Player) sender;
        variables.put("player", player.getName());
        variables.put("uuid", player.getUniqueId().toString());
        // TODO: Find a better way to do this.
        if (a.get("sellMode") != null && a.get("sellMode").equalsIgnoreCase("log")) {
            if (!sender.hasPermission("stickycommands.sell.log"))
                return ExitCode.EXIT_PERMISSION_DENIED.setMessage(locale.translate("no-permission", variables));
            return handleLog(player, new Arguments(Arrays.copyOfRange(args, 1, args.length))); // We no longer need the
                                                                                               // first
                                                                                               // argument, just
                                                                                               // everything
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
            switch (a.get("sellMode") == null ? "hand" : a.get("sellMode").toLowerCase()) {
                case "hand":
                case "confirm":
                case "":
                    // I don't like this, but it works so whatever...
                    // TODO: discomfuckulate this shit
                    if (a.exists("confirm") ? a.get("confirm").equalsIgnoreCase("confirm")
                            : a.exists("sellMode") ? a.get("sellMode").equalsIgnoreCase("confirm") : false) {
                        variables.put("amount", String.valueOf(item.getAmount()));
                        variables.put("worth", String.valueOf(item.getWorth() * item.getAmount()));
                        player.sendMessage(locale.translate("sell.sell-message", variables));
                        item.sell(player, false, variables, item.getAmount());
                        return ExitCode.EXIT_SUCCESS;
                    }
                    sender.sendMessage(locale.translate("sell.must-confirm", variables));
                    return ExitCode.EXIT_SUCCESS;
                case "inventory":
                case "invent":
                case "inv":
                    if (a.exists("confirm") && a.get("confirm").equalsIgnoreCase("confirm")) {
                        variables.put("amount", String.valueOf(inventoryAmount));
                        variables.put("worth", Item.getDecimalFormat().format(item.getWorth() * inventoryAmount));
                        player.sendMessage(locale.translate("sell.sell-message", variables));
                        item.sell(player, true, variables, inventoryAmount);
                        return ExitCode.EXIT_SUCCESS;
                    }
                    sender.sendMessage(locale.translate("sell.must-confirm", variables));
                    return ExitCode.EXIT_SUCCESS;
                default:
                    return ExitCode.EXIT_INVALID_SYNTAX.setMessage(locale.translate("invalid-syntax", variables));
            }
        } else if (worth == 0.0) {
            sender.sendMessage(locale.translate("sell.cannot-sell", variables));
            return ExitCode.EXIT_SUCCESS;
        } else {
            sender.sendMessage(locale.translate("sell.bad-worth", variables));
            return ExitCode.EXIT_ERROR.setMessage(locale.translate("server-error", variables));
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
            return ExitCode.EXIT_INVALID_SYNTAX.setMessage(locale.translate("invalid-syntax", variables));

        // I hate and love this.
        Integer page = a.get("page") == null ? 1
                : (NumberUtil.isNumeric(a.get("page")) ? Integer.parseInt(a.get("page")) : 1);
        Database database = Main.getInstance().getDatabase();
        var salesList = database.getSaleLog(page);

        ArrayList<String> sales = new ArrayList<String>();
        for (Sale sale : salesList) {
            variables.put("log_player", sale.getUsername());
            variables.put("saleid", sale.getOrderId().toString());
            variables.put("item", sale.getItem().getName());
            variables.put("item_enum", sale.getItem().getType().toString());
            variables.put("amount", sale.getAmount().toString());
            variables.put("price", sale.getPrice().toString());
            variables.put("new_balance", sale.getNewBalance().toString());
            variables.put("old_balance", sale.getOldBalance().toString());
            variables.put("date", sale.getDate().toString());
            sales.add(locale.translate("sell.log.log", variables));
        }

        Integer totalPages = database.getSaleLogSize() / 8;
        if (sales.size() < 1 || page > totalPages) {
            sender.sendMessage(locale.translate("sell.log.no-sales", variables));
            return ExitCode.EXIT_SUCCESS;
        }

        // Let's send this is one giant message, just incase it manages to
        // get cut off by other messages!
        var logMessage = "";
        for (String log : sales)
            logMessage += log;

        variables.put("current", String.valueOf(page));
        variables.put("total", String.valueOf(totalPages));

        sender.sendMessage(locale.translate("sell.log.log-message", variables));
        sender.sendMessage(logMessage);
        sender.sendMessage(locale.translate("sell.log.paginator", variables));

        return ExitCode.EXIT_SUCCESS;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length < 2) {
            return Arrays.asList(new String[] { "hand", "inventory",
                    (sender.hasPermission("stickycommands.sell.log") ? "log" : "") });
        } else if (args.length == 2) {
            return Arrays.asList(new String[] { "confirm" });
        }
        return null;
    }
}
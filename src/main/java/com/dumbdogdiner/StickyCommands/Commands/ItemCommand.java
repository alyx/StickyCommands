package com.dumbdogdiner.StickyCommands.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dumbdogdiner.StickyCommands.Utils.InventoryWorkaround;
import com.dumbdogdiner.StickyCommands.Utils.Messages;
import com.dumbdogdiner.StickyCommands.Utils.PermissionUtil;
import com.dumbdogdiner.StickyCommands.Utils.TranslationUtil;
import com.dumbdogdiner.StickyCommands.Utils.User;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemCommand implements CommandExecutor, TabCompleter {

    // Syntax /item <item> [amount]
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.item", false))
            return User.PermissionDenied(sender, "stickycommands.item");

        Player player = (Player) sender;
        PlayerInventory inv = player.getInventory();

        if (args.length < 1)
            return User.invalidSyntax(sender);

        Material mat = Material.matchMaterial(args[0]);
        int arg = 1;
        if (args.length > 1 && TranslationUtil.isInteger(args[1]))
            arg = Integer.valueOf(args[1]);

        if (mat == null || mat.isAir())
            return User.invalidSyntax(sender);

        ItemStack is = new ItemStack(mat, arg); // uuw lmao thanks. Sometimes i'm retarded

        String itemName = null;
        if (args.length > 2) {
            String[] metaAction = args[2].split(":");
            if (metaAction[0].equalsIgnoreCase("name"))
                itemName = ChatColor.translateAlternateColorCodes('&', metaAction[1].replace("_", ""));
        }

        ItemMeta isMeta = is.getItemMeta();
        if (itemName != null)
            isMeta.setDisplayName(itemName);

        is.setItemMeta(isMeta);

        is.addEnchantments(getEnchants(args));

        //lmao I love this
        // Java is dumb, and our int MUST be final to go into this hashmap....
        final String javaisfuckingdumb = String.valueOf(arg);
        // So we need to create a hasmap for the config nodes.
        Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
            {
                // Lets add our nodes to replace...
                put("player", player.getName());
                put("item", WordUtils.capitalizeFully(is.getType().toString().replace("_", " ")));
                put("amount", String.valueOf(javaisfuckingdumb)); // bro, really? This has to be final, fucking dumb language
            }
        };
        // Now lets just send a message!
        try {
            // Messages.Translate is some black magic fuckery, don't ask, just use.
            sender.sendMessage(Messages.Translate("itemMessage", Variables));
        }
        catch (InvalidConfigurationException e) {

        }
        
        
        InventoryWorkaround.addItems(inv, is); // this needs an item stack btw

        return false;
    }

    HashMap<Enchantment, Integer> getEnchants(String... commandArgument) {
        HashMap<Enchantment, Integer> enchMap = new HashMap<>();
        for(String arg : commandArgument) {
            String[] metaCheck = arg.split(":");
            if(metaCheck.length < 1) continue;
            if(metaCheck[0].equalsIgnoreCase("enchantment")) {
                if(metaCheck.length < 3) continue;
                String enchString = metaCheck[1];
                Enchantment enchObj = Enchantment.getByKey(NamespacedKey.minecraft(enchString)); // I hate this, but it should work uuw
                if(enchObj == null) continue;

                int enchLevel = 1;
                if(metaCheck[2] != null) {
                    if(!TranslationUtil.isInteger(metaCheck[2])) continue;
                    enchLevel = Integer.parseInt(metaCheck[2]);
                }

                enchMap.put(enchObj, enchLevel);
            }
        }
        return enchMap;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // includes all aliases dw
        List<String> options = new ArrayList<String>();
        if (command.getName().equalsIgnoreCase("item")) {

            if (args.length == 1) {
                options = Stream.of(Material.values()).map(Material::name).collect(Collectors.toList());
                // thx spigotmc.org :)
            }
        }

        return (List<String>) options;
    }



}
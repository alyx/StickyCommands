package com.dumbdogdiner.StickyCommands.Commands;

import java.util.ArrayList;
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
import org.bukkit.Bukkit;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

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

        is = applyMetaTags(is, args);

        //lmao I love this
        // Java is dumb, and our int MUST be final to go into this hashmap....
        final String javaisfuckingdumb = String.valueOf(arg);
        final String ihatejava = is.getType().toString().replace("_", " ");
        // So we need to create a hasmap for the config nodes.
        Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
            {
                // Lets add our nodes to replace...
                put("player", player.getName());
                put("item", WordUtils.capitalizeFully(ihatejava));
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

    ItemStack applyMetaTags(ItemStack is, String... commandArgument) {
        ItemMeta isMeta = is.getItemMeta();
        HashMap<Enchantment, Integer> enchMap = new HashMap<>();

        for(String arg : commandArgument) {
            String[] metaCheck = arg.split(":");
            if(metaCheck.length < 1) continue;

            switch(metaCheck[0]) {
                case "name":
                    if(metaCheck.length < 2) continue;
                    String itemName = metaCheck[1].replace("_", " ");
                    itemName = ChatColor.translateAlternateColorCodes('&', itemName);

                    isMeta.setDisplayName(itemName);
                    continue;
                case "enchant":
                case "enchantment":
                    if(metaCheck.length < 3) continue;
                    String enchString = metaCheck[1];
                    Enchantment enchObj = Enchantment.getByKey(NamespacedKey.minecraft(enchString));
                    if(enchObj == null) continue;

                    int enchLevel = 1;
                    if(metaCheck[2] != null) {
                        if(!TranslationUtil.isInteger(metaCheck[2])) continue;
                        enchLevel = Integer.parseInt(metaCheck[2]);
                    }

                    enchMap.put(enchObj, enchLevel);
                    continue;
                case "lore":
                    if(metaCheck.length < 2) continue;
                    String[] lore = metaCheck[1].split(",");
                    ArrayList<String> itemLore = new ArrayList<>();
                    for(String loreString : lore) {
                        loreString = ChatColor.translateAlternateColorCodes('&', loreString.replace("_", " ")); 
                        itemLore.add(loreString);
                    }
                    isMeta.setLore(itemLore);
                    continue;
                case "hideflags":
                    if(metaCheck.length < 2) continue;
                    boolean bool = Boolean.valueOf(metaCheck[1]);
                    if(bool) isMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    continue;
                case "owner":
                    if(metaCheck.length < 2) continue;
                    if(!is.getType().equals(Material.PLAYER_HEAD)) continue;
                    SkullMeta isSkullMeta = (SkullMeta) isMeta;
                    if(Bukkit.getOfflinePlayer(metaCheck[1]) == null) continue;
                    isSkullMeta.setOwner(metaCheck[1]);
                    isMeta = (ItemMeta) isSkullMeta;
            }
        }

        is.setItemMeta(isMeta);
        is.addUnsafeEnchantments(enchMap);
        return is;
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

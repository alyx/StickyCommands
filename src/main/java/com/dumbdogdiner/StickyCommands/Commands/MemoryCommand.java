package com.dumbdogdiner.StickyCommands.Commands;

import java.util.Map;
import java.util.TreeMap;

import com.dumbdogdiner.StickyCommands.Utils.Messages;
import com.dumbdogdiner.StickyCommands.Utils.PermissionUtil;
import com.dumbdogdiner.StickyCommands.Utils.User;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_16_R1.MinecraftServer;

public class MemoryCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtil.Check(sender, "stickycommands.memory", false))
            return User.invalidSyntax(sender);

        Player player = (Player) sender;

        double[] tpsd = MinecraftServer.getServer().recentTps;
        double tps = (double) Math.round(tpsd[0] * 100) / 100;
        long maxmem = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        long totalmem = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long freemem = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long usedmem = Runtime.getRuntime().totalMemory() / 1024 / 1024 - Runtime.getRuntime().freeMemory() / 1024 / 1024;

        Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
            {
                // Lets add our nodes to replace...
                put("player", sender.getName());
                put("maxmem", String.valueOf(maxmem));
                put("totalmem", String.valueOf(totalmem));
                put("freemem", String.valueOf(freemem));
                put("usedmem", String.valueOf(usedmem));
                put("tps", String.valueOf(tps));
                put("entities", String.valueOf(player.getWorld().getEntities().size()));
                put("chunks", String.valueOf(player.getWorld().getLoadedChunks().length));
                put("world", player.getWorld().getName());
            }
        };

        try {
            sender.sendMessage(Messages.Translate("memoryMessage", Variables));
        } 
        catch (InvalidConfigurationException e) {
            e.printStackTrace();
            sender.sendMessage(Messages.serverError);
        }

        return true;
    }

}
package com.dumbdogdiner.StickyCommands.Utils;

import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import com.dumbdogdiner.StickyCommands.Main;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

public class User {

    static Main self = Main.getPlugin(Main.class);
    private final Player player;
    private boolean afk = false;

    public User(Player player) {
        this.player = player;
    }

    public boolean isAfk() {
        return this.afk;
    }

    public void setAfk(boolean afk) {
        this.afk = afk;
    }

    /**
     * Send the player a permission denied message
     * 
     * @param sender         the person who is executing the command
     * @param PermissionNode The permission node they're being denied for
     * @return always true, for use in the command classes.
     */
    public static boolean PermissionDenied(CommandSender sender, String PermissionNode) {
        try {
            sender.sendMessage(
                    Messages.Translate("noPermission", new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                        {
                            put("player", sender.getName());
                            put("permission", PermissionNode);
                        }
                    }));
        } catch (InvalidConfigurationException ex) {
            ex.printStackTrace();
            sender.sendMessage("Permission Denied!");
        }
        return true;
    }

    /**
     * Send the player an invalid syntax message
     * 
     * @param sender the person who is executing the command
     * @return always false, for use in the command classes.
     */
    public static boolean invalidSyntax(CommandSender sender) {
        sender.sendMessage(Messages.invalidSyntax);
        return false;
    }

    public static CompletableFuture<String> getServer(String player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        Main.serverName = new CompletableFuture<>();

        try {
            out.writeUTF("GetServer");
            out.writeUTF(player);
            self.getServer().sendPluginMessage(self, "BungeeCord", out.toByteArray());
        } catch (Exception e) {
            // Ensure if an error happened, any calls to .get() will throw this exception
            // and
            // the value is unusable.
            Main.serverName.completeExceptionally(e);
            e.printStackTrace();
        }

        return Main.serverName;
    }
}
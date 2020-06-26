package com.dumbdogdiner.StickyCommands.Utils;

import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import com.dumbdogdiner.StickyCommands.Main;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_16_R1.PacketPlayOutAnimation;
import net.minecraft.server.v1_16_R1.TileEntityShulkerBox.AnimationPhase;

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
     * Send the invalid player message
     * 
     * @param player        the player to lookup
     * @return always true, for use in the command classes.
     */
    public static boolean invalidPlayer(CommandSender sender, String player) {
        try {
            sender.sendMessage(
                    Messages.Translate("playerDoesNotExist", new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                        {
                            put("player", player);
                        }
                    }));
        } catch (InvalidConfigurationException ex) {
            ex.printStackTrace();
            sender.sendMessage(Messages.serverError);
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

    public static void whipPlayer(CraftPlayer player, int times, double damage) {
        Random rand = new Random();
        PacketPlayOutAnimation animation = new PacketPlayOutAnimation(player.getHandle(), 1);
        
        int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(self, new Runnable() {
            @Override
            public void run() {
                player.getHandle().playerConnection.sendPacket(animation);
                Float f = rand.nextFloat() * (4.0F - 3.0F) + 1.0F;
                Vector direction = new Vector();
                direction.setX(0.0D + Math.random() - Math.random());
                direction.setY(Math.random());
                direction.setZ(0.0D + Math.random() - Math.random());
                Vector v = direction.multiply(f).setY(0.4F);
                player.damage(damage);
                player.setVelocity(v);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 1.0F, 1.0F);
                
            }
        }, 0L, 10L);

        Bukkit.getScheduler().runTaskLater(self, new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().cancelTask(task);
            }
        }, times * 10);
    }
}
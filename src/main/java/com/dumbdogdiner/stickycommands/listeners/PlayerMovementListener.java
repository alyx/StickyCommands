package com.dumbdogdiner.stickycommands.listeners;

import java.util.Map;
import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.utils.DebugUtil;
import com.dumbdogdiner.stickycommands.utils.Messages;
import com.dumbdogdiner.stickycommands.utils.User;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMovementListener implements Listener {
    @EventHandler
    public void onMove(PlayerMoveEvent event) throws InvalidConfigurationException {
        Player player = event.getPlayer();
        User u = Main.USERS.get(player.getUniqueId());
        if (u == null) {
            Main.USERS.put(player.getUniqueId(), new User(player));
            u = Main.USERS.get(player.getUniqueId());
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        double x = Math.floor(from.getX());
        double z = Math.floor(from.getZ());
        double y = Math.floor(from.getY());
        if (Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z || Math.floor(to.getY()) != y) {
            if (u.isAfk()) {
                Map<String, String> Variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                    {
                        put("player", player.getName());
                    }
                };
                DebugUtil.sendDebug("Setting AFK mode to false for " + player.getName(), this.getClass(), DebugUtil.getLineNumber());
                u.setAfk(false);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(Messages.Translate("notAfkMessage", Variables));
                }
            }
        }
    }

}
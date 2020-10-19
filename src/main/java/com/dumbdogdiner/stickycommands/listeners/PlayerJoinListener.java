package com.dumbdogdiner.stickycommands.listeners;

import java.sql.Timestamp;

import com.dumbdogdiner.stickyapi.common.util.TimeUtil;
import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.User;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles logic relating to players joining and leaving the server.
 */
public class PlayerJoinListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        Main.getInstance().getOnlineUserCache().put(User.fromPlayer(player));
        Main.getInstance().getDatabase().updateUser(player.getUniqueId().toString(), player.getName(), player.getAddress().getAddress().getHostAddress(), TimeUtil.now(), true, true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        Main.getInstance().getOnlineUserCache().removeKey(player.getUniqueId().toString());
        Main.getInstance().getDatabase().updateUser(player.getUniqueId().toString(), player.getName(), player.getAddress().getAddress().getHostAddress(), TimeUtil.now(), false, false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerKickEvent event) {
        var player = event.getPlayer();
        Main.getInstance().getOnlineUserCache().removeKey(player.getUniqueId().toString());
        Main.getInstance().getDatabase().updateUser(player.getUniqueId().toString(), player.getName(), player.getAddress().getAddress().getHostAddress(), TimeUtil.now(), false, false);
    }
}

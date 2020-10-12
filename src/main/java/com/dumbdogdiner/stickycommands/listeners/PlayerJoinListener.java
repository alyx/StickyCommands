package com.dumbdogdiner.stickycommands.listeners;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.User;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles logic relating to players joining and leaving the server.
 */
public class PlayerJoinListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Main.getInstance().getOnlineUserCache().put(User.fromPlayer(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Main.getInstance().getOnlineUserCache().removeKey(e.getPlayer().getUniqueId().toString());
    }
}

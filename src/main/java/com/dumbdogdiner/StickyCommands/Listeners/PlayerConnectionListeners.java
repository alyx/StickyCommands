package com.dumbdogdiner.StickyCommands.Listeners;

import java.sql.Timestamp;

import com.dumbdogdiner.StickyCommands.Utils.DatabaseUtil;
import com.dumbdogdiner.StickyCommands.Utils.TimeUtil;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListeners implements Listener {
    @EventHandler
    public void OnPlayerConnect(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String ipaddr = player.getAddress().getAddress().getHostAddress();
        Timestamp firstjoin = TimeUtil.TimestampNow();
        Timestamp lastjoin = TimeUtil.TimestampNow();

        if (!player.hasPlayedBefore()) {
            DatabaseUtil.InsertUser(uuid, player.getName(), ipaddr, firstjoin, lastjoin, true);
            return;
        }
        DatabaseUtil.UpdateUser(uuid, player.getName(), ipaddr, lastjoin, true, true);
    }

    @EventHandler
    public void OnPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        DatabaseUtil.UpdateUser(player.getUniqueId().toString(), player.getName(), player.getAddress().getAddress().getHostAddress(), TimeUtil.TimestampNow(), false, false);
    }

    @EventHandler
    public void OnPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DatabaseUtil.UpdateUser(player.getUniqueId().toString(), player.getName(), player.getAddress().getAddress().getHostAddress(), TimeUtil.TimestampNow(), false, false);
    }

}
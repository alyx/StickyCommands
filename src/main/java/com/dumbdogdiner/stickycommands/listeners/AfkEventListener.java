package com.dumbdogdiner.stickycommands.listeners;

import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.User;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class AfkEventListener implements Listener {
    TreeMap<String, String> variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        var player = event.getPlayer();

        var from = event.getFrom();
        var to = event.getTo();

        var x = Math.floor(from.getX());
        var z = Math.floor(from.getZ());
        var y = Math.floor(from.getY());

        if (Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z || Math.floor(to.getY()) != y) {
            checkAfk(player, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        checkAfk(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        checkAfk(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        checkAfk(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        checkAfk(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        checkAfk(event.getPlayer(), event);
    }

    private void checkAfk(Player player, PlayerEvent event) {
        var user = Main.getInstance().getOnlineUser(player.getUniqueId());
        if (user.isAfk()) {
            variables.put("player", player.getName());
            System.out.println(variables.size());
            user.setAfk(false);
            for (var p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Main.getInstance().getLocaleProvider().translate("not-afk-message", variables));
            }
        }
    }
}

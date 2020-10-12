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
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    TreeMap<String, String> variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        User u = Main.getInstance().getOnlineUser(player.getUniqueId());
        variables.put("player", player.getName());

        Location from = event.getFrom();
        Location to = event.getTo();

        double x = Math.floor(from.getX());
        double z = Math.floor(from.getZ());
        double y = Math.floor(from.getY());
        if (Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z || Math.floor(to.getY()) != y) {
            if (u.isAfk()) {
                u.setAfk(false);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(Main.getInstance().getLocaleProvider().translate("not-afk-message", variables));
                }
            }
        }
    }
}

package com.dumbdogdiner.StickyCommands.Listeners;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.concurrent.Future;

import com.dumbdogdiner.StickyCommands.Main;
import com.dumbdogdiner.StickyCommands.Utils.DatabaseUtil;
import com.dumbdogdiner.StickyCommands.Utils.Messages;
import com.dumbdogdiner.StickyCommands.Utils.TimeUtil;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListeners implements Listener {
    private Main self = Main.getPlugin(Main.class);
    @EventHandler
    public void OnPlayerConnect(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String ipaddr = player.getAddress().getAddress().getHostAddress();
        Timestamp firstjoin = TimeUtil.TimestampNow();
        Timestamp lastjoin = TimeUtil.TimestampNow();
        if (self.sqlError)
            return;
        if (!player.hasPlayedBefore()) {
            DatabaseUtil.InsertUser(uuid, player.getName(), ipaddr, firstjoin, lastjoin, true);
            return;
        }
        DatabaseUtil.UpdateUser(uuid, player.getName(), ipaddr, lastjoin, true, true);

        try {
            Future<ResultSet> res = DatabaseUtil.LookupUser(uuid);
            ResultSet result = res.get();
            if (result.wasNull()) {
                // Format our message.
                player.sendMessage("you really shouldn't have reached this.");
                return;
            }

            if (result.next()) {
                player.setFlySpeed(result.getFloat("FlySpeed"));
                player.setWalkSpeed(result.getFloat("WalkSpeed"));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(Messages.serverError);
        }
        

    }

    @EventHandler
    public void OnPlayerKick(PlayerKickEvent event) {
        if (self.sqlError)
            return;
        Player player = event.getPlayer();
        DatabaseUtil.UpdateUser(player.getUniqueId().toString(), player.getName(), player.getAddress().getAddress().getHostAddress(), TimeUtil.TimestampNow(), false, false);
    }

    @EventHandler
    public void OnPlayerQuit(PlayerQuitEvent event) {
        if (self.sqlError)
            return;
        Player player = event.getPlayer();
        System.out.println("lmao");
        DatabaseUtil.UpdateUser(player.getUniqueId().toString(), player.getName(), player.getAddress().getAddress().getHostAddress(), TimeUtil.TimestampNow(), false, false);
    }

}
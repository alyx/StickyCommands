package com.dumbdogdiner.stickycommands.listeners;

import com.dumbdogdiner.stickycommands.StickyCommands;
import com.dumbdogdiner.stickycommands.utils.PowerTool;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractionListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var user = StickyCommands.getInstance().getOnlineUser(player.getUniqueId());
        var is = player.getInventory().getItemInMainHand();
        if (is.getType() == Material.AIR || user == null)
            return;

        // Bukkit is stupid, and this event fires twice if a player is looking at a block, so this means the powertool will execute twice because
        // it is detecting both air and a block being clicked at the same time, thanks bukkit, you're wonderful. If anyone has any ideas on how to fix this, please let me know... -zach
        // TODO Event fires twice if a block is right clicked
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR))
            return;

        if (user.getPowerTools() != null) {
            for (PowerTool pt : user.getPowerTools().values()) {
                if (pt.getItem().getType() != is.getType())
                    continue;
                pt.execute();
                event.setCancelled(true);
                break;
            }
        }
    }
}
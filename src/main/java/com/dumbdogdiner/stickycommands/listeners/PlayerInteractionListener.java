package com.dumbdogdiner.stickycommands.listeners;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractionListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var is = player.getInventory().getItemInMainHand();
        var meta = is.getItemMeta();
        if (is.getType() == Material.AIR)
            return;
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            assert lore != null;
            String[] metaCheck = lore.get(0).split(":");
            if (metaCheck[0].equalsIgnoreCase("command") && metaCheck[1] != null) {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK
                        || event.getAction() == Action.LEFT_CLICK_AIR) {
                    if (metaCheck[1].equalsIgnoreCase("c") && metaCheck.length > 2) {
                        player.chat(metaCheck[2]);
                        event.setCancelled(true);
                        return;
                    }
                    player.performCommand(metaCheck[1]);
                    event.setCancelled(true);
                }
            }
        }
    }
}
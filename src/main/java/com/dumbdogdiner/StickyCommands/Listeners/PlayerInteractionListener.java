package com.dumbdogdiner.StickyCommands.Listeners;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerInteractionListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack is = player.getInventory().getItemInMainHand();
        ItemMeta meta = is.getItemMeta();
        if (is.getType() == Material.AIR || is.getType() == null) return;
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            String[] metaCheck = lore.get(0).split(":");
            if (metaCheck[0].equalsIgnoreCase("command") && metaCheck[1] != null) {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
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
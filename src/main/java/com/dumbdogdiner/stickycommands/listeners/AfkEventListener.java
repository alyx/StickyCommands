package com.dumbdogdiner.stickycommands.listeners;

import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.StickyCommands;
import com.dumbdogdiner.stickycommands.User;
import com.dumbdogdiner.stickycommands.commands.AfkCommand;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.jetbrains.annotations.NotNull;

public class AfkEventListener implements Listener {
    TreeMap<String, String> variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        User user = StickyCommands.getInstance().getOnlineUser(event.getPlayer().getUniqueId());
        // Let's make sure this is only a 3 block buffer!
        if (user.getBlockBuffer().size() > 3)
            user.getBlockBuffer().remove(user.getBlockBuffer().iterator().next()); // Remove the first entry
        
        var player = event.getPlayer();
        
        var from = event.getFrom();
        var to = event.getTo();
        var hasMoved = (Math.floor(to.getX()) != Math.floor(from.getX()) || Math.floor(to.getY()) != Math.floor(from.getY()) || Math.floor(to.getZ()) != Math.floor(from.getZ()));
        
        if (hasMoved) {
            // Always add to our block buffer
            user.getBlockBuffer().add(event.getFrom().getBlock().getType());
            if ((player.getWorld().getBlockAt(event.getTo()).getType() != Material.WATER) 
                && (!player.isSwimming() || !player.isInsideVehicle() || !player.isGliding())
                && !user.getBlockBuffer().contains(Material.WATER)
                && (!nearbyContainsPlayer(player, 1, 1, 1))) {
                    // Reset their AFK status
                    checkAfk(player, event);
            }
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
    public void onToggleSprint(PlayerToggleSprintEvent event) {
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
        @NotNull
        User user = StickyCommands.getInstance().getOnlineUser(player.getUniqueId());
        if (user == null) // If for some reason, their user object doesn't exist (???) let's create a new one.
            StickyCommands.getInstance().getOnlineUserCache().put(player.getUniqueId(), User.fromPlayer(player));

        assert user != null;
        user.resetAfkTime();
        if (user.isAfk()) {
            AfkCommand.setAFKAndBroadcast(user, false);
        }
    }

    boolean nearbyContainsPlayer(Player player, Integer x, Integer y, Integer z) {
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), x, y, z)) {
            // We need to check if something like a sheep or villager moved the player! So we can just use LivingEntity
            if (entity instanceof LivingEntity && !(entity.equals(player)))
                return true;
        }
        return false;
    }
}

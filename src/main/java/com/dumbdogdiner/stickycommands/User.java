package com.dumbdogdiner.stickycommands;

import java.util.ArrayList;
import java.util.UUID;

import com.dumbdogdiner.stickyapi.common.cache.Cacheable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import lombok.Getter;
import lombok.Setter;

public class User implements Cacheable {

    /**A
     * The username of the user.
     */
    @Getter
    @Setter
    private String name;

    /**
     * The UUID of the user.
     */
    @Getter
    @Setter
    private UUID uniqueId;

    /**
     * Whether or not this user is AFK.
     */
    @Getter
    @Setter
    private boolean afk;
    
    @Getter
    @Setter
    private Integer afkTime = 0;
    
   // I spent an hour trying to come up with a good solution to this weird problem where if you are being pushed by water, and on the corner water block, your from block is considered air and not water...
   // So, we need to keep a buffer of the last 3 blocks the player stood in, and if it contains water, we'll consider it as the water pushing them, since there's no event for
   // checking if a player is being pushed by water!
    @Getter
    private ArrayList<Material> blockBuffer = new ArrayList<Material>();

    public User(String username, UUID uniqueId) {
        this.name = username;
        this.uniqueId = uniqueId;
    }

    /**
     * Get the {@link org.bukkit.entity.Player} object from this user
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(this.uniqueId);
    }

    /**
     * Create a new user object from a player object.
     */
    public static User fromPlayer(Player player) {
        return new User(player.getName(), player.getUniqueId());
    }

    public String getKey() {
        return this.uniqueId.toString();
    }

    public void setSpeed(SpeedType type, Float speed) {
        speed = speed < 1.9F 
                ? (speed > 0F
                    ? (type == SpeedType.FLY
                            ? speed
                            : speed + 0.1F > 1F
                                ? speed
                                : speed + 0.1F)
                    : 0.1F) 
                : 1F;
        switch(type) {
            case FLY:
                Bukkit.getPlayer(this.uniqueId).setFlySpeed(speed);
                // Main.getInstance().getDatabase().setSpeed(this.uniqueId, speed, 1);
                break;
            case WALK:
                Bukkit.getPlayer(this.uniqueId).setWalkSpeed(speed);
                // Main.getInstance().getDatabase().setSpeed(this.uniqueId, speed, 0);
                break;
        }
    }
}
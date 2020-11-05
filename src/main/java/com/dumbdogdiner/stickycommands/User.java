package com.dumbdogdiner.stickycommands;

import java.util.ArrayList;
import java.util.UUID;

import com.dumbdogdiner.stickyapi.common.cache.Cacheable;
import com.dumbdogdiner.stickycommands.utils.SpeedType;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.Setter;

public class User implements Cacheable {

    /**
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
     * We need a CUSTOM setter.
     */
    @Getter
    private boolean afk = false;

    @Getter
    private Integer afkTime = 0;

    public boolean setAfk(boolean AFKState){
        if(!AFKState)
            afkTime = 0;
        afk = AFKState;
        if(afk){
            getPlayer().setMetadata("AFK", new FixedMetadataValue(StickyCommands.getInstance(), "&8[AFK]"));
        } else {
            getPlayer().removeMetadata("AFK", StickyCommands.getInstance());
        }
        return afk;
    }

    public int incAfkTime(){
        return ++afkTime;
    }

    public void resetAfkTime(){
        afkTime = 0;
    }
    
   // I spent an hour trying to come up with a good solution to this weird problem where if you are being pushed by water, and on the corner water block, your from block is considered air and not water...
   // So, we need to keep a buffer of the last 3 blocks the player stood in, and if it contains water, we'll consider it as the water pushing them, since there's no event for
   // checking if a player is being pushed by water!
    @Getter
    @NotNull
    private ArrayList<Material> blockBuffer = new ArrayList<Material>();

    public User(String username, UUID uniqueId) {
        this.name = username;
        this.uniqueId = uniqueId;
    }

    public User(Player player) {
        this.name = player.getName();
        this.uniqueId = player.getUniqueId();
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
        if (speed <= 0F)
            speed = 0.1F;

        else if (speed > 1F)
            speed = 1F;

        if (type == SpeedType.WALK)
            speed = speed + 0.1F > 1F ? speed : speed + 0.1F;
                
        switch(type) {
            case FLY:
                Bukkit.getPlayer(this.uniqueId).setFlySpeed(speed);
                StickyCommands.getInstance().getDatabase().setSpeed(this.uniqueId, speed, 1);
                break;
            case WALK:
                Bukkit.getPlayer(this.uniqueId).setWalkSpeed(speed);
                StickyCommands.getInstance().getDatabase().setSpeed(this.uniqueId, speed, 0);
                break;
        }
    }
}
package com.dumbdogdiner.stickycommands;

import com.dumbdogdiner.stickyapi.common.cache.Cacheable;
import com.dumbdogdiner.stickycommands.utils.Item;
import com.dumbdogdiner.stickycommands.utils.PowerTool;
import com.dumbdogdiner.stickycommands.utils.SpeedType;
import lombok.Getter;
import lombok.Setter;
import me.xtomyserrax.StaffFacilities.SFAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

//TODO Move to stickyapi

public class User implements Cacheable {

    /**
     * The username of the user.
     */
    @Getter
    @Setter
    @NotNull
    private String name;

    /**
     * The UUID of the user.
     */
    @Getter
    @Setter
    @NotNull
    private UUID uniqueId;

    /**
     * The list of powertools the user has
     */
    @Getter
    private HashMap<Material, PowerTool> powerTools = new HashMap<Material, PowerTool>();

    /**
     * Whether or not this user is AFK.
     * We need a CUSTOM setter.
     */
    @Getter
    @NotNull
    private boolean afk = false;

    @Getter
    @NotNull
    private Integer afkTime = 0;

    public void setAfk(boolean AFKState){
        if(!AFKState)
            afkTime = 0;
        afk = AFKState;
    }

    /**
     * Checks if a given player is hidden, vanished, staffvanished, or fakeleaved
     * @return Whether the user is hidden.
     */
    public boolean isHidden(){
        if(StickyCommands.getInstance().isStaffFacilitiesEnabled()){
            Player player = this.getPlayer();
            /*System.out.println(SFAPI.isPlayerFakeleaved(player));
            System.out.println(SFAPI.isPlayerStaffVanished(player));
            System.out.println(SFAPI.isPlayerVanished(player));
            System.out.println(isVanished()); */
            return  SFAPI.isPlayerFakeleaved(player) ||
                    SFAPI.isPlayerStaffVanished(player) ||
                    SFAPI.isPlayerVanished(player) ||
                    isVanished();
        }
        return false;
    }

    /**
     * Checks if a given player is in a vanished state.
     *
     * @return Whether the user is vanished.
     */
    public boolean isVanished() {
        for (MetadataValue meta : getPlayer().getMetadata("vanished")) {
            if (meta.asBoolean()) {
                return true;
            }
        }

        return false;
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

    public User(@NotNull String username, @NotNull UUID uniqueId) {
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

    public void addPowerTool(PowerTool powerTool) {
        this.powerTools.put(powerTool.getItem().getType(), powerTool);
    }

    public void removePowerTool(Item item) {
        for (PowerTool pt : this.powerTools.values()) {
            if (item.getType() == pt.getItem().getType())
                this.powerTools.remove(pt.getItem().getType());
        }
    }

    public void setSpeed(SpeedType type, Float speed) {
        if (speed <= 0F)
            speed = 0.1F;

        else if (speed > 1F)
            speed = 1F;

        if (type == SpeedType.WALK)
            speed = (speed + 0.1F > 1F) ? speed : speed + 0.1F;

        Player p = getPlayer();
        assert p != null;
                
        switch(type) {
            case FLY:
                p.setFlySpeed(speed);
                StickyCommands.getInstance().getDatabase().setSpeed(this.uniqueId, speed, 1);
                break;
            case WALK:
                p.setWalkSpeed(speed);
                StickyCommands.getInstance().getDatabase().setSpeed(this.uniqueId, speed, 0);
                break;
        }
    }
}
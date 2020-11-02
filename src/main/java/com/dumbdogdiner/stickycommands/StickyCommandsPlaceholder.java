package com.dumbdogdiner.stickycommands;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class StickyCommandsPlaceholder extends PlaceholderExpansion {
    private static StickyCommandsPlaceholder INSTANCE;
    private StickyCommandsPlaceholder(){

    }


    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
        return StickyCommands.getInstance().getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>The identifier has to be lowercase and can't contain _ or %
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier(){
        return StickyCommands.getInstance().getName().toLowerCase();
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
        return StickyCommands.getInstance().getDescription().getVersion();
    }

    public static StickyCommandsPlaceholder getInstance(){
        if(INSTANCE == null)
            INSTANCE = new StickyCommandsPlaceholder();
        return INSTANCE;
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A {@link org.bukkit.entity.Player Player}.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier){
        // For now: ASSUME player is the player we want to know if is AFK;

        // %stickycommands_afk%
        if(identifier.equals("afk")){
            return StickyCommands.getInstance().getOnlineUser(player.getUniqueId()).isAfk() ? "&8[AFK]" : "";
        }


        // We return null if an invalid placeholder was provided
        return null;
    }
}
